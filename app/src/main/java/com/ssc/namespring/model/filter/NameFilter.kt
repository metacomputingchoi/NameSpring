// model/filter/NameFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.loader.DataLoader
import com.ssc.namespring.model.utils.NameUtils
import com.ssc.namespring.model.analyzer.NameCombinationAnalyzer
import com.ssc.namespring.model.constants.Constants

class NameFilter(private val dataLoader: DataLoader) {

    fun filterNames(
        goodCombinations: List<Map<String, Any>>,
        surHangul: String,
        surHanja: String,
        name1Hangul: String?,
        name1Hanja: String?,
        name2Hangul: String?,
        name2Hanja: String?,
        fourJu: FourJu,
        dictElementsCount: ElementCount,
        zeroElements: List<String>,
        oneElements: List<String>,
        birthInfo: BirthInfo
    ): List<NameResult> {
        val results = mutableListOf<NameResult>()
        val surHangulElement = NameUtils.getHangulElement(surHangul[0])
        val surHangulPm = NameUtils.getHangulPn(surHangul[0])
        val nameAnalyzer = NameCombinationAnalyzer(dataLoader.hanja2Stroke)

        for (comb in goodCombinations) {
            val analysisDetails = comb["analysis_details"] as Map<*, *>
            val stroke1 = comb["stroke1"] as Int
            val stroke2 = comb["stroke2"] as Int

            // 이름1 한자 후보 찾기
            val hanja1List = when {
                name1Hanja != null -> dataLoader.hanjaInfo.filter { it.hanja == name1Hanja }
                name1Hangul != null -> dataLoader.hanjaInfo.filter {
                    it.wonHoeksu == stroke1 && it.inmyeongYongEum == name1Hangul
                }
                else -> dataLoader.hanjaInfo.filter { it.wonHoeksu == stroke1 }
            }

            // 이름2 한자 후보 찾기
            val hanja2List = when {
                name2Hanja != null -> dataLoader.hanjaInfo.filter { it.hanja == name2Hanja }
                name2Hangul != null -> dataLoader.hanjaInfo.filter {
                    it.wonHoeksu == stroke2 && it.inmyeongYongEum == name2Hangul
                }
                else -> dataLoader.hanjaInfo.filter { it.wonHoeksu == stroke2 }
            }

            for (h1 in hanja1List) {
                for (h2 in hanja2List) {
                    if (h1.hanja.isEmpty() || h2.hanja.isEmpty() ||
                        h1.inmyeongYongEum.isNullOrEmpty() || h2.inmyeongYongEum.isNullOrEmpty()) {
                        continue
                    }

                    val result = NameResult(
                        surHangul = surHangul,
                        surHanja = surHanja,
                        surHangulElement = surHangulElement,
                        surHangulPm = surHangulPm,
                        birthInfo = birthInfo,
                        sajuInfo = fourJu,
                        dictElementsCount = dictElementsCount,
                        zeroElements = zeroElements,
                        oneElements = oneElements,
                        combinationAnalysis = nameAnalyzer.mapToCombinationAnalysis(analysisDetails),
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

    private fun processFiltering(
        result: NameResult,
        h1: HanjaInfo,
        h2: HanjaInfo,
        surHangul: String,
        surHangulElement: String?,
        surHangulPm: Int,
        zeroElements: List<String>,
        oneElements: List<String>
    ): Boolean {
        val combinedHanja = h1.hanja + h2.hanja
        val combinedPronounciation = h1.inmyeongYongEum!! + h2.inmyeongYongEum!!

        // 길이 체크
        if (combinedPronounciation.length != Constants.NAME_LENGTH || combinedHanja.length != Constants.NAME_LENGTH) {
            result.filteringProcess.add(FilteringStep(
                step = "length_check",
                passed = false,
                reason = "combined_pronounciation_length=${combinedPronounciation.length}, combined_hanja_length=${combinedHanja.length}"
            ))
            return false
        }

        result.filteringProcess.add(FilteringStep("length_check", true))

        // 원소 추출
        val elem1 = NameUtils.getHangulElement(combinedPronounciation[0])
        val elem2 = NameUtils.getHangulElement(combinedPronounciation[1])
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
        val combinedPm = "$surHangulPm${NameUtils.getHangulPn(combinedPronounciation[0])}${NameUtils.getHangulPn(combinedPronounciation[1])}"

        result.combinedElement = combinedElement
        result.combinedPm = combinedPm

        // 길이 체크 2
        if (combinedElement.length != Constants.COMBINED_LENGTH || combinedPm.length != Constants.COMBINED_LENGTH) {
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
        if (pmSet.size <= Constants.MIN_PM_DIVERSITY) {
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
        if (combinedPm[Constants.PM_FIRST_INDEX] == combinedPm[Constants.PM_THIRD_INDEX]) {
            result.filteringProcess.add(FilteringStep(
                step = "pm_position_check",
                passed = false,
                reason = "pm[0]=${combinedPm[Constants.PM_FIRST_INDEX]} == pm[2]=${combinedPm[Constants.PM_THIRD_INDEX]}"
            ))
            return false
        }

        result.filteringProcess.add(FilteringStep("pm_position_check", true))

        // 원소 조화 체크
        val (isHarmonious, harmonyDetails) = NameUtils.isHarmoniousElementCombination(combinedElement)
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
        val jawon1 = NameUtils.normalize(h1.jawonOheng ?: "")
        val jawon2 = NameUtils.normalize(h2.jawonOheng ?: "")

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
        if (combinedPronounciation in dataLoader.hangulGivenNames) {
            result.filteringProcess.add(FilteringStep(
                step = "hangul_naturalness_check",
                passed = true,
                details = mapOf(
                    "name" to combinedPronounciation,
                    "name_data" to dataLoader.hangulGivenNames[combinedPronounciation]!!
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