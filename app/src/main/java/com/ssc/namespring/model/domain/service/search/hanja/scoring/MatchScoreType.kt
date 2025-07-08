// model/domain/service/search/hanja/scoring/MatchScoreType.kt
package com.ssc.namespring.model.domain.service.search.hanja.scoring

enum class MatchScoreType(val baseScore: Float) {
    EXACT_MATCH(10f),
    EXACT_MATCH_NO_SPACE(9f),
    CONTAINS(8f),
    CONTAINS_NO_SPACE(7f),
    CHOSUNG_WITH_SPACE(6f),
    CHOSUNG_NO_SPACE(5f),
    MIXED_PATTERN(4f),
    MIXED_PATTERN_NO_SPACE(3f),
    WORD_MATCH(2f),
    CHAR_MATCH(1f)
}