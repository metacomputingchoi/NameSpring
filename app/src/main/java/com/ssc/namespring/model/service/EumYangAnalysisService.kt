// model/service/EumYangAnalysisService.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.data.analysis.component.EumYangAnalysisInfo
import com.ssc.namespring.model.filter.FilterValidationHelper
import com.ssc.namespring.model.filter.validation.strategies.*
import kotlin.math.abs

class EumYangAnalysisService {

    fun isEumYangUnbalanced(eumyangList: List<Int>): Boolean {
        return eumyangList.sum() == 0 || eumyangList.sum() == eumyangList.size
    }

    fun analyzeEumYang(eumyangValues: List<Int>): EumYangAnalysisInfo {
        val combinedEumyang = eumyangValues.joinToString("")
        val eumCount = eumyangValues.count { it == 0 }
        val yangCount = eumyangValues.count { it == 1 }
        val balance = if (eumCount + yangCount > 0) {
            yangCount.toFloat() / (eumCount + yangCount)
        } else 0.5f

        return EumYangAnalysisInfo(
            combinedEumyang = combinedEumyang,
            eumCount = eumCount,
            yangCount = yangCount,
            balance = balance,
            pattern = analyzeEumYangPattern(eumCount, yangCount),
            isBalanced = isBalanced(eumCount, yangCount),
            balanceDescription = describeEumYangBalance(eumCount, yangCount)
        )
    }

    fun checkEumYangBalanceWithDetails(
        eumyang: String,
        surLength: Int,
        nameLength: Int
    ): ValidationResult {
        val eumyangSet = eumyang.toSet()
        val eumyangList = eumyang.map { it.toString().toInt() }
        val eumCount = eumyangList.count { it == 0 }
        val yangCount = eumyangList.count { it == 1 }

        val details = FilterValidationHelper.createDetails(
            "음개수" to eumCount,
            "양개수" to yangCount,
            "음양종류수" to eumyangSet.size
        )

        // 다양성 체크
        if (eumyangSet.size < NamingCalculationConstants.EumYangBalance.MIN_VARIETY) {
            return ValidationResult(
                isValid = false,
                reason = "음양 다양성 부족 (최소 ${NamingCalculationConstants.EumYangBalance.MIN_VARIETY}종류 필요)",
                details = details
            )
        }

        // 이름 길이별 전략 적용
        val strategy = NameLengthStrategyFactory.getStrategy(surLength, nameLength)
        return strategy.validateEumYang(eumyangList, details)
    }

    fun checkConsecutiveCount(eumyangList: List<Int>, maxAllowed: Int): Boolean {
        var consecutiveCount = (1 until eumyangList.size).count { index ->
            eumyangList[index] == eumyangList[index - 1]
        }
        if (eumyangList.first() == eumyangList.last()) consecutiveCount++
        return consecutiveCount <= maxAllowed
    }

    private fun analyzeEumYangPattern(eumCount: Int, yangCount: Int): String {
        return when {
            eumCount == 0 -> "전체 양(陽)"
            yangCount == 0 -> "전체 음(陰)"
            eumCount > yangCount * 2 -> "음(陰) 과다"
            yangCount > eumCount * 2 -> "양(陽) 과다"
            abs(eumCount - yangCount) <= 1 -> "음양 균형"
            else -> "음양 편중"
        }
    }

    private fun isBalanced(eumCount: Int, yangCount: Int): Boolean {
        return eumCount > 0 && yangCount > 0 && abs(eumCount - yangCount) <= 2
    }

    private fun describeEumYangBalance(eumCount: Int, yangCount: Int): String {
        val total = eumCount + yangCount
        return if (total > 0) {
            "음(陰) ${eumCount}개(${(eumCount * 100 / total)}%), 양(陽) ${yangCount}개(${(yangCount * 100 / total)}%)"
        } else {
            "음양 정보 없음"
        }
    }
}