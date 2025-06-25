// model/service/NameGenerator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.repository.HanjaRepository
import com.ssc.namespring.model.util.cartesianProduct
import com.ssc.namespring.model.util.toHangulDecomposition

class NameGenerator(
    private val hanjaRepository: HanjaRepository,
    private val nameSuriAnalyzer: NameSuriAnalyzer
) {

    fun generateNames(
        surHangul: String,
        surHanja: String,
        nameConstraints: List<NameConstraint>,
        nameLength: Int,
        sajuOhaengCount: Map<String, Int>
    ): List<GeneratedName> {
        return if (nameConstraints.all { it.isAllEmpty() }) {
            generateWithoutConstraints(surHangul, surHanja, nameLength)
        } else {
            generateNamesWithConstraints(surHangul, surHanja, nameConstraints, sajuOhaengCount)
        }
    }

    private fun generateWithoutConstraints(
        surHangul: String,
        surHanja: String,
        nameLength: Int
    ): List<GeneratedName> {
        val goodCombinations = nameSuriAnalyzer.analyzeNameCombinations(
            surHangul, surHanja, nameLength
        )
        val surLength = surHanja.length

        return goodCombinations.flatMap { gc ->
            val nameHanjaHoeksu = gc.nameHanjaHoeksu.subList(surLength, gc.nameHanjaHoeksu.size)
            val candidateLists = nameHanjaHoeksu.map { hoeksu ->
                hanjaRepository.hanjaByHoeksu[hoeksu] ?: emptyList()
            }

            cartesianProduct(candidateLists).map { combination ->
                GeneratedName(
                    surnameHangul = surHangul,
                    surnameHanja = surHanja,
                    combinedHanja = combination.joinToString("") { it.hanja },
                    combinedPronounciation = combination.joinToString("") { it.inmyongSound },
                    sagyeok = gc.sagyeok,
                    nameHanjaHoeksu = gc.nameHanjaHoeksu,
                    hanjaDetails = combination
                )
            }
        }
    }

    private fun generateNamesWithConstraints(
        surHangul: String,
        surHanja: String,
        nameConstraints: List<NameConstraint>,
        sajuOhaengCount: Map<String, Int>
    ): List<GeneratedName> {
        val surLength = surHanja.length
        val nameLength = nameConstraints.size

        val goodCombinations = nameSuriAnalyzer.analyzeNameCombinations(
            surHangul, surHanja, nameLength
        )

        return goodCombinations.flatMap { gc ->
            val nameHanjaHoeksu = gc.nameHanjaHoeksu.subList(surLength, gc.nameHanjaHoeksu.size)

            val hanjaCandidatesList = nameHanjaHoeksu.mapIndexedNotNull { i, hoeksuCount ->
                val candidates = hanjaRepository.hanjaByHoeksu[hoeksuCount] ?: emptyList()
                val constraint = nameConstraints[i]

                candidates.filter { cand ->
                    matchesConstraint(cand, constraint)
                }.takeIf { it.isNotEmpty() }
            }

            if (hanjaCandidatesList.size == nameHanjaHoeksu.size) {
                cartesianProduct(hanjaCandidatesList).map { combination ->
                    GeneratedName(
                        surnameHangul = surHangul,
                        surnameHanja = surHanja,
                        combinedHanja = combination.joinToString("") { it.hanja },
                        combinedPronounciation = combination.joinToString("") { it.inmyongSound },
                        sagyeok = gc.sagyeok,
                        nameHanjaHoeksu = gc.nameHanjaHoeksu,
                        hanjaDetails = combination
                    )
                }
            } else emptyList()
        }
    }

    private fun matchesConstraint(hanja: HanjaInfo, constraint: NameConstraint): Boolean {
        // 한자 제약 확인
        if (constraint.hanjaType == Constants.ConstraintTypes.COMPLETE &&
            hanja.hanja != constraint.hanjaValue) {
            return false
        }

        // 한글 제약 확인
        return when (constraint.hangulType) {
            Constants.ConstraintTypes.COMPLETE -> hanja.inmyongSound == constraint.hangulValue
            Constants.ConstraintTypes.INITIAL -> {
                hanja.inmyongSound.isNotEmpty() &&
                        getInitialFromHangul(hanja.inmyongSound[0]) == constraint.hangulValue?.get(0)
            }
            else -> true
        }
    }

    private fun getInitialFromHangul(char: Char): Char? {
        return if (char in Constants.HANGUL_START..Constants.HANGUL_END) {
            val (cho, _, _) = char.toHangulDecomposition()
            Constants.INITIALS[cho]
        } else null
    }
}
