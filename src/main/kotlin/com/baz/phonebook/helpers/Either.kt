package com.baz.phonebook.helpers

sealed class Either<out T, out Err> {
    data class Error<Err>(val error: Err) : Either<Nothing, Err>()
    data class Success<T>(val value: T) : Either<T, Nothing>()
}

fun <T> failWith(error: T) = Either.Error(error)
