package com.baz.phonebook

import com.baz.phonebook.helpers.Either
import com.baz.phonebook.helpers.failWith
import java.io.PrintStream


sealed class PhoneBookError {
    object NumberAlreadyExist : PhoneBookError()
}

/**
 * A phone book keeping numbers ordered by name
 */
class PhoneBook<T> {
    private val nameToPhoneNumber: MutableMap<String, PhoneNumber<T>> = sortedMapOf()

    fun getPhoneNumber(name: String): PhoneNumber<T>? =
        nameToPhoneNumber[name]

    /**
     * Add a new phone number
     * @return the new phone number if it was added
     */
    fun addPhoneNumber(number: PhoneNumber<T>): Either<PhoneNumber<T>, PhoneBookError> =
        nameToPhoneNumber.putIfAbsent(number.name, number)
            ?.let { failWith(PhoneBookError.NumberAlreadyExist) }
            ?: Either.Success(number)

    fun removePhoneNumber(name: String): PhoneNumber<T>? =
        nameToPhoneNumber.remove(name)

    fun printPhoneBook(outStream: PrintStream): Unit =
        nameToPhoneNumber.values.forEach(outStream::println)

    fun getAllPhoneNumbers() =
        nameToPhoneNumber.values.asSequence()
}

