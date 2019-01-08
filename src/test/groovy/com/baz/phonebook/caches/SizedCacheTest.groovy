package com.baz.phonebook.caches

import com.baz.phonebook.PhoneNumber
import com.baz.phonebook.helpers.Either
import kotlin.sequences.Sequence
import org.junit.Assert
import spock.lang.Specification

class SizedCacheTest extends Specification {

    def "create cache and get cached values"() {
        def cache = new SizedCache<Integer>({ a, b -> b <=> a }, 3)

        def nums = [1, 1, 1, 3, 4, 5, 6, 2, 1] as Sequence<Integer>

        when:
        def result = cache.recreateCache(nums)

        then:
        Assert.assertTrue(cache.isCacheValid())
        Assert.assertTrue(cache.contains(4))
        Assert.assertEquals([6, 5, 4] as List<Integer>, result)
    }

    def "update cache if valid"() {
        def cache = new SizedCache<Integer>({ a, b -> b <=> a }, 3)

        when:
        cache.recreateCache([1, 2, 6] as Sequence<Integer>)
        cache.updateCacheIfValid([7, 6] as Sequence<Integer>)

        then:
        def result = cache.getCachedValues()
        Assert.assertTrue(result instanceof Either.Success<List<Integer>>)
        Assert.assertEquals([7, 6, 6] as List<Integer>, (result as Either.Success<List<Integer>>).value)
    }

    def "update existing if valid"() {
        def cache = new SizedCache<PhoneNumber<Integer>>(
                { PhoneNumber a, PhoneNumber b -> b.numCalls <=> a.numCalls }, 3)

        def phoneNumbers = [
                new PhoneNumber<Integer>("foo", 123, 20),
                new PhoneNumber<Integer>("baz", 123, 10),
                new PhoneNumber<Integer>("bar", 123, 15),
                new PhoneNumber<Integer>("zzz", 123, 5),
        ]

        def updatedBaz = new PhoneNumber<Integer>("baz", 123, 30)

        def expectingResult = [updatedBaz, phoneNumbers[0], phoneNumbers[2]]

        when:
        cache.recreateCache(phoneNumbers as Sequence<PhoneNumber<Integer>>)
        cache.updateExistingIfValid(updatedBaz)

        then:
        def result = cache.getCachedValues()
        Assert.assertTrue(result instanceof Either.Success<List<PhoneNumber<Integer>>>)
        def castedResult = (result as Either.Success<List<PhoneNumber<Integer>>>)
        Assert.assertEquals(expectingResult as List<PhoneNumber<Integer>>, castedResult.value)
    }

    def "invalidate cache"() {
        def cache = new SizedCache<Integer>({ a, b -> b <=> a }, 3)

        when:
        cache.recreateCache([1, 2, 6] as Sequence<Integer>)
        cache.invalidateCache()
        cache.updateCacheIfValid([6, 7] as Sequence<Integer>)

        then:
        def result = cache.getCachedValues()
        Assert.assertTrue(result instanceof Either.Error<SizedCacheError.InvalidatedCache>)
        Assert.assertFalse(cache.contains(7))
    }
}
