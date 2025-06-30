// service/NameParser.kt
package com.ssc.namingengine.service

import com.ssc.namingengine.common.parsing.ParsingConstants
import com.ssc.namingengine.common.hangul.HangulConstants
import com.ssc.namingengine.common.naming.NamingCalculationConstants
import com.ssc.namingengine.data.NameConstraint
import com.ssc.namingengine.exception.NamingException
import com.ssc.namingengine.util.normalizeNFC

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
}