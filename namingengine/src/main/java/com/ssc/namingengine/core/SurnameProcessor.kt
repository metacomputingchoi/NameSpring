// core/SurnameProcessor.kt
package com.ssc.namingengine.core

import com.ssc.namingengine.common.parsing.ParsingConstants
import com.ssc.namingengine.service.NameParser
import com.ssc.namingengine.service.SurnameValidator
import com.ssc.namingengine.util.logger.Logger

internal class SurnameProcessor(
    private val nameParser: NameParser,
    private val surnameValidator: SurnameValidator,
    private val logger: Logger
) {
    data class SurnameCandidate(
        val surnameParts: List<Pair<String, String>>,
        val nameParts: List<Pair<String, String>>,
        val surHangul: String,
        val surHanja: String
    )

    fun findSurnameCandidates(
        parsed: List<Pair<String, String>>
    ): List<SurnameCandidate> {
        return (1..minOf(2, parsed.size)).mapNotNull { i ->
            val surnameParts = parsed.take(i)
            val nameParts = parsed.drop(i)

            val validation = surnameValidator.validateSurname(surnameParts)
            if (validation.first) {
                SurnameCandidate(
                    surnameParts = surnameParts,
                    nameParts = nameParts,
                    surHangul = validation.second!!,
                    surHanja = validation.third!!
                )
            } else null
        }
    }

    fun validateNameLength(
        candidate: SurnameCandidate,
        verbose: Boolean
    ): Boolean {
        if (!nameParser.validateNameLengthConstraint(candidate.nameParts)) {
            if (verbose) {
                logger.v("${ParsingConstants.ErrorMessages.NAME_LENGTH_CONSTRAINT}${candidate.surHangul}")
            }
            return false
        }
        return true
    }
}