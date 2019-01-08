package com.baz.phonebook.parser

import com.baz.phonebook.helpers.Either
import org.spockframework.util.Assert
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class BulgarianPhoneNumberParserTest extends Specification {
    def parser = new BulgarianPhoneNumberParser()

    def "valid phone number #number"() {
        expect:
        def result = parser.asNormalizedBulgarianPhoneNumber(number)

        Assert.that(result instanceof Either.Success<NormalizedBulgarianPhoneNumber>)

        (result as Either.Success<NormalizedBulgarianPhoneNumber>).value.normalizedNumber == normalizedNumber

        where:
        number           || normalizedNumber
        '+359878123456'  || '+359878123456'
        '0878123456'     || '+359878123456'
        '00359878123456' || '+359878123456'
    }

    def "invalid phone number #number"() {
        expect:
        def result = parser.asNormalizedBulgarianPhoneNumber(number)
        Assert.that(result instanceof Either.Error<ParserError>)

        (result as Either.Error<ParserError>).error.class == error

        where:
        number           || error
        // empty number
        ''               || ParserError.CountryCodeError
        // invalid country code
        '+358878123456'  || ParserError.CountryCodeError
        '00358878123456' || ParserError.CountryCodeError
        '00878123456'    || ParserError.CountryCodeError
        '1878123456'     || ParserError.CountryCodeError
        // invalid operator
        '+359828123456'  || ParserError.OperatorCodeError
        '+359278123456'  || ParserError.OperatorCodeError
        // digit not between 2-9
        '+359870123456'  || ParserError.InvalidNumberError
        '+359871123456'  || ParserError.InvalidNumberError
        // more digits
        '+3598781234567' || ParserError.InvalidNumberError
        // less digits
        '+35987812345'   || ParserError.InvalidNumberError
    }
}
