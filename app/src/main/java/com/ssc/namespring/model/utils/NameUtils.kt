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
        val cho = (char.code - '가'.code) / 588
        val choList = listOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
        return Constants.FIVE_ELEMENTS[choList[cho]]
    }

    fun getHangulPn(char: Char): Int {
        val jung = (char.code - '가'.code) / 28 % 21
        val jungList = listOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
        return if (jungList[jung] in listOf('ㅏ', 'ㅑ', 'ㅐ', 'ㅒ', 'ㅗ', 'ㅛ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅣ')) 1 else 0
    }

    fun getHangulStrokeCount(char: Char): Int {
        val charCode = char.code - 44032
        val finaleOffset = charCode % 28
        val medialOffset = (charCode / 28) % 21
        val initialOffset = charCode / (28 * 21)

        val initials = listOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
        val medials = listOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
        val finales = listOf("", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ")

        return (Constants.HANGUL_STROKES[initials[initialOffset].toString()] ?: 0) +
                (Constants.HANGUL_STROKES[medials[medialOffset].toString()] ?: 0) +
                (Constants.HANGUL_STROKES[finales[finaleOffset]] ?: 0)
    }

    fun isHarmoniousElementCombination(elementCombination: String): Pair<Boolean, List<Map<String, Any>>> {
        val elements = listOf("木", "火", "土", "金", "水")
        var scoreCoexist = 0
        val elementHarmonyDetails = mutableListOf<Map<String, Any>>()

        for (i in 1 until elementCombination.length) {
            val prevElement = elements.indexOf(elementCombination[i - 1].toString())
            val currElement = elements.indexOf(elementCombination[i].toString())
            val diff = (currElement - prevElement + 5) % 5

            if (diff == 1 || diff == 4) {
                scoreCoexist++
                elementHarmonyDetails.add(mapOf(
                    "position" to "${i-1}-$i",
                    "elements" to "${elementCombination[i-1]}-${elementCombination[i]}",
                    "relation" to "harmonious",
                    "diff" to diff
                ))
            } else if (diff == 2 || diff == 3) {
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
