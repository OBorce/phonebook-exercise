package com.baz.phonebook

import com.baz.phonebook.helpers.Either
import org.junit.Assert
import spock.lang.Specification

class PhoneBookTest extends Specification {

    def "phone number not found"() {
        def phonebook = new PhoneBook<Integer>()
        def name = "foo"
        def wrongName = "baz"
        def number = new PhoneNumber(name, 1234, 0)

        when:
        phonebook.addPhoneNumber(number)

        then:
        def result = phonebook.getPhoneNumber(wrongName)
        Assert.assertNull(result)
    }

    def "add phone number and retrieve"() {
        def phonebook = new PhoneBook<Integer>()
        def name = "foo"
        def number = new PhoneNumber(name, 1235, 0)

        when:
        def added = phonebook.addPhoneNumber(number)

        then:
        Assert.assertTrue(added instanceof Either.Success<PhoneNumber<Integer>>)
        Assert.assertEquals(number, (added as Either.Success<PhoneNumber<Integer>>).value)

        def result = phonebook.getPhoneNumber(name)
        Assert.assertNotNull(result)
        Assert.assertEquals(number, result)
    }

    def "add phone number already exists"() {
        def phonebook = new PhoneBook<Integer>()
        def name = "foo"
        def number = new PhoneNumber(name, 1235, 0)

        when:
        phonebook.addPhoneNumber(number)
        def added = phonebook.addPhoneNumber(number)

        then:
        Assert.assertTrue(added instanceof Either.Error<PhoneBookError.NumberAlreadyExist>)

        def result = phonebook.getPhoneNumber(name)
        Assert.assertNotNull(result)
        Assert.assertEquals(number, result)
    }

    def "remove phone number and check if exists"() {
        def phonebook = new PhoneBook<Integer>()
        def name = "foo"
        def number = new PhoneNumber(name, 1235, 0)

        when:
        def added = phonebook.addPhoneNumber(number)
        def removed = phonebook.removePhoneNumber(name)
        def getAfterRemoved = phonebook.getPhoneNumber(name)

        then:
        Assert.assertTrue(added instanceof Either.Success<PhoneNumber<Integer>>)
        Assert.assertEquals(number, (added as Either.Success<PhoneNumber<Integer>>).value)

        Assert.assertNotNull(removed)
        Assert.assertEquals(number, removed)

        Assert.assertNull(getAfterRemoved)
    }

    def "get all numbers in alphabetic order"() {
        def phonebook = new PhoneBook<Integer>()
        def name1 = "foo"
        def name2 = "abc"
        def number1 = new PhoneNumber(name1, 1235, 0)
        def number2 = new PhoneNumber(name2, 2235, 0)

        when:
        phonebook.addPhoneNumber(number1)
        phonebook.addPhoneNumber(number2)

        then:
        def list = new ArrayList<String>()
        phonebook.getAllPhoneNumbers().iterator().forEachRemaining({ it -> list.add(it.name) })
        Assert.assertEquals(2, list.size())
        Assert.assertEquals([name1, name2].sort(), list)
    }

    def "print all numbers in alphabetic order"() {
        def phonebook = new PhoneBook<Integer>()
        def name1 = "foo"
        def name2 = "abc"
        def number1 = new PhoneNumber(name1, 1235, 0)
        def number2 = new PhoneNumber(name2, 2235, 0)

        def output = """PhoneNumber(name=${number2.name}, number=${number2.number}, numCalls=${number2.numCalls})
PhoneNumber(name=${number1.name}, number=${number1.number}, numCalls=${number1.numCalls})
""".toString()

        when:
        phonebook.addPhoneNumber(number1)
        phonebook.addPhoneNumber(number2)

        def bo = new ByteArrayOutputStream()
        phonebook.printPhoneBook(new PrintStream(bo))
        bo.flush()

        then:
        Assert.assertEquals(output, bo.toString())
    }
}
