// model/service/NameParser.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.common.hangul.HangulConstants
import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.NameConstraint
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.util.normalizeNFC
import com.ssc.namespring.model.util.toHangulDecomposition

class NameParser {

    fun parseNameInput(input: String): List<Pair<String, String>> {
        val normalizedInput = input.normalizeNFC()
        val pattern = ParsingConstants.NAME_PATTERN.toRegex()

        return pattern.findAll(normalizedInput).map { match ->
            val (hangul, hanja) = match.destructured
            hangul.normalizeNFC() to hanja.normalizeNFC()
        }.toList().also {
            if (it.isEmpty()) {
                throw NamingException.InvalidInputException(
                    ParsingConstants.ErrorMessages.INVALID_INPUT_FORMAT,
                    input = input
                )
            }
        }
    }

    fun extractConstraintsFromInput(nameParts: List<Pair<String, String>>): List<NameConstraint> {
        return nameParts.map { (hangul, hanja) ->
            val hangulType = when {
                hangul == ParsingConstants.INPUT_SEPARATOR -> ParsingConstants.ConstraintTypes.EMPTY
                hangul.length == 1 && hangul[0] in HangulConstants.INITIALS -> ParsingConstants.ConstraintTypes.INITIAL
                hangul.length == 1 && hangul[0] in HangulConstants.HANGUL_START..HangulConstants.HANGUL_END -> ParsingConstants.ConstraintTypes.COMPLETE
                else -> throw NamingException.InvalidInputException(
                    ParsingConstants.ErrorMessages.INVALID_HANGUL,
                    input = hangul
                )
            }

            val hanjaType = if (hanja == ParsingConstants.INPUT_SEPARATOR) {
                ParsingConstants.ConstraintTypes.EMPTY
            } else {
                ParsingConstants.ConstraintTypes.COMPLETE
            }

            NameConstraint(
                hangulType = hangulType,
                hangulValue = if (hangul == ParsingConstants.INPUT_SEPARATOR) null else hangul,
                hanjaType = hanjaType,
                hanjaValue = if (hanja == ParsingConstants.INPUT_SEPARATOR) null else hanja
            )
        }
    }

    fun validateNameLengthConstraint(nameParts: List<Pair<String, String>>): Boolean {
        val totalLength = nameParts.size
        val emptyCount = nameParts.count { (hangul, hanja) ->
            hangul == ParsingConstants.INPUT_SEPARATOR && hanja == ParsingConstants.INPUT_SEPARATOR
        }
        val filledCount = totalLength - emptyCount

        return if (totalLength <= NamingCalculationConstants.MAX_EMPTY_SLOTS) {
            true
        } else {
            val requiredFilled = totalLength - NamingCalculationConstants.MAX_EMPTY_SLOTS
            filledCount >= requiredFilled
        }
    }

    fun getInitialFromHangul(char: Char): Char? {
        return if (char in HangulConstants.HANGUL_START..HangulConstants.HANGUL_END) {
            val (cho, _, _) = char.toHangulDecomposition()
            HangulConstants.INITIALS[cho]
        } else null
    }
}
