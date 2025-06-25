// model/service/NameParser.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.data.NameConstraint
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.util.normalizeNFC
import com.ssc.namespring.model.util.toHangulDecomposition

class NameParser {

    fun parseNameInput(input: String): List<Pair<String, String>> {
        val normalizedInput = input.normalizeNFC()
        val pattern = Constants.NAME_PATTERN.toRegex()

        return pattern.findAll(normalizedInput).map { match ->
            val (hangul, hanja) = match.destructured
            hangul.normalizeNFC() to hanja.normalizeNFC()
        }.toList().also {
            if (it.isEmpty()) {
                throw NamingException.InvalidInputException(Constants.ErrorMessages.INVALID_INPUT_FORMAT)
            }
        }
    }

    fun extractConstraintsFromInput(nameParts: List<Pair<String, String>>): List<NameConstraint> {
        return nameParts.map { (hangul, hanja) ->
            val hangulType = when {
                hangul == Constants.INPUT_SEPARATOR -> Constants.ConstraintTypes.EMPTY
                hangul.length == 1 && hangul[0] in Constants.INITIALS -> Constants.ConstraintTypes.INITIAL
                hangul.length == 1 && hangul[0] in Constants.HANGUL_START..Constants.HANGUL_END -> Constants.ConstraintTypes.COMPLETE
                else -> throw NamingException.InvalidInputException(Constants.ErrorMessages.INVALID_HANGUL + hangul)
            }

            val hanjaType = if (hanja == Constants.INPUT_SEPARATOR) {
                Constants.ConstraintTypes.EMPTY
            } else {
                Constants.ConstraintTypes.COMPLETE
            }

            NameConstraint(
                hangulType = hangulType,
                hangulValue = if (hangul == Constants.INPUT_SEPARATOR) null else hangul,
                hanjaType = hanjaType,
                hanjaValue = if (hanja == Constants.INPUT_SEPARATOR) null else hanja
            )
        }
    }

    fun validateNameLengthConstraint(nameParts: List<Pair<String, String>>): Boolean {
        val totalLength = nameParts.size
        val emptyCount = nameParts.count { (hangul, hanja) ->
            hangul == Constants.INPUT_SEPARATOR && hanja == Constants.INPUT_SEPARATOR
        }
        val filledCount = totalLength - emptyCount

        return if (totalLength <= Constants.MAX_EMPTY_SLOTS) {
            true
        } else {
            val requiredFilled = totalLength - Constants.MAX_EMPTY_SLOTS
            filledCount >= requiredFilled
        }
    }

    fun getInitialFromHangul(char: Char): Char? {
        return if (char in Constants.HANGUL_START..Constants.HANGUL_END) {
            val (cho, _, _) = char.toHangulDecomposition()
            Constants.INITIALS[cho]
        } else null
    }
}