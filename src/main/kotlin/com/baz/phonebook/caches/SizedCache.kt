package com.baz.phonebook.caches

import com.baz.phonebook.helpers.Either
import com.baz.phonebook.helpers.failWith
import java.util.*

sealed class SizedCacheError {
    object InvalidatedCache : SizedCacheError()
}

/**
 * Data Structure for caching the Top N values based on a comparator
 *
 * Not thread safe
 */
class SizedCache<T>(comparator: Comparator<T>, private val cacheSize: Int) {
    private val cachedValues = PriorityQueue<T>(cacheSize + 1, comparator.reversed())
    private var isInvalidated: Boolean = false

    private fun addValue(value: T) {
        cachedValues.add(value)
        if (cachedValues.size > cacheSize) {
            cachedValues.poll()
        }
    }

    private fun getOrderedCachedValues(): List<T> =
        cachedValues.sortedWith(cachedValues.comparator().reversed())

    /**
     * @return the cached values ordered by the provided comparator if cache is valid or InvalidatedCache error
     */
    fun getCachedValues(): Either<List<T>, SizedCacheError> =
        isCacheValid()
            .takeIf { it }
            ?.let { Either.Success(getOrderedCachedValues()) }
            ?: failWith(SizedCacheError.InvalidatedCache)

    /**
     * Compares all the values in the cache against the provided value using Object.equals
     * @return true if any of the values returns true
     */
    fun contains(value: T): Boolean =
        cachedValues.contains(value)

    /**
     * Clear the cache and mark as invalidated
     */
    fun invalidateCache() {
        cachedValues
            .clear()
            .also { isInvalidated = true }
    }

    /**
     * @return true if the cache has not been invalidated since its last recreation
     */
    fun isCacheValid(): Boolean =
        !isInvalidated

    /**
     * Recreate the cache with new values
     * @return the newly cached values
     */
    fun recreateCache(values: Sequence<T>): List<T> =
        values
            .also { this.invalidateCache() }
            .forEach(this::addValue)
            .also { isInvalidated = false }
            .let { getOrderedCachedValues() }

    /**
     * Try to update the cache with new values if it is still valid
     */
    fun updateCacheIfValid(values: Sequence<T>) {
        values
            .takeIf { isCacheValid() }
            ?.forEach(this::addValue)
    }

    fun updateExistingIfValid(value: T) {
        value
            .takeIf { contains(value) }
            ?.let {
                cachedValues.remove(value)
                cachedValues.add(value)
            }
    }
}
