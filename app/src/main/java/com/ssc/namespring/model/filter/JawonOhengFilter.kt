// model/filter/JawonOhengFilter.kt
package com.ssc.namespring.model.filter

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
        val key = "$surLength-$nameLength"

        return when (key) {
            "1-1", "2-1" -> {
                when {
                    zeroElements.isNotEmpty() -> jawonElements[0] in zeroElements
                    oneElements.isNotEmpty() -> jawonElements[0] in oneElements
                    else -> true
                }
            }
            "1-2", "2-2" -> checkForDoubleChar(jawonElements, zeroElements, oneElements)
            "1-3", "2-3" -> checkForTripleChar(jawonElements, zeroElements, oneElements)
            "1-4", "2-4" -> checkForQuadChar(jawonElements, zeroElements, oneElements)
            else -> true
        }
    }

    private fun checkForDoubleChar(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>
    ): Boolean {
        return when {
            zeroElements.size == 1 -> jawonElements.all { it == zeroElements[0] }
            zeroElements.size >= 2 -> jawonElements.all { it in zeroElements } && jawonElements[0] != jawonElements[1]
            oneElements.size == 1 -> jawonElements.any { it == oneElements[0] }
            oneElements.size >= 2 -> jawonElements.all { it in oneElements } && jawonElements[0] != jawonElements[1]
            else -> true
        }
    }

    private fun checkForTripleChar(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>
    ): Boolean {
        return when {
            zeroElements.size == 1 -> jawonElements.all { it == zeroElements[0] }
            zeroElements.size == 2 -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                counts.all { it >= 1 } && counts.sum() == 3
            }
            zeroElements.size >= 3 -> jawonElements.all { it in zeroElements } && jawonElements.toSet().size == 3
            oneElements.isNotEmpty() -> jawonElements.any { it in oneElements }
            else -> true
        }
    }

    private fun checkForQuadChar(
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>
    ): Boolean {
        return when {
            zeroElements.size == 1 -> jawonElements.all { it == zeroElements[0] }
            zeroElements.size == 2 -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                counts.all { it == 2 }
            }
            zeroElements.size == 3 -> {
                val counts = zeroElements.map { elem -> jawonElements.count { it == elem } }
                counts.contains(2) && counts.count { it == 1 } == 2
            }
            zeroElements.size >= 4 -> jawonElements.all { it in zeroElements } && jawonElements.toSet().size == 4
            oneElements.isNotEmpty() -> jawonElements.any { it in oneElements }
            else -> true
        }
    }
}
