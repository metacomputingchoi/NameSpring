// model/filter/constants/ValidationConstants.kt
package com.ssc.namespring.model.filter.constants

object ValidationConstants {

    object EumYang {
        const val MAX_CONSECUTIVE_SINGLE = 3
        const val MAX_CONSECUTIVE_DOUBLE = 2
        const val MAX_CONSECUTIVE_TRIPLE = 3
    }

    object JawonOhaeng {
        const val PAIR_COUNT = 2
        const val SINGLE_COUNT = 1

        object SingleChar {
            const val ELEMENT_COUNT = 1
        }

        object DoubleChar {
            const val ELEMENT_COUNT = 2
        }

        object TripleChar {
            const val ELEMENT_COUNT = 3
            const val MIN_COUNT_PER_ELEMENT = 1
        }

        object QuadChar {
            const val ELEMENT_COUNT = 4
            const val EXPECTED_UNIQUE_COUNT = 4
            const val EXPECTED_SINGLE_COUNT_FOR_TRIPLE = 2
        }
    }

    object Messages {
        const val BALANCED_SAJU = "사주 오행이 균형잡혀 있음"
        const val EUM_YANG_BALANCED = "음양 균형 양호"
        const val EUM_YANG_UNBALANCED = "음양 개수 불균형"
        const val EUM_YANG_CONSECUTIVE = "연속 음양이 너무 많음"
        const val FIRST_LAST_DIFFERENT = "처음과 끝의 음양이 다름"
        const val FIRST_LAST_SAME = "처음과 끝의 음양이 같음"
        const val NO_JAWON_ELEMENT = "자원 오행이 없음"
    }
}