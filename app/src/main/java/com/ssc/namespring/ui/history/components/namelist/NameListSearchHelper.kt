// ui/history/components/namelist/NameListSearchHelper.kt
package com.ssc.namespring.ui.history.components.namelist

import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.ui.history.components.namelist.validators.KoreanValidator
import com.ssc.namespring.ui.history.components.namelist.utils.KoreanDecomposer

class NameListSearchHelper(
    private val enableFuzzySearch: Boolean = false
) {
    private val smartMatcher = SmartKoreanMatcher(enableFuzzySearch)
    private val koreanValidator = KoreanValidator()
    private val koreanDecomposer = KoreanDecomposer()

    fun filterNames(names: List<GeneratedName>, query: String): List<GeneratedName> {
        if (query.isBlank()) return names

        val trimmedQuery = query.trim()

        if (koreanValidator.isInvalidKoreanComposition(trimmedQuery)) {
            return emptyList()
        }

        return names.filter { generatedName ->
            val fullName = "${generatedName.surnameHangul}${generatedName.combinedPronounciation}"
            smartMatch(fullName, trimmedQuery)
        }.sortedByDescending { generatedName ->
            val fullName = "${generatedName.surnameHangul}${generatedName.combinedPronounciation}"
            smartMatcher.calculateRelevanceScore(fullName, trimmedQuery)
        }
    }

    private fun smartMatch(name: String, query: String): Boolean {
        // 정확한 매칭
        if (name.contains(query)) return true

        // 불완전한 한글 처리
        val decomposedQuery = koreanDecomposer.decomposeIncompleteKorean(query)
        if (decomposedQuery != query && smartMatcher.matchesDecomposed(name, decomposedQuery)) return true

        // 공백 제거 매칭
        val queryNoSpace = query.replace(" ", "")
        if (queryNoSpace != query && name.contains(queryNoSpace)) return true

        // 초성 검색
        if (smartMatcher.matchesChosung(name, query)) return true

        // 혼합 패턴 검색
        if (smartMatcher.matchesMixedPattern(name, query)) return true

        // 자모 분리 검색
        if (smartMatcher.matchesJamoPattern(name, query)) return true

        // 편집 거리 기반 유사도 검색 (오타 허용)
        if (enableFuzzySearch && smartMatcher.isSimilarEnough(name, query)) return true

        return false
    }
}