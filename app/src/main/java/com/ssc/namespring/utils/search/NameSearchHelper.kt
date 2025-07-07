// utils/search/NameSearchHelper.kt
package com.ssc.namespring.utils.search

import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.util.HangulUtils

class NameSearchHelper {

    fun matches(name: GeneratedName, query: String): Boolean {
        if (query.isEmpty()) return true

        val lowerQuery = query.lowercase()

        // 이름으로 검색
        if (matchesText(name.combinedPronounciation, lowerQuery)) return true

        // 한자로 검색
        if (name.combinedHanja.contains(query)) return true

        // 의미로 검색
        val meanings = name.hanjaDetails.mapNotNull { it.inmyongMeaning.takeIf { it.isNotBlank() } }
        if (meanings.any { it.contains(query) }) return true

        return false
    }

    private fun matchesText(text: String, query: String): Boolean {
        val lowerText = text.lowercase()

        // 일반 포함 검색
        if (lowerText.contains(query)) return true

        // 초성 검색
        if (matchesChosung(text, query)) return true

        // 혼합 검색 (초성+완성형)
        if (matchesMixed(text, query)) return true

        return false
    }

    private fun matchesChosung(text: String, query: String): Boolean {
        val initials = HangulUtils.extractInitials(text)
        val isChosungQuery = query.all { it in 'ㄱ'..'ㅎ' }

        if (isChosungQuery) {
            return initials.contains(query)
        }

        return false
    }

    private fun matchesMixed(text: String, query: String): Boolean {
        if (query.isEmpty() || text.isEmpty()) return false

        var textIndex = 0
        var queryIndex = 0

        while (textIndex < text.length && queryIndex < query.length) {
            val textChar = text[textIndex]
            val queryChar = query[queryIndex]

            when {
                queryChar in 'ㄱ'..'ㅎ' -> {
                    val textInitial = HangulUtils.getInitialFromHangul(textChar)
                    if (textInitial == queryChar) {
                        queryIndex++
                        textIndex++
                    } else {
                        textIndex++
                    }
                }
                else -> {
                    if (textChar.lowercase() == queryChar.lowercase()) {
                        queryIndex++
                        textIndex++
                    } else {
                        textIndex++
                    }
                }
            }
        }

        return queryIndex == query.length
    }
}