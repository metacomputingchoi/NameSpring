// ui/history/components/namelist/validators/KoreanValidator.kt
package com.ssc.namespring.ui.history.components.namelist.validators

class KoreanValidator {
    fun isInvalidKoreanComposition(text: String): Boolean {
        val hasOnlyJamo = text.all { it in 'ㄱ'..'ㅣ' }
        if (hasOnlyJamo && text.length > 3) return true

        return text.any { char ->
            if (char.code in 0xAC00..0xD7A3) {
                val code = char.code - 0xAC00
                val jong = code % 28
                val jung = (code / 28) % 21
                val cho = code / (21 * 28)
                isInvalidCombination(cho, jung, jong)
            } else false
        }
    }

    private fun isInvalidCombination(cho: Int, jung: Int, jong: Int): Boolean {
        val invalidPatterns = setOf(
            Triple(0, 18, 27), Triple(2, 19, 26), Triple(5, 20, 25)
        )

        if (cho in listOf(1, 4, 8, 10, 13)) {
            if (jung in listOf(1, 3, 5, 7, 11, 12, 14, 15, 16, 17)) {
                return true
            }
        }

        return Triple(cho, jung, jong) in invalidPatterns
    }
}