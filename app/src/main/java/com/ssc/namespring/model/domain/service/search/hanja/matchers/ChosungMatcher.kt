// model/domain/service/search/hanja/matchers/ChosungMatcher.kt
package com.ssc.namespring.model.domain.service.search.hanja.matchers

import com.ssc.namespring.model.common.utils.MixedPatternUtils
import com.ssc.namespring.model.domain.service.search.hanja.HanjaSearchUtils
import com.ssc.namespring.model.domain.service.search.hanja.scoring.MatchScoreType

class ChosungMatcher {
    fun getChosungScore(text: String, query: String): Float? {
        // 초성 검색 (띄어쓰기 포함)
        if (query.matches(Regex("^[ㄱ-ㅎ\\s]+$"))) {
            if (matchesChosungWithSpace(text, query)) {
                return MatchScoreType.CHOSUNG_WITH_SPACE.baseScore
            }
        }

        // 초성 검색 (띄어쓰기 무시)
        val queryNoSpace = query.replace(" ", "")
        if (queryNoSpace.matches(Regex("^[ㄱ-ㅎ]+$"))) {
            val textNoSpace = text.replace(" ", "")
            if (MixedPatternUtils.matchChosungPattern(textNoSpace, queryNoSpace)) {
                return MatchScoreType.CHOSUNG_NO_SPACE.baseScore
            }
        }

        return null
    }

    private fun matchesChosungWithSpace(text: String, chosungQuery: String): Boolean {
        val textChosung = text.map { char ->
            when {
                char == ' ' -> ' '
                char.toString().matches(Regex("[가-힣]")) -> {
                    HanjaSearchUtils.getChosung(char)
                }
                else -> ""
            }
        }.joinToString("")

        return textChosung.contains(chosungQuery)
    }
}