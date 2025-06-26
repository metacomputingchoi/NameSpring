// model/common/naming/NamingCalculationConstants.kt
package com.ssc.namespring.model.common.naming

object NamingCalculationConstants {
    // 길한 획수
    val GILHAN_HOEKSU = setOf(
        1, 3, 5, 6, 7, 8, 11, 13, 15, 16, 17, 18, 21, 23, 24, 25,
        29, 31, 32, 33, 35, 37, 38, 39, 41, 45, 47, 48, 52, 57,
        61, 63, 65, 67, 68, 81
    )

    // 사격 계산 상수
    const val JEONG_MODULO = 81
    const val STROKE_MODULO = 10
    const val YIN_YANG_MODULO = 2
    const val OHAENG_COUNT = 5  // 오행 개수 상수 추가

    // 최소 점수 기준
    object MinScore {
        const val COMPLEX_SURNAME_SINGLE_NAME = 2
        const val SINGLE_SURNAME_SINGLE_NAME = 3
        const val DEFAULT = 4
    }

    // 이름 길이 제약
    const val MAX_EMPTY_SLOTS = 2

    // 획수 범위
    const val MIN_STROKE = 1
    const val MAX_STROKE = 27

    // 성명 길이 조합
    object NameLengthCombinations {
        val SINGLE_SINGLE = 1 to 1      // 성 1자, 이름 1자
        val SINGLE_DOUBLE = 1 to 2      // 성 1자, 이름 2자
        val SINGLE_TRIPLE = 1 to 3      // 성 1자, 이름 3자
        val SINGLE_QUAD = 1 to 4        // 성 1자, 이름 4자
        val DOUBLE_SINGLE = 2 to 1      // 성 2자, 이름 1자
        val DOUBLE_DOUBLE = 2 to 2      // 성 2자, 이름 2자
        val DOUBLE_TRIPLE = 2 to 3      // 성 2자, 이름 3자
        val DOUBLE_QUAD = 2 to 4        // 성 2자, 이름 4자
    }

    // 음양 균형 체크 관련
    object YinYangBalance {
        const val MIN_VARIETY = 2       // 최소 음양 다양성
        const val MAX_CONSECUTIVE_SINGLE = 1
        const val MAX_CONSECUTIVE_DOUBLE = 2
        const val MAX_CONSECUTIVE_TRIPLE = 3
    }

    // 자원오행 체크 관련
    object JawonCheck {
        object DoubleChar {
            const val ZERO_SINGLE_SIZE = 1
            const val ZERO_MULTIPLE_SIZE = 2
            const val ONE_SINGLE_SIZE = 1
            const val ONE_MULTIPLE_SIZE = 2
            const val EXPECTED_DIFFERENT_COUNT = 2
        }

        object TripleChar {
            const val ZERO_SINGLE_SIZE = 1
            const val ZERO_DOUBLE_SIZE = 2
            const val ZERO_MULTIPLE_SIZE = 3
            const val TRIPLE_SUM = 3
            const val MIN_COUNT_PER_ELEMENT = 1
            const val EXPECTED_UNIQUE_COUNT = 3
        }

        object QuadChar {
            const val ZERO_SINGLE_SIZE = 1
            const val ZERO_DOUBLE_SIZE = 2
            const val ZERO_TRIPLE_SIZE = 3
            const val ZERO_MULTIPLE_SIZE = 4
            const val PAIR_COUNT = 2
            const val SINGLE_COUNT = 1
            const val EXPECTED_PAIRS_FOR_DOUBLE = 2
            const val EXPECTED_SINGLE_COUNT = 2
            const val EXPECTED_UNIQUE_COUNT = 4
        }
    }

    // 오행 조화 점수
    object OhaengHarmonyScores {
        const val CONFLICTING_FORWARD_DIFF = 4
        const val CONFLICTING_BACKWARD_DIFF = -6
        const val GENERATING_FORWARD_DIFF = 2
        const val GENERATING_BACKWARD_DIFF = -8
    }

    // 사격 계산
    object FourPillarAnalysis {
        const val FOUR_TYPES_COUNT = 4
        const val START_INDEX = 1
        const val PREVIOUS_INDEX_OFFSET = 1
        const val MIN_HARMONY_SCORE = 0
        const val MIN_REQUIRED_SCORE = 1
    }

    // 점수 계산 상수
    object ScoreConstants {
        const val HARMONY_GENERATING_SCORE = 10
        const val HARMONY_CONFLICTING_PENALTY = 15
        const val SAGYEOK_SCORE_MULTIPLIER = 25
        const val YIN_YANG_BALANCE_SCORE = 20
        const val HOEKSU_GILHAN_SCORE = 10
    }
}