// model/service/YinYangAnalysisService.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.data.analysis.component.YinYangAnalysisInfo
import com.ssc.namespring.model.filter.FilterValidationHelper
import com.ssc.namespring.model.filter.strategy.NameLengthStrategy
import com.ssc.namespring.model.filter.strategy.NameLengthStrategyFactory
import kotlin.math.abs

class YinYangAnalysisService {

    fun isYinYangUnbalanced(eumyangList: List<Int>): Boolean {
        return eumyangList.sum() == 0 || eumyangList.sum() == eumyangList.size
    }

    fun analyzeYinYang(eumyangValues: List<Int>): YinYangAnalysisInfo {
        val combinedEumyang = eumyangValues.joinToString("")
        val yinCount = eumyangValues.count { it == 0 }
        val yangCount = eumyangValues.count { it == 1 }
        val balance = if (yinCount + yangCount > 0) {
            yangCount.toFloat() / (yinCount + yangCount)
        } else 0.5f

        return YinYangAnalysisInfo(
            combinedEumyang = combinedEumyang,
            yinCount = yinCount,
            yangCount = yangCount,
            balance = balance,
            pattern = analyzeYinYangPattern(yinCount, yangCount),
            isBalanced = isBalanced(yinCount, yangCount),
            balanceDescription = describeYinYangBalance(yinCount, yangCount)
        )
    }

    fun checkYinYangBalanceWithDetails(
        eumyang: String,
        surLength: Int,
        nameLength: Int
    ): ValidationResult {
        val eumyangSet = eumyang.toSet()
        val eumyangList = eumyang.map { it.toString().toInt() }
        val yinCount = eumyangList.count { it == 0 }
        val yangCount = eumyangList.count { it == 1 }

        val details = FilterValidationHelper.createDetails(
            "음개수" to yinCount,
            "양개수" to yangCount,
            "음양종류수" to eumyangSet.size
        )

        // 다양성 체크
        if (eumyangSet.size < NamingCalculationConstants.YinYangBalance.MIN_VARIETY) {
            return ValidationResult(
                isValid = false,
                reason = "음양 다양성 부족 (최소 ${NamingCalculationConstants.YinYangBalance.MIN_VARIETY}종류 필요)",
                details = details
            )
        }

        // 이름 길이별 전략 적용
        val strategy = NameLengthStrategyFactory.getStrategy(surLength, nameLength)
        return strategy.validateYinYang(eumyangList, details)
    }

    fun checkConsecutiveCount(eumyangList: List<Int>, maxAllowed: Int): Boolean {
        var consecutiveCount = (1 until eumyangList.size).count { index ->
            eumyangList[index] == eumyangList[index - 1]
        }
        if (eumyangList.first() == eumyangList.last()) consecutiveCount++
        return consecutiveCount <= maxAllowed
    }

    private fun analyzeYinYangPattern(yinCount: Int, yangCount: Int): String {
        return when {
            yinCount == 0 -> "전체 양(陽)"
            yangCount == 0 -> "전체 음(陰)"
            yinCount > yangCount * 2 -> "음(陰) 과다"
            yangCount > yinCount * 2 -> "양(陽) 과다"
            abs(yinCount - yangCount) <= 1 -> "음양 균형"
            else -> "음양 편중"
        }
    }

    private fun isBalanced(yinCount: Int, yangCount: Int): Boolean {
        return yinCount > 0 && yangCount > 0 && abs(yinCount - yangCount) <= 2
    }

    private fun describeYinYangBalance(yinCount: Int, yangCount: Int): String {
        val total = yinCount + yangCount
        return if (total > 0) {
            "음(陰) ${yinCount}개(${(yinCount * 100 / total)}%), 양(陽) ${yangCount}개(${(yangCount * 100 / total)}%)"
        } else {
            "음양 정보 없음"
        }
    }
}