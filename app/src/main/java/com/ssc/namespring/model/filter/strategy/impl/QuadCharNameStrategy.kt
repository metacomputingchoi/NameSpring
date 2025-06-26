// model/filter/strategy/impl/QuadCharNameStrategy.kt
package com.ssc.namespring.model.filter.strategy.impl

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.strategy.NameLengthStrategy
import com.ssc.namespring.model.service.EumYangAnalysisService
import com.ssc.namespring.model.util.ValidationResultFactory

class QuadCharNameStrategy : NameLengthStrategy {

    private val eumYangService = EumYangAnalysisService()

    override fun validateEumYang(
        eumyangList: List<Int>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val eumCount = eumyangList.count { it == 0 }
        val yangCount = eumyangList.count { it == 1 }
        val balanced = eumCount == yangCount

        val consecutive = eumYangService.checkConsecutiveCount(
            eumyangList,
            NamingCalculationConstants.EumYangBalance.MAX_CONSECUTIVE_TRIPLE
        )

        return when {
            !balanced -> ValidationResultFactory.createFailure(
                "음양 개수 불균형",
                *details.toList().toTypedArray()
            )
            !consecutive -> ValidationResultFactory.createFailure(
                "연속 음양이 너무 많음",
                *details.toList().toTypedArray()
            )
            else -> ValidationResultFactory.createSuccess(
                "음양 균형 양호",
                *details.toList().toTypedArray()
            )
        }
    }

    override fun validateJawonOhaeng(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val elementCounts = jawonElements.groupingBy { it }.eachCount()
        details["자원오행구성"] = elementCounts

        return when {
            zeroElements.size == NamingCalculationConstants.JawonCheck.QuadChar.ZERO_SINGLE_SIZE -> {
                val allSame = jawonElements.all { it == zeroElements[0] }
                details["모두동일오행"] = allSame
                ValidationResultFactory.createConditional(
                    allSame,
                    "부족한 오행 ${zeroElements[0]}으로 통일",
                    "부족한 오행으로 통일되지 않음",
                    *details.toList().toTypedArray()
                )
            }

            zeroElements.size == NamingCalculationConstants.JawonCheck.QuadChar.ZERO_DOUBLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                val allPairs = counts.all {
                    it == NamingCalculationConstants.JawonCheck.QuadChar.PAIR_COUNT
                }
                details["모두쌍으로구성"] = allPairs
                ValidationResultFactory.createConditional(
                    allPairs,
                    "두 부족 오행이 각각 2개씩 균형있게 구성",
                    "두 부족 오행이 균형있게 구성되지 않음",
                    *details.toList().toTypedArray()
                )
            }

            zeroElements.size == NamingCalculationConstants.JawonCheck.QuadChar.ZERO_TRIPLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                val hasPair = counts.contains(NamingCalculationConstants.JawonCheck.QuadChar.PAIR_COUNT)
                val singleCount = counts.count {
                    it == NamingCalculationConstants.JawonCheck.QuadChar.SINGLE_COUNT
                }
                details["쌍포함"] = hasPair
                details["단일개수"] = singleCount

                when {
                    !hasPair -> ValidationResultFactory.createFailure(
                        "쌍으로 구성된 오행이 없음",
                        *details.toList().toTypedArray()
                    )
                    singleCount != NamingCalculationConstants.JawonCheck.QuadChar.EXPECTED_SINGLE_COUNT ->
                        ValidationResultFactory.createFailure(
                            "단일 오행 개수가 맞지 않음",
                            *details.toList().toTypedArray()
                        )
                    else -> ValidationResultFactory.createSuccess(
                        "세 부족 오행이 2-1-1로 구성",
                        *details.toList().toTypedArray()
                    )
                }
            }

            zeroElements.size >= NamingCalculationConstants.JawonCheck.QuadChar.ZERO_MULTIPLE_SIZE -> {
                val allInZero = jawonElements.all { it in zeroElements }
                val uniqueCount = jawonElements.toSet().size
                details["모두부족오행포함"] = allInZero
                details["고유오행개수"] = uniqueCount

                when {
                    !allInZero -> ValidationResultFactory.createFailure(
                        "부족한 오행이 아닌 것이 포함됨",
                        *details.toList().toTypedArray()
                    )
                    uniqueCount != NamingCalculationConstants.JawonCheck.QuadChar.EXPECTED_UNIQUE_COUNT ->
                        ValidationResultFactory.createFailure(
                            "4개의 서로 다른 오행으로 구성되지 않음",
                            *details.toList().toTypedArray()
                        )
                    else -> ValidationResultFactory.createSuccess(
                        "4개의 서로 다른 부족 오행으로 구성",
                        *details.toList().toTypedArray()
                    )
                }
            }

            oneElements.isNotEmpty() -> {
                val contains = jawonElements.any { it in oneElements }
                details["약한오행포함"] = contains
                ValidationResultFactory.createConditional(
                    contains,
                    "약한 오행을 포함",
                    "약한 오행을 포함하지 않음",
                    *details.toList().toTypedArray()
                )
            }

            else -> ValidationResultFactory.createSuccess(
                "사주 오행이 균형잡혀 있음",
                *details.toList().toTypedArray()
            )
        }
    }
}