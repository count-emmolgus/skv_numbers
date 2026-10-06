package org.example.internal

import org.apache.commons.csv.CSVFormat
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.io.path.*


internal class ParseFiles {

    data class FakeDataResponse(
        val name: String,
        val email: String
    )

    companion object {
        private val csvReader = CSVFormat.Builder.create(CSVFormat.DEFAULT).apply {
            setIgnoreSurroundingSpaces(true)
        }.build()

        private val httpClient = HttpClient.newHttpClient()

        private val objectMapper = jacksonObjectMapper()

        @JvmStatic
        fun main(args: Array<String>) {
            val resourceFolder = File("src/main/resources")
            val csvFiles = resourceFolder.walk().filter { it.name.endsWith(".csv") }
            val (pnrFiles, samordningFiles) = csvFiles.partition { it.path.contains("pnr") }

            val storedSamordningsNumberAmount = storeAsFiles(samordningFiles)
            val storedPnrAmount = storeAsFiles(pnrFiles)

            val totalAmount = storedSamordningsNumberAmount + storedPnrAmount // 47983

            val request = HttpRequest.newBuilder()
                .uri(URI("https://fejka.nu/?json=1&num=$totalAmount"))
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            val fakeData: List<FakeDataResponse> = objectMapper.readValue(response.body())

            val fakePersonFile = Path("$resourceFolder/fakePersons.json").also { it.deleteIfExists(); it.createFile() }
            fakePersonFile.writeText(objectMapper.writeValueAsString(fakeData))
            fakePersonFile.bufferedWriter().use { writer ->
                writer.write("[")
                writer.newLine()
                fakeData.forEachIndexed { index, it ->
                    writer.write(
                        """
                        { "name": "${it.name}", "email": "${
                            it.email.replace(
                                "fejka.nu",
                                "fake-nox.se"
                            )
                        }" }${if (index != pnrFiles.lastIndex) "," else ""}
                    """.trimIndent()
                    )
                    writer.newLine()
                }
                writer.write("]")
            }
        }

        private fun storeAsFiles(files: List<File>): Int {
            val numbers = files.flatMap { file ->
                file.inputStream().use { stream ->
                    csvReader.parse(stream.reader())
                        .drop(1)
                        .map { it[0] as String }
                }
            }
            val numbersGrouped = numbers.groupBy { it.substring(0, 4) }
            val parentFolder = files.first().parentFile.parentFile

            return numbersGrouped.mapNotNull { (year, records) ->
                runCatching {
                    val file = Path("$parentFolder/$year.txt").createFile()
                    file.writeLines(records)
                }
                records.size
            }.sum()
        }
    }
}
