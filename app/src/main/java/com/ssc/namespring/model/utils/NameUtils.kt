// model/utils/NameUtils.kt
package com.ssc.namespring.model.utils

import com.ssc.namespring.model.constants.Constants
import java.text.Normalizer

object NameUtils {

    fun normalize(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFC)
    }

    fun getHangulElement(char: Char?): String? {
        if (char == null) return null
        val cho = (char.code - Constants.HANGUL_BASE.code) / Constants.HANGUL_CHO_DIVISOR
        return Constants.FIVE_ELEMENTS[Constants.HANGUL_CHO_LIST[cho]]
    }

    fun getHangulPn(char: Char): Int {
        val jung = (char.code - Constants.HANGUL_BASE.code) / Constants.HANGUL_JUNG_DIVISOR % Constants.HANGUL_JUNG_COUNT
        return if (Constants.HANGUL_JUNG_LIST[jung] in Constants.YANG_VOWELS) 1 else 0
    }

    fun getHangulStrokeCount(char: Char): Int {
        val charCode = char.code - Constants.HANGUL_CODE_OFFSET
        val finaleOffset = charCode % Constants.HANGUL_JUNG_DIVISOR
        val medialOffset = (charCode / Constants.HANGUL_JUNG_DIVISOR) % Constants.HANGUL_JUNG_COUNT
        val initialOffset = charCode / (Constants.HANGUL_JUNG_DIVISOR * Constants.HANGUL_JUNG_COUNT)

        return (Constants.HANGUL_STROKES[Constants.HANGUL_CHO_LIST[initialOffset].toString()] ?: 0) +
                (Constants.HANGUL_STROKES[Constants.HANGUL_JUNG_LIST[medialOffset].toString()] ?: 0) +
                (Constants.HANGUL_STROKES[Constants.HANGUL_JONG_LIST[finaleOffset]] ?: 0)
    }

    fun isHarmoniousElementCombination(elementCombination: String): Pair<Boolean, List<Map<String, Any>>> {
        val elements = Constants.ELEMENTS
        var scoreCoexist = 0
        val elementHarmonyDetails = mutableListOf<Map<String, Any>>()

        for (i in 1 until elementCombination.length) {
            val prevElement = elements.indexOf(elementCombination[i - 1].toString())
            val currElement = elements.indexOf(elementCombination[i].toString())
            val diff = (currElement - prevElement + Constants.ELEMENT_COUNT) % Constants.ELEMENT_COUNT

            if (diff == Constants.ELEMENT_HARMONY_DIFF_1 || diff == Constants.ELEMENT_HARMONY_DIFF_2) {
                scoreCoexist++
                elementHarmonyDetails.add(mapOf(
                    "position" to "${i-1}-$i",
                    "elements" to "${elementCombination[i-1]}-${elementCombination[i]}",
                    "relation" to "harmonious",
                    "diff" to diff
                ))
            } else if (diff == Constants.ELEMENT_CONFLICT_DIFF_1 || diff == Constants.ELEMENT_CONFLICT_DIFF_2) {
                elementHarmonyDetails.add(mapOf(
                    "position" to "${i-1}-$i",
                    "elements" to "${elementCombination[i-1]}-${elementCombination[i]}",
                    "relation" to "conflicting",
                    "diff" to diff
                ))
                return false to elementHarmonyDetails
            }
        }

        return (scoreCoexist > 0) to elementHarmonyDetails
    }
}