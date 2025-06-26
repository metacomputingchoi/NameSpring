// model/filter/FilterValidationHelper.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.util.normalizeNFC

object FilterValidationHelper {

    fun <T> extractAndValidate(
        data: Map<String, Int>,
        threshold: Int,
        transformer: (String) -> T
    ): List<T> {
        return data.filterValues { it == threshold }
            .keys
            .map(transformer)
    }

    fun extractElementsByThreshold(
        sajuOhaengCount: Map<String, Int>,
        threshold: Int
    ): List<String> {
        return extractAndValidate(sajuOhaengCount, threshold) { it.normalizeNFC() }
    }

    fun createDetails(
        vararg pairs: Pair<String, Any>
    ): MutableMap<String, Any> {
        return mutableMapOf(*pairs)
    }
}