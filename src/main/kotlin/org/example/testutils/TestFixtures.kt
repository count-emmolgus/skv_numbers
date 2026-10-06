package org.example.testutils

import java.io.File
import kotlin.random.Random

private val random = Random(1)

private val pnrFolder = File("src/main/resources/pnr")
private val samordningsFolder = File("src/main/resources/samordning")

object TestFixtures {
    val pnr = pnrFolder.asDictOfPersonIdentifiers()
    val samordningsNumber = samordningsFolder.asDictOfPersonIdentifiers()

    fun makeTestPerson(
        name: String? = null,
        email: String? = null,
    ): TestPerson {
        val pnr = pnr.entries.random(random).value.random(random)
        val name = name ?: pnr
        return TestPerson(
            personNumber = pnr,
            name = name,
            email = "p"
        )
    }


}

private fun File.asDictOfPersonIdentifiers(): Map<String, List<String>> {
    return listFiles { it.isFile }!!.associate { file ->
        file.name to file.readLines().filterNot { it.isBlank() }
    }
}