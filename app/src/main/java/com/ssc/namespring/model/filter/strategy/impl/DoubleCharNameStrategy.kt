// model/filter/strategy/impl/DoubleCharNameStrategy.kt
package com.ssc.namespring.model.filter.strategy.impl

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.common.naming.NamingCalculationConstants.NameLengthCombinations
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.strategy.NameLengthStrategy
import com.ssc.namespring.model.service.YinYangAnalysisService
import com.ssc.namespring.model.util.ValidationResultFactory

class DoubleCharNameStrategy : NameLengthStrategy {

    private val yinYangService = YinYangAnalysisService()

    override fun validateYinYang(
        eumyangList: List<Int>,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val yinCount = eumyangList.count { it == 0 }
        val yangCount = eumyangList.count { it == 1 }

        // 1자성 2자이름인 경우 처음과 끝 체크
        if (eumyangList.size == 3) {
            val firstLast = eumyangList.first() != eumyangList.last()
            return ValidationResultFactory.createConditional(
                firstLast,
                "처음과 끝의 음양이 다름",
                "처음과 끝의 음양이 같음",
                *details.toList().toTypedArray()
            )
        }

        // 2자성 2자이름 또는 1자성 4자이름인 경우
        val balanced = yinCount == yangCount
        val maxConsecutive = if (eumyangList.size == 5) {
            NamingCalculationConstants.YinYangBalance.MAX_CONSECUTIVE_DOUBLE
        } else {
            NamingCalculationConstants.YinYangBalance.MAX_CONSECUTIVE_SINGLE
        }

        val consecutive = yinYangService.checkConsecutiveCount(eumyangList, maxConsecutive)

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
        details["자원오행구성"] = jawonElements.groupingBy { it }.eachCount()

        return when {
            zeroElements.size == NamingCalculationConstants.JawonCheck.DoubleChar.ZERO_SINGLE_SIZE -> {
                val allSame = jawonElements.all { it == zeroElements[0] }
                details["모두동일오행"] = allSame
                ValidationResultFactory.createConditional(
                    allSame,
                    "부족한 오행 ${zeroElements[0]}으로 통일",
                    "부족한 오행으로 통일되지 않음",
                    *details.toList().toTypedArray()
                )
            }

            zeroElements.size >= NamingCalculationConstants.JawonCheck.DoubleChar.ZERO_MULTIPLE_SIZE -> {
                val allInZero = jawonElements.all { it in zeroElements }
                val different = jawonElements[0] != jawonElements[1]
                details["모두부족오행포함"] = allInZero
                details["서로다른오행"] = different

                when {
                    !allInZero -> ValidationResultFactory.createFailure(
                        "부족한 오행이 아닌 것이 포함됨",
                        *details.toList().toTypedArray()
                    )
                    !different -> ValidationResultFactory.createFailure(
                        "같은 오행으로 구성됨",
                        *details.toList().toTypedArray()
                    )
                    else -> ValidationResultFactory.createSuccess(
                        "서로 다른 부족 오행으로 구성",
                        *details.toList().toTypedArray()
                    )
                }
            }

            oneElements.size == NamingCalculationConstants.JawonCheck.DoubleChar.ONE_SINGLE_SIZE -> {
                val contains = jawonElements.any { it == oneElements[0] }
                details["약한오행포함"] = contains
                ValidationResultFactory.createConditional(
                    contains,
                    "약한 오행 ${oneElements[0]}을(를) 포함",
                    "약한 오행을 포함하지 않음",
                    *details.toList().toTypedArray()
                )
            }

            oneElements.size >= NamingCalculationConstants.JawonCheck.DoubleChar.ONE_MULTIPLE_SIZE -> {
                val allInOne = jawonElements.all { it in oneElements }
                val different = jawonElements[0] != jawonElements[1]
                details["모두약한오행포함"] = allInOne
                details["서로다른오행"] = different

                when {
                    !allInOne -> ValidationResultFactory.createFailure(
                        "약한 오행이 아닌 것이 포함됨",
                        *details.toList().toTypedArray()
                    )
                    !different -> ValidationResultFactory.createFailure(
                        "같은 오행으로 구성됨",
                        *details.toList().toTypedArray()
                    )
                    else -> ValidationResultFactory.createSuccess(
                        "서로 다른 약한 오행으로 구성",
                        *details.toList().toTypedArray()
                    )
                }
            }

            else -> ValidationResultFactory.createSuccess(
                "사주 오행이 균형잡혀 있음",
                *details.toList().toTypedArray()
            )
        }
    }
}