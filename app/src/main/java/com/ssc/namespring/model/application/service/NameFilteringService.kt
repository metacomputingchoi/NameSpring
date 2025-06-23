// model/application/service/NameFilteringService.kt
package com.ssc.namespring.model.application.service

import com.ssc.namespring.model.domain.name.entity.*
import com.ssc.namespring.model.domain.saju.entity.BirthDateTime
import com.ssc.namespring.model.domain.saju.entity.Saju
import com.ssc.namespring.model.domain.element.entity.ElementBalance
import com.ssc.namespring.model.domain.hanja.entity.Hanja
import com.ssc.namespring.model.infrastructure.repository.HanjaRepository
import com.ssc.namespring.model.infrastructure.repository.NameRepository
import com.ssc.namespring.model.common.util.HangulUtils
import com.ssc.namespring.model.common.util.ElementUtils
import com.ssc.namespring.model.common.constants.FilterConstants

class NameFilteringService(
    private val hanjaRepository: HanjaRepository,
    private val nameRepository: NameRepository,
    private val nameAnalysisService: NameAnalysisService
) {
    fun filterNames(
        goodCombinations: List<Map<String, Any>>,
        surHangul: String,
        surHanja: String,
        name1Hangul: String?,
        name1Hanja: String?,
        name2Hangul: String?,
        name2Hanja: String?,
        fourJu: Saju,
        dictElementsCount: ElementBalance,
        zeroElements: List<String>,
        oneElements: List<String>,
        birthInfo: BirthDateTime
    ): List<Name> {
        val results = mutableListOf<Name>()
        val surHangulElement = HangulUtils.getHangulElement(surHangul[0])
        val surHangulPm = HangulUtils.getHangulPn(surHangul[0])

        for (comb in goodCombinations) {
            val analysisDetails = comb["analysis_details"] as Map<*, *>
            val stroke1 = comb["stroke1"] as Int
            val stroke2 = comb["stroke2"] as Int

            val hanja1List = findHanjaList(name1Hanja, name1Hangul, stroke1)
            val hanja2List = findHanjaList(name2Hanja, name2Hangul, stroke2)

            for (h1 in hanja1List) {
                for (h2 in hanja2List) {
                    if (h1.hanja.isEmpty() || h2.hanja.isEmpty() ||
                        h1.inmyeongYongEum.isNullOrEmpty() || h2.inmyeongYongEum.isNullOrEmpty()) {
                        continue
                    }

                    val result = Name(
                        surHangul = surHangul,
                        surHanja = surHanja,
                        surHangulElement = surHangulElement,
                        surHangulPm = surHangulPm,
                        birthInfo = birthInfo,
                        sajuInfo = fourJu,
                        dictElementsCount = dictElementsCount,
                        zeroElements = zeroElements,
                        oneElements = oneElements,
                        combinationAnalysis = nameAnalysisService.mapToCombinationAnalysis(analysisDetails),
                        hanja1Info = h1,
                        hanja2Info = h2,
                        filteringProcess = mutableListOf()
                    )

                    if (processFiltering(result, h1, h2, surHangul, surHangulElement, surHangulPm, zeroElements, oneElements)) {
                        results.add(result)
                    }
                }
            }
        }

        return results
    }

    private fun findHanjaList(hanjaChar: String?, hangul: String?, stroke: Int): List<Hanja> {
        return when {
            hanjaChar != null -> hanjaRepository.findByHanja(hanjaChar)
            hangul != null -> hanjaRepository.findByStrokeAndPronunciation(stroke, hangul)
            else -> hanjaRepository.findByStroke(stroke)
        }
    }

    private fun processFiltering(
        result: Name,
        h1: Hanja,
        h2: Hanja,
        surHangul: String,
        surHangulElement: String?,
        surHangulPm: Int,
        zeroElements: List<String>,
        oneElements: List<String>
    ): Boolean {
        val combinedHanja = h1.hanja + h2.hanja
        val combinedPronounciation = h1.inmyeongYongEum!! + h2.inmyeongYongEum!!

        // 길이 체크
        if (combinedPronounciation.length != FilterConstants.NAME_LENGTH || 
            combinedHanja.length != FilterConstants.NAME_LENGTH) {
            result.filteringProcess.add(FilteringStep(
                step = "length_check",
                passed = false,
                reason = "combined_pronounciation_length=${combinedPronounciation.length}, combined_hanja_length=${combinedHanja.length}"
            ))
            return false
        }

        result.filteringProcess.add(FilteringStep("length_check", true))

        // 원소 추출
        val elem1 = HangulUtils.getHangulElement(combinedPronounciation[0])
        val elem2 = HangulUtils.getHangulElement(combinedPronounciation[1])
        if (elem1 == null || elem2 == null) {
            result.filteringProcess.add(FilteringStep(
                step = "element_extraction",
                passed = false,
                reason = "elem1=$elem1, elem2=$elem2"
            ))
            return false
        }

        result.filteringProcess.add(FilteringStep(
            step = "element_extraction",
            passed = true,
            details = mapOf("elem1" to elem1, "elem2" to elem2)
        ))

        val combinedElement = surHangulElement + elem1 + elem2
        val combinedPm = "$surHangulPm${HangulUtils.getHangulPn(combinedPronounciation[0])}${HangulUtils.getHangulPn(combinedPronounciation[1])}"

        result.combinedElement = combinedElement
        result.combinedPm = combinedPm

        // 길이 체크 2
        if (combinedElement.length != FilterConstants.COMBINED_LENGTH || 
            combinedPm.length != FilterConstants.COMBINED_LENGTH) {
            result.filteringProcess.add(FilteringStep(
                step = "combined_length_check",
                passed = false,
                reason = "combined_element_length=${combinedElement.length}, combined_pm_length=${combinedPm.length}"
            ))
            return false
        }

        result.filteringProcess.add(FilteringStep("combined_length_check", true))

        // 음양 다양성 체크
        val pmSet = combinedPm.toSet()
        if (pmSet.size <= FilterConstants.MIN_PM_DIVERSITY) {
            result.filteringProcess.add(FilteringStep(
                step = "pm_diversity_check",
                passed = false,
                reason = "pm_set=$pmSet"
            ))
            return false
        }

        result.filteringProcess.add(FilteringStep(
            step = "pm_diversity_check",
            passed = true,
            details = mapOf("pm_set" to pmSet.toList())
        ))

        // 첫번째와 세번째 음양 체크
        if (combinedPm[FilterConstants.PM_FIRST_INDEX] == combinedPm[FilterConstants.PM_THIRD_INDEX]) {
            result.filteringProcess.add(FilteringStep(
                step = "pm_position_check",
                passed = false,
                reason = "pm[0]=${combinedPm[FilterConstants.PM_FIRST_INDEX]} == pm[2]=${combinedPm[FilterConstants.PM_THIRD_INDEX]}"
            ))
            return false
        }

        result.filteringProcess.add(FilteringStep("pm_position_check", true))

        // 원소 조화 체크
        val (isHarmonious, harmonyDetails) = ElementUtils.isHarmoniousElementCombination(combinedElement)
        if (!isHarmonious) {
            result.filteringProcess.add(FilteringStep(
                step = "element_harmony_check",
                passed = false,
                details = mapOf("harmony_details" to harmonyDetails)
            ))
            return false
        }

        result.filteringProcess.add(FilteringStep(
            step = "element_harmony_check",
            passed = true,
            details = mapOf("harmony_details" to harmonyDetails)
        ))

        // 자원오행 체크
        val jawon1 = ElementUtils.normalize(h1.jawonOheng ?: "")
        val jawon2 = ElementUtils.normalize(h2.jawonOheng ?: "")

        val jawonCheckResult = checkJawon(jawon1, jawon2, zeroElements, oneElements)

        result.filteringProcess.add(FilteringStep(
            step = "jawon_check",
            passed = jawonCheckResult["passed"] as Boolean,
            details = mapOf("details" to jawonCheckResult)
        ))

        if (!(jawonCheckResult["passed"] as Boolean)) {
            return false
        }

        // 한글 자연스러움 체크
        if (nameRepository.existsHangulName(combinedPronounciation)) {
            result.filteringProcess.add(FilteringStep(
                step = "hangul_naturalness_check",
                passed = true,
                details = mapOf(
                    "name" to combinedPronounciation,
                    "name_data" to nameRepository.getHangulNameData(combinedPronounciation)
                )
            ))
        } else {
            result.filteringProcess.add(FilteringStep(
                step = "hangul_naturalness_check",
                passed = false,
                details = mapOf("name" to combinedPronounciation)
            ))
            return false
        }

        // 최종 결과 정보
        result.combinedHanja = combinedHanja
        result.combinedPronounciation = combinedPronounciation

        return true
    }

    private fun checkJawon(jawon1: String, jawon2: String, zeroElements: List<String>, oneElements: List<String>): Map<String, Any> {
        val result = mutableMapOf<String, Any>(
            "jawon1" to jawon1,
            "jawon2" to jawon2,
            "check_type" to "",
            "passed" to false
        )

        when {
            zeroElements.size == 1 -> {
                result["check_type"] = "single_zero_element"
                if (jawon1 == zeroElements[0] && jawon2 == zeroElements[0]) {
                    result["passed"] = true
                } else {
                    result["reason"] = "expected both ${zeroElements[0]}, got $jawon1 and $jawon2"
                }
            }
            zeroElements.size >= 2 -> {
                result["check_type"] = "multiple_zero_elements"
                var valid = false
                for (i in zeroElements.indices) {
                    for (j in zeroElements.indices) {
                        if (i != j) {
                            val zero1 = zeroElements[i]
                            val zero2 = zeroElements[j]
                            if ((jawon1 == zero1 && jawon2 == zero2) || (jawon1 == zero2 && jawon2 == zero1)) {
                                valid = true
                                result["matched_combination"] = listOf(zero1, zero2)
                                break
                            }
                        }
                    }
                    if (valid) break
                }
                result["passed"] = valid
                if (!valid) {
                    result["reason"] = "no matching combination found for $jawon1 and $jawon2"
                }
            }
            oneElements.size == 1 -> {
                result["check_type"] = "single_one_element"
                if (jawon1 == oneElements[0] || jawon2 == oneElements[0]) {
                    result["passed"] = true
                } else {
                    result["reason"] = "neither is ${oneElements[0]}"
                }
            }
            oneElements.size >= 2 -> {
                result["check_type"] = "multiple_one_elements"
                var valid = false
                for (i in oneElements.indices) {
                    for (j in oneElements.indices) {
                        if (i != j) {
                            val one1 = oneElements[i]
                            val one2 = oneElements[j]
                            if ((jawon1 == one1 && jawon2 == one2) || (jawon1 == one2 && jawon2 == one1)) {
                                valid = true
                                result["matched_combination"] = listOf(one1, one2)
                                break
                            }
                        }
                    }
                    if (valid) break
                }
                result["passed"] = valid
                if (!valid) {
                    result["reason"] = "no matching combination found for $jawon1 and $jawon2"
                }
            }
            else -> {
                result["check_type"] = "no_filter"
                result["passed"] = true
            }
        }

        return result
    }
}