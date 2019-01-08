package com.baz.phonebook.parser

import com.baz.phonebook.helpers.Either
import com.baz.phonebook.helpers.failWith
import com.baz.phonebook.parser.ParserResult.NormalizedBulgarianPhoneNumber

/*
A normalized Bulgarian phone number has the following format:
+359878123456, where
+359 is the country code
the next 2 digits are the mobile operator’s code and are one of the following sequences: 87, 88, 89
the next digit is a digit between 2 to 9
the next 6 digits are digits between 0 and 9

The following formats are allowed:
0878123456 - 0 replaces +359
00359878123456 - 00 replaces +
 */
private val countryCodeRegex = """^((\+359)|(00359)|(0(?!0)))""".toRegex()
private val operatorCodeRegex = """^8[7-9]""".toRegex()
private val bulgarianNumberRegex = """^[2-9][0-9]{6}$""".toRegex()



class NormalizedBulgarianPhoneNumber internal constructor(val normalizedNumber: String)

sealed class ParserError {
    object CountryCodeError : ParserError()
    object OperatorCodeError : ParserError()
    object InvalidNumberError : ParserError()
}

typealias ParserResult = Either<NormalizedBulgarianPhoneNumber, ParserError>

private const val BulgarianPhoneNumberPrefix = "+359"
private const val BulgarianPhoneNumberLength = 9

/**
 * Parser for valid bulgarian phone numbers
 */
class BulgarianPhoneNumberParser {
    private data class RegexWithError(val regex: Regex, val parserError: ParserError)

    private val parsers = listOf(
        RegexWithError(countryCodeRegex, ParserError.CountryCodeError),
        RegexWithError(operatorCodeRegex, ParserError.OperatorCodeError),
        RegexWithError(bulgarianNumberRegex, ParserError.InvalidNumberError)
    )

    private fun normalizePhoneNumber(number: String) =
        (BulgarianPhoneNumberPrefix + number.takeLast(BulgarianPhoneNumberLength))
            .let(::NormalizedBulgarianPhoneNumber)

    private fun endOfMatch(regex: Regex, string: CharSequence): Int? =
        regex.find(string)
            ?.run { range.last + 1 }

    private fun checkInvalidNumber(number: String): ParserResult? {
        parsers
            .fold(0) { startIndex, parser ->
                endOfMatch(parser.regex, number.subSequence(startIndex, number.length))
                    ?.let { it + startIndex }
                    ?: return failWith(parser.parserError)
            }

        return null
    }

    /**
     * @return a normalized bulgarian PhoneNumber if the input number is valid else the parser error
     */
    fun asNormalizedBulgarianPhoneNumber(number: String): ParserResult =
        checkInvalidNumber(number)
            ?: Either.Success(normalizePhoneNumber(number))
}
