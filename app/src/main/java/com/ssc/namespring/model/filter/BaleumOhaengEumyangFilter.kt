// model/filter/BaleumOhaengEumyangFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.FilteringStep
import com.ssc.namespring.model.exception.NamingException
import kotlin.math.abs

class BaleumOhaengEumyangFilter(
    private val getBaleumOhaeng: (Char) -> String?,
    private val getBaleumEumyang: (Char) -> Int?,
    private val checkBaleumOhaengHarmony: (String) -> Boolean
) : NameFilterStrategy {

    override fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName> {
        val surBaleumOhaeng = context.surHangul.mapNotNull { getBaleumOhaeng(it) }
        val surBaleumEumyang = context.surHangul.mapNotNull { getBaleumEumyang(it) }

        return names.filter { name ->
            try {
                isValid(name, context, surBaleumOhaeng, surBaleumEumyang)
            } catch (e: Exception) {
                throw NamingException.FilteringException(
                    "발음오행음양 필터 처리 중 오류 발생",
                    filterName = "BaleumOhaengEumyangFilter",
                    cause = e
                )
            }
        }
    }

    override fun filterBatch(names: Sequence<GeneratedName>, context: FilterContext): Sequence<GeneratedName> {
        val surBaleumOhaeng = context.surHangul.mapNotNull { getBaleumOhaeng(it) }
        val surBaleumEumyang = context.surHangul.mapNotNull { getBaleumEumyang(it) }

        return names.filter { name ->
            try {
                isValid(name, context, surBaleumOhaeng, surBaleumEumyang)
            } catch (e: Exception) {
                throw NamingException.FilteringException(
                    "발음오행음양 필터 배치 처리 중 오류 발생",
                    filterName = "BaleumOhaengEumyangFilter",
                    cause = e
                )
            }
        }
    }

    override fun evaluate(name: GeneratedName, context: FilterContext): FilteringStep {
        val surBaleumOhaeng = context.surHangul.mapNotNull { getBaleumOhaeng(it) }
        val surBaleumEumyang = context.surHangul.mapNotNull { getBaleumEumyang(it) }

        return try {
            val validationResult = getValidationDetails(name, context, surBaleumOhaeng, surBaleumEumyang)

            FilteringStep(
                filterName = "발음오행음양필터",
                passed = validationResult.isValid,
                reason = validationResult.reason,
                details = validationResult.details
            )
        } catch (e: Exception) {
            FilteringStep(
                filterName = "발음오행음양필터",
                passed = false,
                reason = "평가 중 오류 발생: ${e.message}",
                details = emptyMap()
            )
        }
    }

    private fun isValid(
        name: GeneratedName,
        context: FilterContext,
        surBaleumOhaeng: List<String>,
        surBaleumEumyang: List<Int>
    ): Boolean {
        val validationResult = getValidationDetails(name, context, surBaleumOhaeng, surBaleumEumyang)
        return validationResult.isValid
    }

    private fun getValidationDetails(
        name: GeneratedName,
        context: FilterContext,
        surBaleumOhaeng: List<String>,
        surBaleumEumyang: List<Int>
    ): ValidationResult {
        val nameBaleumOhaeng = name.combinedPronounciation.mapNotNull { getBaleumOhaeng(it) }
        val nameBaleumEumyang = name.combinedPronounciation.mapNotNull { getBaleumEumyang(it) }

        val combinedBaleumOhaeng = (surBaleumOhaeng + nameBaleumOhaeng).joinToString("")
        val combinedEumyang = (surBaleumEumyang + nameBaleumEumyang).joinToString("") { it.toString() }
        val totalChars = context.surLength + context.nameLength

        val details = mutableMapOf<String, Any>(
            "발음오행" to combinedBaleumOhaeng,
            "발음음양" to combinedEumyang,
            "총글자수" to totalChars
        )

        if (combinedBaleumOhaeng.length != totalChars || combinedEumyang.length != totalChars) {
            return ValidationResult(
                isValid = false,
                reason = "발음오행 또는 음양 정보가 누락됨",
                details = details
            )
        }

        val yinYangResult = checkYinYangBalanceWithDetails(combinedEumyang, context.surLength, context.nameLength)
        details["음양균형"] = yinYangResult.details

        if (!yinYangResult.isValid) {
            return ValidationResult(
                isValid = false,
                reason = yinYangResult.reason,
                details = details
            )
        }

        val harmonyValid = checkBaleumOhaengHarmony(combinedBaleumOhaeng)
        details["오행조화"] = if (harmonyValid) "조화로움" else "부조화"

        return ValidationResult(
            isValid = harmonyValid,
            reason = if (harmonyValid) "발음오행이 조화로움" else "발음오행이 상극 관계",
            details = details
        )
    }

    private fun checkYinYangBalance(eumyang: String, surLength: Int, nameLength: Int): Boolean {
        return checkYinYangBalanceWithDetails(eumyang, surLength, nameLength).isValid
    }

    private fun checkYinYangBalanceWithDetails(
        eumyang: String,
        surLength: Int,
        nameLength: Int
    ): ValidationResult {
        val eumyangSet = eumyang.toSet()
        val eumyangList = eumyang.map { it.toString().toInt() }
        val yinCount = eumyangList.count { it == 0 }
        val yangCount = eumyangList.count { it == 1 }

        val details = mutableMapOf<String, Any>(
            "음개수" to yinCount,
            "양개수" to yangCount,
            "음양종류수" to eumyangSet.size
        )

        if (eumyangSet.size < NamingCalculationConstants.YinYangBalance.MIN_VARIETY) {
            return ValidationResult(
                isValid = false,
                reason = "음양 다양성 부족 (최소 ${NamingCalculationConstants.YinYangBalance.MIN_VARIETY}종류 필요)",
                details = details
            )
        }

        val lengthPair = surLength to nameLength
        details["성명구조"] = "${surLength}자성 ${nameLength}자이름"

        val (isValid, reason) = when (lengthPair) {
            NamingCalculationConstants.NameLengthCombinations.SINGLE_SINGLE,
            NamingCalculationConstants.NameLengthCombinations.SINGLE_DOUBLE,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_SINGLE -> {
                val firstLast = eumyang[0] != eumyang[eumyang.length - 1]
                firstLast to if (firstLast) "처음과 끝의 음양이 다름" else "처음과 끝의 음양이 같음"
            }

            NamingCalculationConstants.NameLengthCombinations.SINGLE_TRIPLE -> {
                val consecutive = checkConsecutiveCount(eumyangList, NamingCalculationConstants.YinYangBalance.MAX_CONSECUTIVE_SINGLE)
                consecutive to if (consecutive) "연속 음양 제한 통과" else "연속 음양이 너무 많음"
            }

            NamingCalculationConstants.NameLengthCombinations.SINGLE_QUAD,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_DOUBLE -> {
                val balanced = yinCount == yangCount
                val consecutive = checkConsecutiveCount(eumyangList,
                    if (lengthPair == NamingCalculationConstants.NameLengthCombinations.SINGLE_QUAD)
                        NamingCalculationConstants.YinYangBalance.MAX_CONSECUTIVE_DOUBLE
                    else NamingCalculationConstants.YinYangBalance.MAX_CONSECUTIVE_SINGLE
                )
                (balanced && consecutive) to when {
                    !balanced -> "음양 개수 불균형"
                    !consecutive -> "연속 음양이 너무 많음"
                    else -> "음양 균형 양호"
                }
            }

            NamingCalculationConstants.NameLengthCombinations.DOUBLE_TRIPLE -> {
                val diff = abs(yinCount - yangCount)
                val balanced = diff == 1
                val consecutive = checkConsecutiveCount(eumyangList, NamingCalculationConstants.YinYangBalance.MAX_CONSECUTIVE_DOUBLE)
                (balanced && consecutive) to when {
                    !balanced -> "음양 개수 차이가 1이 아님"
                    !consecutive -> "연속 음양이 너무 많음"
                    else -> "음양 균형 양호"
                }
            }

            NamingCalculationConstants.NameLengthCombinations.DOUBLE_QUAD -> {
                val balanced = yinCount == yangCount
                val consecutive = checkConsecutiveCount(eumyangList, NamingCalculationConstants.YinYangBalance.MAX_CONSECUTIVE_TRIPLE)
                (balanced && consecutive) to when {
                    !balanced -> "음양 개수 불균형"
                    !consecutive -> "연속 음양이 너무 많음"
                    else -> "음양 균형 양호"
                }
            }

            else -> true to "기타 구조"
        }

        return ValidationResult(isValid, reason, details)
    }

    private fun checkConsecutiveCount(eumyangList: List<Int>, maxAllowed: Int): Boolean {
        var consecutiveCount = (1 until eumyangList.size).count { eumyangList[it] == eumyangList[it - 1] }
        if (eumyangList.first() == eumyangList.last()) consecutiveCount++
        return consecutiveCount <= maxAllowed
    }

    private data class ValidationResult(
        val isValid: Boolean,
        val reason: String,
        val details: Map<String, Any>
    )
}