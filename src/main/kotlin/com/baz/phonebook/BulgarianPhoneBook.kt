package com.baz.phonebook

import com.baz.phonebook.caches.SizedCache
import com.baz.phonebook.caches.SizedCacheError
import com.baz.phonebook.helpers.Either
import com.baz.phonebook.helpers.failWith
import com.baz.phonebook.parser.*
import java.io.*
import java.lang.Exception

typealias BulgarianPhoneNumber = PhoneNumber<NormalizedBulgarianPhoneNumber>

sealed class AddPhoneNumberError {
    class Parser(error: ParserError) : AddPhoneNumberError()
    class PhoneBook(error: PhoneBookError) : AddPhoneNumberError()
}

sealed class ParseFileError(val errorMessage: String?) {
    class FileNotFound(errorMessage: String?) : ParseFileError(errorMessage)
    class IOError(errorMessage: String?) : ParseFileError(errorMessage)
    class UnknownError(errorMessage: String?) : ParseFileError(errorMessage)
}

/**
 * Class keeping track of bulgarian phone numbers in alphabetic order
 * and the top 5 phone numbers with the most outgoing calls
 */
class BulgarianPhoneBook {
    private val phoneBook = PhoneBook<NormalizedBulgarianPhoneNumber>()
    private val phoneNumberParser = BulgarianPhoneNumberParser()
    private val top5NumbersByNumOfOutCalls =
        SizedCache<BulgarianPhoneNumber>(compareByDescending { it.numCalls }, cacheSize = 5)

    /**
     * Add a new phone number
     * @return Either the newly added BulgarianPhoneNumber or the occurred Error by the Parser or the PhoneBook
     */
    fun addPhoneNumber(
        name: String,
        number: String,
        numOutgoingCalls: Long = 0L
    ): Either<BulgarianPhoneNumber, AddPhoneNumberError> =
        phoneNumberParser.asNormalizedBulgarianPhoneNumber(number)
            .let { result ->
                when (result) {
                    is Either.Success -> updatePhoneBook(BulgarianPhoneNumber(name, result.value, numOutgoingCalls))
                    is Either.Error -> failWith(AddPhoneNumberError.Parser(result.error))
                }
            }

    fun getPhoneNumber(name: String): BulgarianPhoneNumber? =
        phoneBook.getPhoneNumber(name)

    fun removePhoneNumber(name: String): Boolean =
        phoneBook
            .removePhoneNumber(name)
            ?.also(this::removeFromCache) != null

    /**
     * Update the number of outgoing calls for an existing number by the provided amount
     * @return true if number was found and updated
     */
    fun addOutgoingCallsForNumber(name: String, numberOfOutCalls: Long): Boolean =
        phoneBook.removePhoneNumber(name)
            ?.let { it.copy(numCalls = it.numCalls + numberOfOutCalls) }
            ?.also(top5NumbersByNumOfOutCalls::updateExistingIfValid)
            ?.let(phoneBook::addPhoneNumber) != null

    /**
     * Print each phone number in new line ordered alphabetically by name
     */
    fun printPhoneBook(printStream: PrintStream = System.out) {
        phoneBook.printPhoneBook(printStream)
    }

    fun getTop5NumbersByNumberOfOutgoingCalls(): List<BulgarianPhoneNumber> =
        top5NumbersByNumOfOutCalls.getCachedValues()
            .let { result ->
                when (result) {
                    is Either.Success -> result.value
                    is Either.Error ->
                        when (result.error) {
                            is SizedCacheError.InvalidatedCache -> recreateCache()
                        }
                }
            }

    /**
     * Read phone numbers from a csv file
     * @return error if unable to read from file
     */
    fun readFromFile(filePath: String): ParseFileError? {
        try {
            File(filePath)
                .bufferedReader()
                .useLines { lines ->
                    lines.forEach { line ->
                        line.split(",")
                            .takeIf { it.size == 2 }
                            ?.let { addPhoneNumber(it[0], it[1]) }
                    }
                }
        } catch (e: FileNotFoundException) {
            return ParseFileError.FileNotFound(e.message)
        } catch (e: IOException) {
            return ParseFileError.IOError(e.message)
        } catch (e: Exception) {
            return ParseFileError.UnknownError(e.message)
        }

        return null
    }

    private fun updatePhoneBook(number: BulgarianPhoneNumber): Either<BulgarianPhoneNumber, AddPhoneNumberError> =
        phoneBook.addPhoneNumber(number)
            .let { result ->
                when (result) {
                    is Either.Success -> result
                        .also { top5NumbersByNumOfOutCalls.updateCacheIfValid(sequenceOf(it.value)) }
                    is Either.Error -> failWith(AddPhoneNumberError.PhoneBook(result.error))
                }
            }

    private fun recreateCache(): List<BulgarianPhoneNumber> =
        phoneBook
            .getAllPhoneNumbers()
            .let(top5NumbersByNumOfOutCalls::recreateCache)

    private fun removeFromCache(phoneNumber: BulgarianPhoneNumber) {
        phoneNumber
            .takeIf(top5NumbersByNumOfOutCalls::contains)
            ?.let { top5NumbersByNumOfOutCalls.invalidateCache() }
    }
}
