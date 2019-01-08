package com.baz.phonebook

/**
 * A phone number with its corresponding number of outgoing calls
 */
data class PhoneNumber<T>(val name: String, val number: T, val numCalls: Long = 0L) {
    override fun equals(other: Any?): Boolean {
        if (other is PhoneNumber<*>) {
            return name == other.name && number == other.number
        }

        return false
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
