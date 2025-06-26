// model/filter/strategy/impl/TripleCharNameStrategy.kt
package com.ssc.namespring.model.filter.strategy.impl

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.strategy.NameLengthStrategy
import com.ssc.namespring.model.service.EumYangAnalysisService
import com.ssc.namespring.model.util.ValidationResultFactory
import kotlin.math.abs

class TripleCharNameStrategy : NameLengthStrategy {

    private val eumYangService = EumYangAnalysisService()

    override fun validateEumYang(
        eumyangList: List<Int>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val eumCount = eumyangList.count { it == 0 }
        val yangCount = eumyangList.count { it == 1 }

        // 1자성 3자이름인 경우
        if (eumyangList.size == 4) {
            val consecutive = eumYangService.checkConsecutiveCount(
                eumyangList,
                NamingCalculationConstants.EumYangBalance.MAX_CONSECUTIVE_SINGLE
            )
            return ValidationResultFactory.createConditional(
                consecutive,
                "연속 음양 제한 통과",
                "연속 음양이 너무 많음",
                *details.toList().toTypedArray()
            )
        }

        // 2자성 3자이름인 경우
        val diff = abs(eumCount - yangCount)
        val balanced = diff == 1
        val consecutive = eumYangService.checkConsecutiveCount(
            eumyangList,
            NamingCalculationConstants.EumYangBalance.MAX_CONSECUTIVE_DOUBLE
        )

        return when {
            !balanced -> ValidationResultFactory.createFailure(
                "음양 개수 차이가 1이 아님",
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
        details["자원오행구성"] = jawonElements.groupingBy { it }.eachCount()

        return when {
            zeroElements.size == NamingCalculationConstants.JawonCheck.TripleChar.ZERO_SINGLE_SIZE -> {
                val allSame = jawonElements.all { it == zeroElements[0] }
                details["모두동일오행"] = allSame
                ValidationResultFactory.createConditional(
                    allSame,
                    "부족한 오행 ${zeroElements[0]}으로 통일",
                    "부족한 오행으로 통일되지 않음",
                    *details.toList().toTypedArray()
                )
            }

            zeroElements.size == NamingCalculationConstants.JawonCheck.TripleChar.ZERO_DOUBLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                val allHaveMin = counts.all {
                    it >= NamingCalculationConstants.JawonCheck.TripleChar.MIN_COUNT_PER_ELEMENT
                }
                val sumCorrect = counts.sum() == NamingCalculationConstants.JawonCheck.TripleChar.TRIPLE_SUM
                details["각오행최소개수충족"] = allHaveMin
                details["총개수정확"] = sumCorrect

                when {
                    !allHaveMin -> ValidationResultFactory.createFailure(
                        "각 부족 오행이 최소 1개씩 포함되지 않음",
                        *details.toList().toTypedArray()
                    )
                    !sumCorrect -> ValidationResultFactory.createFailure(
                        "오행 개수 합이 맞지 않음",
                        *details.toList().toTypedArray()
                    )
                    else -> ValidationResultFactory.createSuccess(
                        "두 부족 오행을 균형있게 포함",
                        *details.toList().toTypedArray()
                    )
                }
            }

            zeroElements.size >= NamingCalculationConstants.JawonCheck.TripleChar.ZERO_MULTIPLE_SIZE -> {
                val allInZero = jawonElements.all { it in zeroElements }
                val uniqueCount = jawonElements.toSet().size
                details["모두부족오행포함"] = allInZero
                details["고유오행개수"] = uniqueCount

                when {
                    !allInZero -> ValidationResultFactory.createFailure(
                        "부족한 오행이 아닌 것이 포함됨",
                        *details.toList().toTypedArray()
                    )
                    uniqueCount != NamingCalculationConstants.JawonCheck.TripleChar.EXPECTED_UNIQUE_COUNT ->
                        ValidationResultFactory.createFailure(
                            "3개의 서로 다른 오행으로 구성되지 않음",
                            *details.toList().toTypedArray()
                        )
                    else -> ValidationResultFactory.createSuccess(
                        "3개의 서로 다른 부족 오행으로 구성",
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