// model/filter/JawonOhaengFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.util.normalizeNFC

class JawonOhaengFilter : AbstractNameFilter() {

    override fun getFilterName(): String = "자원오행필터"

    override fun isValid(
        name: GeneratedName,
        context: FilterContext
    ): Boolean {
        val zeroElements = context.sajuOhaengCount.filterValues { it == 0 }.keys.map { it.normalizeNFC() }
        val oneElements = context.sajuOhaengCount.filterValues { it == 1 }.keys.map { it.normalizeNFC() }
        val validationResult = getValidationDetailsInternal(name, context, zeroElements, oneElements)
        return validationResult.isValid
    }

    override fun getValidationDetails(
        name: GeneratedName,
        context: FilterContext
    ): ValidationResult {
        val zeroElements = context.sajuOhaengCount.filterValues { it == 0 }.keys.map { it.normalizeNFC() }
        val oneElements = context.sajuOhaengCount.filterValues { it == 1 }.keys.map { it.normalizeNFC() }
        return getValidationDetailsInternal(name, context, zeroElements, oneElements)
    }

    private fun getValidationDetailsInternal(
        name: GeneratedName,
        context: FilterContext,
        zeroElements: List<String>,
        oneElements: List<String>
    ): ValidationResult {
        val jawonElements = name.hanjaDetails.map { it.jawonOhaeng.normalizeNFC() }

        val details = mutableMapOf<String, Any>(
            "자원오행" to jawonElements,
            "사주부족오행" to zeroElements,
            "사주약한오행" to oneElements,
            "성명구조" to "${context.surLength}자성 ${context.nameLength}자이름"
        )

        return checkJawonConditionWithDetails(
            jawonElements,
            zeroElements,
            oneElements,
            context.surLength,
            context.nameLength,
            details
        )
    }

    private fun checkJawonConditionWithDetails(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        surLength: Int,
        nameLength: Int,
        details: MutableMap<String, Any>
    ): ValidationResult {
        val lengthPair = surLength to nameLength

        return when (lengthPair) {
            NamingCalculationConstants.NameLengthCombinations.SINGLE_SINGLE,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_SINGLE -> {
                when {
                    zeroElements.isNotEmpty() -> {
                        val contains = jawonElements[0] in zeroElements
                        details["보완오행포함"] = contains
                        ValidationResult(
                            contains,
                            if (contains) "부족한 오행 ${jawonElements[0]}을(를) 보완함"
                            else "부족한 오행을 보완하지 못함",
                            details
                        )
                    }
                    oneElements.isNotEmpty() -> {
                        val contains = jawonElements[0] in oneElements
                        details["보강오행포함"] = contains
                        ValidationResult(
                            contains,
                            if (contains) "약한 오행 ${jawonElements[0]}을(를) 보강함"
                            else "약한 오행을 보강하지 못함",
                            details
                        )
                    }
                    else -> ValidationResult(true, "사주 오행이 균형잡혀 있음", details)
                }
            }

            NamingCalculationConstants.NameLengthCombinations.SINGLE_DOUBLE,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_DOUBLE ->
                checkForDoubleCharWithDetails(jawonElements, zeroElements, oneElements, details)

            NamingCalculationConstants.NameLengthCombinations.SINGLE_TRIPLE,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_TRIPLE ->
                checkForTripleCharWithDetails(jawonElements, zeroElements, oneElements, details)

            NamingCalculationConstants.NameLengthCombinations.SINGLE_QUAD,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_QUAD ->
                checkForQuadCharWithDetails(jawonElements, zeroElements, oneElements, details)

            else -> ValidationResult(true, "기타 구조", details)
        }
    }

    private fun checkForDoubleCharWithDetails(
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
                ValidationResult(
                    allSame,
                    if (allSame) "부족한 오행 ${zeroElements[0]}으로 통일"
                    else "부족한 오행으로 통일되지 않음",
                    details
                )
            }

            zeroElements.size >= NamingCalculationConstants.JawonCheck.DoubleChar.ZERO_MULTIPLE_SIZE -> {
                val allInZero = jawonElements.all { it in zeroElements }
                val different = jawonElements[0] != jawonElements[1]
                details["모두부족오행포함"] = allInZero
                details["서로다른오행"] = different
                ValidationResult(
                    allInZero && different,
                    when {
                        !allInZero -> "부족한 오행이 아닌 것이 포함됨"
                        !different -> "같은 오행으로 구성됨"
                        else -> "서로 다른 부족 오행으로 구성"
                    },
                    details
                )
            }

            oneElements.size == NamingCalculationConstants.JawonCheck.DoubleChar.ONE_SINGLE_SIZE -> {
                val contains = jawonElements.any { it == oneElements[0] }
                details["약한오행포함"] = contains
                ValidationResult(
                    contains,
                    if (contains) "약한 오행 ${oneElements[0]}을(를) 포함"
                    else "약한 오행을 포함하지 않음",
                    details
                )
            }

            oneElements.size >= NamingCalculationConstants.JawonCheck.DoubleChar.ONE_MULTIPLE_SIZE -> {
                val allInOne = jawonElements.all { it in oneElements }
                val different = jawonElements[0] != jawonElements[1]
                details["모두약한오행포함"] = allInOne
                details["서로다른오행"] = different
                ValidationResult(
                    allInOne && different,
                    when {
                        !allInOne -> "약한 오행이 아닌 것이 포함됨"
                        !different -> "같은 오행으로 구성됨"
                        else -> "서로 다른 약한 오행으로 구성"
                    },
                    details
                )
            }

            else -> ValidationResult(true, "사주 오행이 균형잡혀 있음", details)
        }
    }

    private fun checkForTripleCharWithDetails(
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
                ValidationResult(
                    allSame,
                    if (allSame) "부족한 오행 ${zeroElements[0]}으로 통일"
                    else "부족한 오행으로 통일되지 않음",
                    details
                )
            }

            zeroElements.size == NamingCalculationConstants.JawonCheck.TripleChar.ZERO_DOUBLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                val allHaveMin = counts.all { it >= NamingCalculationConstants.JawonCheck.TripleChar.MIN_COUNT_PER_ELEMENT }
                val sumCorrect = counts.sum() == NamingCalculationConstants.JawonCheck.TripleChar.TRIPLE_SUM
                details["각오행최소개수충족"] = allHaveMin
                details["총개수정확"] = sumCorrect
                ValidationResult(
                    allHaveMin && sumCorrect,
                    when {
                        !allHaveMin -> "각 부족 오행이 최소 1개씩 포함되지 않음"
                        !sumCorrect -> "오행 개수 합이 맞지 않음"
                        else -> "두 부족 오행을 균형있게 포함"
                    },
                    details
                )
            }

            zeroElements.size >= NamingCalculationConstants.JawonCheck.TripleChar.ZERO_MULTIPLE_SIZE -> {
                val allInZero = jawonElements.all { it in zeroElements }
                val uniqueCount = jawonElements.toSet().size
                details["모두부족오행포함"] = allInZero
                details["고유오행개수"] = uniqueCount
                ValidationResult(
                    allInZero && uniqueCount == NamingCalculationConstants.JawonCheck.TripleChar.EXPECTED_UNIQUE_COUNT,
                    when {
                        !allInZero -> "부족한 오행이 아닌 것이 포함됨"
                        uniqueCount != NamingCalculationConstants.JawonCheck.TripleChar.EXPECTED_UNIQUE_COUNT ->
                            "3개의 서로 다른 오행으로 구성되지 않음"
                        else -> "3개의 서로 다른 부족 오행으로 구성"
                    },
                    details
                )
            }

            oneElements.isNotEmpty() -> {
                val contains = jawonElements.any { it in oneElements }
                details["약한오행포함"] = contains
                ValidationResult(
                    contains,
                    if (contains) "약한 오행을 포함"
                    else "약한 오행을 포함하지 않음",
                    details
                )
            }

            else -> ValidationResult(true, "사주 오행이 균형잡혀 있음", details)
        }
    }

    private fun checkForQuadCharWithDetails(
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
                ValidationResult(
                    allSame,
                    if (allSame) "부족한 오행 ${zeroElements[0]}으로 통일"
                    else "부족한 오행으로 통일되지 않음",
                    details
                )
            }

            zeroElements.size == NamingCalculationConstants.JawonCheck.QuadChar.ZERO_DOUBLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                val allPairs = counts.all { it == NamingCalculationConstants.JawonCheck.QuadChar.PAIR_COUNT }
                details["모두쌍으로구성"] = allPairs
                ValidationResult(
                    allPairs,
                    if (allPairs) "두 부족 오행이 각각 2개씩 균형있게 구성"
                    else "두 부족 오행이 균형있게 구성되지 않음",
                    details
                )
            }

            zeroElements.size == NamingCalculationConstants.JawonCheck.QuadChar.ZERO_TRIPLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                val hasPair = counts.contains(NamingCalculationConstants.JawonCheck.QuadChar.PAIR_COUNT)
                val singleCount = counts.count { it == NamingCalculationConstants.JawonCheck.QuadChar.SINGLE_COUNT }
                details["쌍포함"] = hasPair
                details["단일개수"] = singleCount
                ValidationResult(
                    hasPair && singleCount == NamingCalculationConstants.JawonCheck.QuadChar.EXPECTED_SINGLE_COUNT,
                    when {
                        !hasPair -> "쌍으로 구성된 오행이 없음"
                        singleCount != NamingCalculationConstants.JawonCheck.QuadChar.EXPECTED_SINGLE_COUNT ->
                            "단일 오행 개수가 맞지 않음"
                        else -> "세 부족 오행이 2-1-1로 구성"
                    },
                    details
                )
            }

            zeroElements.size >= NamingCalculationConstants.JawonCheck.QuadChar.ZERO_MULTIPLE_SIZE -> {
                val allInZero = jawonElements.all { it in zeroElements }
                val uniqueCount = jawonElements.toSet().size
                details["모두부족오행포함"] = allInZero
                details["고유오행개수"] = uniqueCount
                ValidationResult(
                    allInZero && uniqueCount == NamingCalculationConstants.JawonCheck.QuadChar.EXPECTED_UNIQUE_COUNT,
                    when {
                        !allInZero -> "부족한 오행이 아닌 것이 포함됨"
                        uniqueCount != NamingCalculationConstants.JawonCheck.QuadChar.EXPECTED_UNIQUE_COUNT ->
                            "4개의 서로 다른 오행으로 구성되지 않음"
                        else -> "4개의 서로 다른 부족 오행으로 구성"
                    },
                    details
                )
            }

            oneElements.isNotEmpty() -> {
                val contains = jawonElements.any { it in oneElements }
                details["약한오행포함"] = contains
                ValidationResult(
                    contains,
                    if (contains) "약한 오행을 포함"
                    else "약한 오행을 포함하지 않음",
                    details
                )
            }

            else -> ValidationResult(true, "사주 오행이 균형잡혀 있음", details)
        }
    }
}