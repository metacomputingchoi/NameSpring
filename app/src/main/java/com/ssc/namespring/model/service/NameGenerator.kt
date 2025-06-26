// model/service/NameGenerator.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.common.hangul.HangulConstants
import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.repository.HanjaRepository
import com.ssc.namespring.model.util.toHangulDecomposition

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
            }.filterNotNull().forEach { yield(it) }  // filterNotNull 추가
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

        // 성씨의 획수 계산
        val surHanjaHoeksu = surHanja.map {
            hanjaHoeksuAnalyzer.getHanjaHoeksu(it.toString()) ?: 0
        }

        return sequence {
            // 각 위치별로 가능한 한자 목록 생성
            val candidatesByPosition = mutableListOf<List<HanjaInfo>>()

            for ((index, constraint) in nameConstraints.withIndex()) {
                val candidates = when {
                    // 한자가 고정된 경우
                    constraint.hanjaType == ParsingConstants.ConstraintTypes.COMPLETE &&
                            constraint.hanjaValue != null && constraint.hanjaValue != "_" -> {
                        val hanjaInfo = hanjaRepository.findByHanja(constraint.hanjaValue)
                        if (hanjaInfo != null) {
                            listOf(hanjaInfo)
                        } else {
                            return@sequence // 한자를 찾을 수 없으면 종료
                        }
                    }
                    // 한자가 고정되지 않은 경우 - 모든 가능한 획수의 한자 수집
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
                    return@sequence // 후보가 없으면 종료
                }
                candidatesByPosition.add(candidates)
            }

            // 모든 조합 생성
            generateCombinationsRecursive(candidatesByPosition, 0, mutableListOf()) { combination ->
                // 전체 획수 배열 생성
                val allHoeksu = surHanjaHoeksu + combination.map { it.wonHoeksu }

                // 사격 계산
                val sagyeok = calculateSagyeok(allHoeksu, surLength)
                val score = sagyeok.getValues().count { it in NamingCalculationConstants.GILHAN_HOEKSU }

                // 평가 모드가 아닐 때만 최소 점수 체크
                if (requireMinScore) {
                    val isComplexSurnameSingleName = surLength >= 2 && nameLength == 1
                    val minScore = getMinScore(isComplexSurnameSingleName, surLength, nameLength)
                    if (score < minScore) {
                        return@generateCombinationsRecursive null
                    }
                }

                // 음양/오행 체크 (평가 모드에서는 건너뜀)
                if (requireMinScore) {
                    val nameBaleumEumyang = allHoeksu.map { it % NamingCalculationConstants.YIN_YANG_MODULO }
                    val isComplexSurnameSingleName = surLength >= 2 && nameLength == 1

                    if (!isComplexSurnameSingleName &&
                        (nameBaleumEumyang.sum() == 0 || nameBaleumEumyang.sum() == nameBaleumEumyang.size)) {
                        return@generateCombinationsRecursive null
                    }

                    val nameHoeksuOhaeng = allHoeksu.map { sv ->
                        val ne = (sv % NamingCalculationConstants.STROKE_MODULO) +
                                (sv % NamingCalculationConstants.STROKE_MODULO) % NamingCalculationConstants.YIN_YANG_MODULO
                        if (ne == NamingCalculationConstants.STROKE_MODULO) 0 else ne
                    }

                    if (!multiOhaengHarmonyAnalyzer.checkHoeksuOhaengHarmony(nameHoeksuOhaeng, isComplexSurnameSingleName)) {
                        return@generateCombinationsRecursive null
                    }

                    val sagyeokSuriOhaeng = sagyeok.getValues().map { ft ->
                        val te = (ft % NamingCalculationConstants.STROKE_MODULO) +
                                (ft % NamingCalculationConstants.STROKE_MODULO) % NamingCalculationConstants.YIN_YANG_MODULO
                        if (te == NamingCalculationConstants.STROKE_MODULO) 0 else te
                    }

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

    // 사격 계산 메서드 추가 (NameSuriAnalyzer와 동일한 로직)
    private fun calculateSagyeok(hanjaHoeksuValues: List<Int>, surLength: Int): Sagyeok {
        val hyeong = hanjaHoeksuValues.subList(surLength, hanjaHoeksuValues.size).sum()
        val won = hanjaHoeksuValues[surLength - 1] + hanjaHoeksuValues[surLength]
        val i = hanjaHoeksuValues.first() + hanjaHoeksuValues.last()
        val jeong = hanjaHoeksuValues.sum() % NamingCalculationConstants.JEONG_MODULO

        return Sagyeok(hyeong, won, i, jeong)
    }

    // 최소 점수 계산 메서드 추가
    private fun getMinScore(
        isComplexSurnameSingleName: Boolean,
        surLength: Int,
        nameLength: Int
    ): Int {
        return when {
            isComplexSurnameSingleName -> NamingCalculationConstants.MinScore.COMPLEX_SURNAME_SINGLE_NAME
            surLength == 1 && nameLength == 1 -> NamingCalculationConstants.MinScore.SINGLE_SURNAME_SINGLE_NAME
            else -> NamingCalculationConstants.MinScore.DEFAULT
        }
    }

    private fun matchesConstraint(hanja: HanjaInfo, constraint: NameConstraint): Boolean {
        // 한자 제약 확인
        if (constraint.hanjaType == ParsingConstants.ConstraintTypes.COMPLETE &&
            hanja.hanja != constraint.hanjaValue) {
            return false
        }

        // 한글 제약 확인
        return when (constraint.hangulType) {
            ParsingConstants.ConstraintTypes.COMPLETE -> hanja.inmyongSound == constraint.hangulValue
            ParsingConstants.ConstraintTypes.INITIAL -> {
                hanja.inmyongSound.isNotEmpty() &&
                        getInitialFromHangul(hanja.inmyongSound[0]) == constraint.hangulValue?.get(0)
            }
            else -> true
        }
    }

    private fun getInitialFromHangul(char: Char): Char? {
        return if (char in HangulConstants.HANGUL_START..HangulConstants.HANGUL_END) {
            val (cho, _, _) = char.toHangulDecomposition()
            HangulConstants.INITIALS[cho]
        } else null
    }
}