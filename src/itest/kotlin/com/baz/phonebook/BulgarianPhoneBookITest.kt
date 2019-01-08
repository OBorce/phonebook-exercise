package com.baz.phonebook

import com.baz.phonebook.helpers.Either
import org.junit.Assert
import org.junit.Test


class BulgarianPhoneBookITest {

    @Test
    fun testAddPhoneNumber() {
        val bulgarianPhoneBook = BulgarianPhoneBook()

        val name = "foo"
        val number = "+359878123456"
        val numOutCalls = 20L
        val addAmountOfNamOutCalls = 20L

        val addPhoneNumber = bulgarianPhoneBook.addPhoneNumber(name, number, numOutCalls)

        Assert.assertTrue(addPhoneNumber is Either.Success)

        val addedPhoneNumber = (addPhoneNumber as Either.Success).value

        Assert.assertEquals(number, addedPhoneNumber.number.normalizedNumber)
        Assert.assertEquals(numOutCalls, addedPhoneNumber.numCalls)

        val existingPhoneNumber = bulgarianPhoneBook.getPhoneNumber(name)

        Assert.assertNotNull(existingPhoneNumber)
        Assert.assertEquals(addedPhoneNumber, existingPhoneNumber)

        val updated = bulgarianPhoneBook.addOutgoingCallsForNumber(name, addAmountOfNamOutCalls)
        Assert.assertTrue(updated)

        val updatedPhoneNumber = bulgarianPhoneBook.getPhoneNumber(name)
        Assert.assertNotNull(updatedPhoneNumber)
        Assert.assertEquals(number, updatedPhoneNumber!!.number.normalizedNumber)
        Assert.assertEquals(numOutCalls + addAmountOfNamOutCalls, updatedPhoneNumber.numCalls)

        val removed = bulgarianPhoneBook.removePhoneNumber(name)
        Assert.assertTrue(removed)
    }

    @Test
    fun testReadFromFile() {
        val numbers: List<Pair<String, String>> = listOf(
            "foo" to "+359878123456",
            "incorrect" to "009878123456",
            "bar" to "+359878223456",
            "baz" to "+359878423456",
            "wrong" to "++359878123456",
            "hello" to "+359878121456",
            "there" to "+359878129456"
        )
        val file = createTempFile()

        file.bufferedWriter().use { buf ->
            numbers.forEachIndexed { idx, it ->
                if (idx > 0) {
                    buf.newLine()
                }

                buf.write(it.first)
                buf.write(",")
                buf.write(it.second)
            }
        }

        val bulgarianPhoneBook = BulgarianPhoneBook()

        bulgarianPhoneBook.readFromFile(file.absolutePath)

        numbers
            .filter { it.first != "incorrect" && it.first != "wrong" }
            .forEach {
                val number = bulgarianPhoneBook.getPhoneNumber(it.first)
                Assert.assertNotNull(number)
                Assert.assertEquals(it.second, number!!.number.normalizedNumber)
            }
    }
}
