// model/filter/validation/rules/eumyang/EumYangValidator.kt
package com.ssc.namespring.model.filter.validation.rules.eumyang

import com.ssc.namespring.model.service.EumYangAnalysisService

class EumYangValidator {
    private val eumYangService = EumYangAnalysisService()

    fun validateBalance(eumyangList: List<Int>): BalanceResult {
        val eumCount = eumyangList.count { it == 0 }
        val yangCount = eumyangList.count { it == 1 }
        return BalanceResult(eumCount, yangCount, eumCount == yangCount)
    }

    fun validateFirstLastDifference(eumyangList: List<Int>): Boolean {
        return eumyangList.first() != eumyangList.last()
    }

    fun validateConsecutive(eumyangList: List<Int>, maxConsecutive: Int): Boolean {
        return eumYangService.checkConsecutiveCount(eumyangList, maxConsecutive)
    }

    data class BalanceResult(
        val eumCount: Int,
        val yangCount: Int,
        val isBalanced: Boolean
    )
}