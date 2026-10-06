package org.example.testutils

import java.time.LocalDate

data class TestPerson(
    val personNumber: String,
    val name: String,
    val email: String,
) {
    val dateOfBirth: LocalDate
        get() = LocalDate.parse(personNumber.substring(0, 8))
}

data class TestSamordningsPerson(
    val samordningsNumber: String,
    val name: String,
    val email: String
) {
    val dateOfBirth: LocalDate
        get() {
            val date = (samordningsNumber.substring(0, 8).toInt() - 60).toString()
            return LocalDate.parse(date)
        }
}
