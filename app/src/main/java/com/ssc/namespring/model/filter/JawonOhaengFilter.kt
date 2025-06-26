// model/filter/JawonOhaengFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.util.normalizeNFC

class JawonOhaengFilter : NameFilterStrategy {

    override fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName> {
        val zeroElements = context.sajuOhaengCount.filterValues { it == 0 }.keys.map { it.normalizeNFC() }
        val oneElements = context.sajuOhaengCount.filterValues { it == 1 }.keys.map { it.normalizeNFC() }

        return names.filter { name ->
            isValid(name, context, zeroElements, oneElements)
        }
    }

    override fun filterBatch(names: Sequence<GeneratedName>, context: FilterContext): Sequence<GeneratedName> {
        val zeroElements = context.sajuOhaengCount.filterValues { it == 0 }.keys.map { it.normalizeNFC() }
        val oneElements = context.sajuOhaengCount.filterValues { it == 1 }.keys.map { it.normalizeNFC() }

        return names.filter { name ->
            isValid(name, context, zeroElements, oneElements)
        }
    }

    private fun isValid(
        name: GeneratedName, 
        context: FilterContext,
        zeroElements: List<String>,
        oneElements: List<String>
    ): Boolean {
        val jawonElements = name.hanjaDetails.map { it.jawonOhaeng.normalizeNFC() }
        return checkJawonCondition(jawonElements, zeroElements, oneElements, context.surLength, context.nameLength)
    }

    private fun checkJawonCondition(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        surLength: Int,
        nameLength: Int
    ): Boolean {
        return when (surLength to nameLength) {
            NamingCalculationConstants.NameLengthCombinations.SINGLE_SINGLE,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_SINGLE -> {
                when {
                    zeroElements.isNotEmpty() -> jawonElements[0] in zeroElements
                    oneElements.isNotEmpty() -> jawonElements[0] in oneElements
                    else -> true
                }
            }
            NamingCalculationConstants.NameLengthCombinations.SINGLE_DOUBLE,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_DOUBLE -> checkForDoubleChar(jawonElements, zeroElements, oneElements)
            NamingCalculationConstants.NameLengthCombinations.SINGLE_TRIPLE,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_TRIPLE -> checkForTripleChar(jawonElements, zeroElements, oneElements)
            NamingCalculationConstants.NameLengthCombinations.SINGLE_QUAD,
            NamingCalculationConstants.NameLengthCombinations.DOUBLE_QUAD -> checkForQuadChar(jawonElements, zeroElements, oneElements)
            else -> true
        }
    }

    private fun checkForDoubleChar(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>
    ): Boolean {
        return when {
            zeroElements.size == NamingCalculationConstants.JawonCheck.DoubleChar.ZERO_SINGLE_SIZE ->
                jawonElements.all { it == zeroElements[0] }

            zeroElements.size >= NamingCalculationConstants.JawonCheck.DoubleChar.ZERO_MULTIPLE_SIZE ->
                jawonElements.all { it in zeroElements } &&
                        jawonElements[0] != jawonElements[1]

            oneElements.size == NamingCalculationConstants.JawonCheck.DoubleChar.ONE_SINGLE_SIZE ->
                jawonElements.any { it == oneElements[0] }

            oneElements.size >= NamingCalculationConstants.JawonCheck.DoubleChar.ONE_MULTIPLE_SIZE ->
                jawonElements.all { it in oneElements } &&
                        jawonElements[0] != jawonElements[1]

            else -> true
        }
    }

    private fun checkForTripleChar(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>
    ): Boolean {
        return when {
            zeroElements.size == NamingCalculationConstants.JawonCheck.TripleChar.ZERO_SINGLE_SIZE ->
                jawonElements.all { it == zeroElements[0] }

            zeroElements.size == NamingCalculationConstants.JawonCheck.TripleChar.ZERO_DOUBLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                counts.all { it >= NamingCalculationConstants.JawonCheck.TripleChar.MIN_COUNT_PER_ELEMENT } &&
                        counts.sum() == NamingCalculationConstants.JawonCheck.TripleChar.TRIPLE_SUM
            }

            zeroElements.size >= NamingCalculationConstants.JawonCheck.TripleChar.ZERO_MULTIPLE_SIZE ->
                jawonElements.all { it in zeroElements } &&
                        jawonElements.toSet().size == NamingCalculationConstants.JawonCheck.TripleChar.EXPECTED_UNIQUE_COUNT

            oneElements.isNotEmpty() ->
                jawonElements.any { it in oneElements }

            else -> true
        }
    }

    private fun checkForQuadChar(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>
    ): Boolean {
        return when {
            zeroElements.size == NamingCalculationConstants.JawonCheck.QuadChar.ZERO_SINGLE_SIZE ->
                jawonElements.all { it == zeroElements[0] }

            zeroElements.size == NamingCalculationConstants.JawonCheck.QuadChar.ZERO_DOUBLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                counts.all { it == NamingCalculationConstants.JawonCheck.QuadChar.PAIR_COUNT }
            }

            zeroElements.size == NamingCalculationConstants.JawonCheck.QuadChar.ZERO_TRIPLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                counts.contains(NamingCalculationConstants.JawonCheck.QuadChar.PAIR_COUNT) &&
                        counts.count { it == NamingCalculationConstants.JawonCheck.QuadChar.SINGLE_COUNT } == NamingCalculationConstants.JawonCheck.QuadChar.EXPECTED_SINGLE_COUNT
            }

            zeroElements.size >= NamingCalculationConstants.JawonCheck.QuadChar.ZERO_MULTIPLE_SIZE ->
                jawonElements.all { it in zeroElements } &&
                        jawonElements.toSet().size == NamingCalculationConstants.JawonCheck.QuadChar.EXPECTED_UNIQUE_COUNT

            oneElements.isNotEmpty() ->
                jawonElements.any { it in oneElements }

            else -> true
        }
    }
}
