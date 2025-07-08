// model/domain/service/search/hanja/matchers/MixedPatternMatcher.kt
package com.ssc.namespring.model.domain.service.search.hanja.matchers

import com.ssc.namespring.model.common.utils.MixedPatternUtils
import com.ssc.namespring.model.domain.service.search.hanja.scoring.MatchScoreType

class MixedPatternMatcher {
    fun getMixedPatternScore(text: String, query: String): Float? {
        if (!MixedPatternUtils.containsMixedPattern(query)) {
            return null
        }

        if (MixedPatternUtils.matchMixedPattern(text, query)) {
            return MatchScoreType.MIXED_PATTERN.baseScore
        }

        val textNoSpace = text.replace(" ", "")
        val queryNoSpace = query.replace(" ", "")
        if (MixedPatternUtils.matchMixedPattern(textNoSpace, queryNoSpace)) {
            return MatchScoreType.MIXED_PATTERN_NO_SPACE.baseScore
        }

        return null
    }
}