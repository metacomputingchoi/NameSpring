// model/service/NameGenerator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.repository.HanjaRepository
import com.ssc.namespring.model.util.NamingCalculationUtils
import com.ssc.namespring.model.util.HangulUtils

class NameGenerator(
    private val hanjaRepository: HanjaRepository,
    private val nameSuriAnalyzer: NameSuriAnalyzer,
    private val hanjaHoeksuAnalyzer: HanjaHoeksuAnalyzer,
    private val multiOhaengHarmonyAnalyzer: MultiOhaengHarmonyAnalyzer
) {

    fun generateNames(
        surHangul: String,
        surHanja: String,
        nameConstraints: List<NameConstraint>,
        nameLength: Int,
        sajuOhaengCount: Map<String, Int>,
        requireMinScore: Boolean = true
    ): Sequence<GeneratedName> {
        return if (nameConstraints.all { it.isAllEmpty() }) {
            generateWithoutConstraints(surHangul, surHanja, nameLength, requireMinScore)
        } else {
            generateNamesWithConstraints(surHangul, surHanja, nameConstraints, sajuOhaengCount, requireMinScore)
        }
    }

    private fun generateWithoutConstraints(
        surHangul: String,
        surHanja: String,
        nameLength: Int,
        requireMinScore: Boolean = true
    ): Sequence<GeneratedName> {
        val goodCombinations = nameSuriAnalyzer.analyzeNameCombinations(
            surHangul, surHanja, nameLength, requireMinScore
        )
        val surLength = surHanja.length

        return sequence {
            goodCombinations.forEach { gc ->
                val nameHanjaHoeksu = gc.nameHanjaHoeksu.subList(surLength, gc.nameHanjaHoeksu.size)
                generateNamesFromHoeksu(
                    surHangul, surHanja, nameHanjaHoeksu, gc
                ).forEach { yield(it) }
            }
        }
    }

    private fun generateNamesFromHoeksu(
        surHangul: String,
        surHanja: String,
        nameHanjaHoeksu: List<Int>,
        goodCombination: GoodCombination
    ): Sequence<GeneratedName> {
        return sequence {
            val candidateLists = nameHanjaHoeksu.map { hoeksu ->
                hanjaRepository.hanjaByHoeksu[hoeksu] ?: emptyList()
            }

            generateCombinationsRecursive(candidateLists, 0, mutableListOf()) { combination ->
                GeneratedName(
                    surnameHangul = surHangul,
                    surnameHanja = surHanja,
                    combinedHanja = combination.joinToString("") { it.hanja },
                    combinedPronounciation = combination.joinToString("") { it.inmyongSound },
                    sagyeok = goodCombination.sagyeok,
                    nameHanjaHoeksu = goodCombination.nameHanjaHoeksu,
                    hanjaDetails = combination
                )
            }.filterNotNull().forEach { yield(it) }
        }
    }

    private fun generateCombinationsRecursive(
        candidateLists: List<List<HanjaInfo>>,
        index: Int,
        current: MutableList<HanjaInfo>,
        createName: (List<HanjaInfo>) -> GeneratedName?
    ): Sequence<GeneratedName?> {
        return sequence {
            if (index == candidateLists.size) {
                yield(createName(current.toList()))
            } else {
                candidateLists[index].forEach { candidate ->
                    current.add(candidate)
                    generateCombinationsRecursive(candidateLists, index + 1, current, createName)
                        .forEach { yield(it) }
                    current.removeAt(current.size - 1)
                }
            }
        }
    }

    private fun generateNamesWithConstraints(
        surHangul: String,
        surHanja: String,
        nameConstraints: List<NameConstraint>,
        sajuOhaengCount: Map<String, Int>,
        requireMinScore: Boolean = true
    ): Sequence<GeneratedName> {
        val surLength = surHanja.length
        val nameLength = nameConstraints.size

        val surHanjaHoeksu = surHanja.map {
            hanjaHoeksuAnalyzer.getHanjaHoeksu(it.toString()) ?: 0
        }

        return sequence {
            val candidatesByPosition = mutableListOf<List<HanjaInfo>>()

            for ((index, constraint) in nameConstraints.withIndex()) {
                val candidates = when {
                    constraint.hanjaType == ParsingConstants.ConstraintTypes.COMPLETE &&
                            constraint.hanjaValue != null && constraint.hanjaValue != "_" -> {
                        val hanjaInfo = hanjaRepository.findByHanja(constraint.hanjaValue)
                        if (hanjaInfo != null) {
                            listOf(hanjaInfo)
                        } else {
                            return@sequence
                        }
                    }
                    else -> {
                        val allCandidates = mutableListOf<HanjaInfo>()
                        for (hoeksu in NamingCalculationConstants.MIN_STROKE..NamingCalculationConstants.MAX_STROKE) {
                            val candidates = hanjaRepository.hanjaByHoeksu[hoeksu] ?: emptyList()
                            allCandidates.addAll(
                                candidates.filter { cand ->
                                    matchesConstraint(cand, constraint)
                                }
                            )
                        }
                        allCandidates
                    }
                }

                if (candidates.isEmpty()) {
                    return@sequence
                }
                candidatesByPosition.add(candidates)
            }

            generateCombinationsRecursive(candidatesByPosition, 0, mutableListOf()) { combination ->
                val allHoeksu = surHanjaHoeksu + combination.map { it.wonHoeksu }

                val sagyeok = NamingCalculationUtils.calculateSagyeok(allHoeksu, surLength)
                val score = sagyeok.getValues().count { it in NamingCalculationConstants.GILHAN_HOEKSU }

                if (requireMinScore) {
                    val isComplexSurnameSingleName = NamingCalculationUtils.isComplexSurnameSingleName(surLength, nameLength)
                    val minScore = NamingCalculationUtils.getMinScore(isComplexSurnameSingleName, surLength, nameLength)
                    if (score < minScore) {
                        return@generateCombinationsRecursive null
                    }
                }

                if (requireMinScore) {
                    val nameBaleumEumyang = allHoeksu.map { it % NamingCalculationConstants.YIN_YANG_MODULO }
                    val isComplexSurnameSingleName = NamingCalculationUtils.isComplexSurnameSingleName(surLength, nameLength)

                    if (!isComplexSurnameSingleName && NamingCalculationUtils.isYinYangUnbalanced(nameBaleumEumyang)) {
                        return@generateCombinationsRecursive null
                    }

                    val nameHoeksuOhaeng = NamingCalculationUtils.calculateHoeksuListToOhaeng(allHoeksu)

                    if (!multiOhaengHarmonyAnalyzer.checkHoeksuOhaengHarmony(nameHoeksuOhaeng, isComplexSurnameSingleName)) {
                        return@generateCombinationsRecursive null
                    }

                    val sagyeokSuriOhaeng = NamingCalculationUtils.calculateHoeksuListToOhaeng(sagyeok.getValues())

                    if (!multiOhaengHarmonyAnalyzer.checkSagyeokSuriOhaengHarmony(sagyeokSuriOhaeng, isComplexSurnameSingleName)) {
                        return@generateCombinationsRecursive null
                    }
                }

                GeneratedName(
                    surnameHangul = surHangul,
                    surnameHanja = surHanja,
                    combinedHanja = combination.joinToString("") { it.hanja },
                    combinedPronounciation = combination.joinToString("") { it.inmyongSound },
                    sagyeok = sagyeok,
                    nameHanjaHoeksu = allHoeksu,
                    hanjaDetails = combination
                )
            }.filterNotNull().forEach { yield(it) }
        }
    }

    private fun matchesConstraint(hanja: HanjaInfo, constraint: NameConstraint): Boolean {
        if (constraint.hanjaType == ParsingConstants.ConstraintTypes.COMPLETE &&
            hanja.hanja != constraint.hanjaValue) {
            return false
        }

        return when (constraint.hangulType) {
            ParsingConstants.ConstraintTypes.COMPLETE -> hanja.inmyongSound == constraint.hangulValue
            ParsingConstants.ConstraintTypes.INITIAL -> {
                hanja.inmyongSound.isNotEmpty() &&
                        HangulUtils.getInitialFromHangul(hanja.inmyongSound[0]) == constraint.hangulValue?.get(0)
            }
            else -> true
        }
    }
}