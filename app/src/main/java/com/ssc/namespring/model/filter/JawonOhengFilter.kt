// model/filter/JawonOhengFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.util.normalizeNFC

class JawonOhengFilter : NameFilterStrategy {

    override fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName> {
        val zeroElements = context.dictElementsCount.filterValues { it == 0 }.keys.map { it.normalizeNFC() }
        val oneElements = context.dictElementsCount.filterValues { it == 1 }.keys.map { it.normalizeNFC() }

        return names.filter { name ->
            val jawonElements = name.hanjaDetails.map { it.sourceElement.normalizeNFC() }
            checkJawonCondition(jawonElements, zeroElements, oneElements, context.surLength, context.nameLength)
        }
    }

    private fun checkJawonCondition(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>,
        surLength: Int,
        nameLength: Int
    ): Boolean {
        return when (surLength to nameLength) {
            Constants.NameLengthCombinations.SINGLE_SINGLE,
            Constants.NameLengthCombinations.DOUBLE_SINGLE -> {
                when {
                    zeroElements.isNotEmpty() -> jawonElements[0] in zeroElements
                    oneElements.isNotEmpty() -> jawonElements[0] in oneElements
                    else -> true
                }
            }
            Constants.NameLengthCombinations.SINGLE_DOUBLE,
            Constants.NameLengthCombinations.DOUBLE_DOUBLE -> checkForDoubleChar(jawonElements, zeroElements, oneElements)
            Constants.NameLengthCombinations.SINGLE_TRIPLE,
            Constants.NameLengthCombinations.DOUBLE_TRIPLE -> checkForTripleChar(jawonElements, zeroElements, oneElements)
            Constants.NameLengthCombinations.SINGLE_QUAD,
            Constants.NameLengthCombinations.DOUBLE_QUAD -> checkForQuadChar(jawonElements, zeroElements, oneElements)
            else -> true
        }
    }

    private fun checkForDoubleChar(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>
    ): Boolean {
        return when {
            zeroElements.size == Constants.JawonCheck.DoubleChar.ZERO_SINGLE_SIZE ->
                jawonElements.all { it == zeroElements[0] }

            zeroElements.size >= Constants.JawonCheck.DoubleChar.ZERO_MULTIPLE_SIZE ->
                jawonElements.all { it in zeroElements } &&
                        jawonElements[0] != jawonElements[1]

            oneElements.size == Constants.JawonCheck.DoubleChar.ONE_SINGLE_SIZE ->
                jawonElements.any { it == oneElements[0] }

            oneElements.size >= Constants.JawonCheck.DoubleChar.ONE_MULTIPLE_SIZE ->
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
            zeroElements.size == Constants.JawonCheck.TripleChar.ZERO_SINGLE_SIZE ->
                jawonElements.all { it == zeroElements[0] }

            zeroElements.size == Constants.JawonCheck.TripleChar.ZERO_DOUBLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                counts.all { it >= Constants.JawonCheck.TripleChar.MIN_COUNT_PER_ELEMENT } &&
                        counts.sum() == Constants.JawonCheck.TripleChar.TRIPLE_SUM
            }

            zeroElements.size >= Constants.JawonCheck.TripleChar.ZERO_MULTIPLE_SIZE ->
                jawonElements.all { it in zeroElements } &&
                        jawonElements.toSet().size == Constants.JawonCheck.TripleChar.EXPECTED_UNIQUE_COUNT

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
            zeroElements.size == Constants.JawonCheck.QuadChar.ZERO_SINGLE_SIZE ->
                jawonElements.all { it == zeroElements[0] }

            zeroElements.size == Constants.JawonCheck.QuadChar.ZERO_DOUBLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                counts.all { it == Constants.JawonCheck.QuadChar.PAIR_COUNT }
            }

            zeroElements.size == Constants.JawonCheck.QuadChar.ZERO_TRIPLE_SIZE -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                counts.contains(Constants.JawonCheck.QuadChar.PAIR_COUNT) &&
                        counts.count { it == Constants.JawonCheck.QuadChar.SINGLE_COUNT } == Constants.JawonCheck.QuadChar.EXPECTED_SINGLE_COUNT
            }

            zeroElements.size >= Constants.JawonCheck.QuadChar.ZERO_MULTIPLE_SIZE ->
                jawonElements.all { it in zeroElements } &&
                        jawonElements.toSet().size == Constants.JawonCheck.QuadChar.EXPECTED_UNIQUE_COUNT

            oneElements.isNotEmpty() ->
                jawonElements.any { it in oneElements }

            else -> true
        }
    }
}