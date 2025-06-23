// model/application/service/report/analyzer/core/ElementAnalyzer.kt
package com.ssc.namespring.model.application.service.report.analyzer.core

import com.ssc.namespring.model.domain.name.entity.Name
import com.ssc.namespring.model.domain.name.value.ElementDetail
import com.ssc.namespring.model.domain.name.value.ElementRelation
import com.ssc.namespring.model.common.constants.ElementConstants
import com.ssc.namespring.model.application.service.report.data.ReportDataHolder

class ElementAnalyzer {
    private val elementData = ReportDataHolder.elementCharacteristicsData
    private val elementRelationsData = ReportDataHolder.elementRelationsData
    private val strings = ReportDataHolder.elementAnalyzerStrings

    fun analyzeHarmony(result: Name): String {
        val baseAnalysis = strings.baseAnalysis.replace("{element}", result.combinedElement ?: "")
        val harmonyAnalysis = analyzeHarmonyDetails(result)
        return baseAnalysis + harmonyAnalysis
    }

    fun analyzeDetailed(result: Name): ElementDetail {
        val nameElements = extractNameElements(result)
        val relationships = analyzeRelationships(nameElements)
        val cycle = analyzeCycle(nameElements)
        val strength = analyzeStrength(nameElements, result.dictElementsCount.toMap())

        return ElementDetail(
            nameElements = nameElements,
            relationshipMap = relationships,
            cycleAnalysis = cycle,
            strengthAnalysis = strength
        )
    }

    fun getLuckyColors(result: Name): List<String> {
        val elements = extractNameElements(result)
        return elements.flatMap { element ->
            elementData.elementColors[element] ?: emptyList()
        }.distinct()
    }

    private fun analyzeHarmonyDetails(result: Name): String {
        val harmonyCheck = result.filteringProcess.find { it.step == "element_harmony_check" }

        if (harmonyCheck?.details == null) {
            return ""
        }

        @Suppress("UNCHECKED_CAST")
        val harmonyDetails = harmonyCheck.details["harmony_details"] as? List<Map<String, Any>> ?: return ""

        return harmonyDetails.joinToString(" ") { detail ->
            when (detail["relation"]) {
                "harmonious" -> strings.harmonyRelations["harmonious"]?.replace("{elements}", detail["elements"].toString()) ?: ""
                else -> strings.harmonyRelations["conflicting"]?.replace("{elements}", detail["elements"].toString()) ?: ""
            }
        }
    }

    private fun extractNameElements(result: Name): List<String> {
        val elements = result.combinedElement ?: return emptyList()
        return elements.map { it.toString() }
    }

    private fun analyzeRelationships(elements: List<String>): List<ElementRelation> {
        if (elements.size < 2) return emptyList()

        val relations = mutableListOf<ElementRelation>()
        val positionLabels = strings.positionLabels

        // 성씨와 이름1의 관계
        if (elements.size >= 2) {
            relations.add(analyzeRelation(elements[0], elements[1], positionLabels["surname_to_first"] ?: strings.positionLabels["surname_to_first"]!!))
        }

        // 이름1과 이름2의 관계
        if (elements.size >= strings.magicNumbers["min_elements_for_cycle"]!!) {
            relations.add(analyzeRelation(elements[1], elements[2], positionLabels["first_to_second"] ?: strings.positionLabels["first_to_second"]!!))
        }

        // 성씨와 이름2의 관계
        if (elements.size >= strings.magicNumbers["min_elements_for_cycle"]!!) {
            relations.add(analyzeRelation(elements[0], elements[2], positionLabels["surname_to_second"] ?: strings.positionLabels["surname_to_second"]!!))
        }

        return relations
    }

    private fun analyzeRelation(from: String, to: String, position: String): ElementRelation {
        val elementsOrder = ElementConstants.ELEMENTS
        val fromIdx = elementsOrder.indexOf(from)
        val toIdx = elementsOrder.indexOf(to)

        if (fromIdx == -1 || toIdx == -1) {
            return ElementRelation(from, to, strings.relationErrors["unknown"]!!, strings.relationErrors["analysis_error"]!!)
        }

        val elementsCount = strings.magicNumbers["elements_count"]!!
        val diff = (toIdx - fromIdx + elementsCount) % elementsCount

        val relationTypes = elementRelationsData.relationTypes
        return when (diff) {
            0 -> {
                val relationType = relationTypes["same"]!!
                ElementRelation(from, to, relationType.name,
                    "$position: ${relationType.description.replace("{element}", elementData.elementCharacteristics[from] ?: from)}")
            }
            strings.magicNumbers["generative_diff_1"], strings.magicNumbers["generative_diff_2"] -> {
                val relationType = relationTypes["generative"]!!
                ElementRelation(from, to, relationType.name,
                    "$position: $from 생 $to - ${relationType.description} ${getGenerativeInfluence(from, to)}")
            }
            strings.magicNumbers["controlling_diff_1"], strings.magicNumbers["controlling_diff_2"] -> {
                val relationType = relationTypes["controlling"]!!
                ElementRelation(from, to, relationType.name,
                    "$position: $from 극 $to - ${relationType.description} ${getControllingInfluence(from, to)}")
            }
            else -> {
                val relationType = relationTypes["special"]!!
                ElementRelation(from, to, relationType.name, relationType.description)
            }
        }
    }

    private fun getGenerativeInfluence(from: String, to: String): String {
        val key = "$from→$to"
        return elementData.elementGenerativeRelations[key] ?: strings.defaultRelations["generative"]!!
    }

    private fun getControllingInfluence(from: String, to: String): String {
        val key = "$from→$to"
        return elementData.elementControllingRelations[key] ?: strings.defaultRelations["controlling"]!!
    }

    private fun analyzeCycle(elements: List<String>): String {
        if (elements.size < strings.magicNumbers["min_elements_for_cycle"]!!) return strings.cycleAnalysis["insufficient_elements"]!!

        val cycleType = determineCycleType(elements)
        val lifeFlowData = ReportDataHolder.lifeFlowData

        val cycleStructure = strings.cycleAnalysis["cycle_structure"]!!
            .replace("{cycle_type}", lifeFlowData.cycleTypes[cycleType] ?: cycleType)
            .replace("{interpretation}", getCycleInterpretation(cycleType, elements))

        return cycleStructure
    }

    private fun determineCycleType(elements: List<String>): String {
        return when {
            isGenerativeCycle(elements) -> "generative_cycle"
            isControllingCycle(elements) -> "controlling_cycle"
            hasGenerativeElements(elements) -> "partial_generative"
            hasControllingElements(elements) -> "partial_controlling"
            else -> "mixed"
        }
    }

    private fun isGenerativeCycle(elements: List<String>): Boolean {
        for (i in 0 until elements.size - 1) {
            if (!isGenerative(elements[i], elements[i + 1])) {
                return false
            }
        }
        return true
    }

    private fun isControllingCycle(elements: List<String>): Boolean {
        for (i in 0 until elements.size - 1) {
            if (!isControlling(elements[i], elements[i + 1])) {
                return false
            }
        }
        return true
    }

    private fun hasGenerativeElements(elements: List<String>): Boolean {
        for (i in 0 until elements.size - 1) {
            if (isGenerative(elements[i], elements[i + 1])) {
                return true
            }
        }
        return false
    }

    private fun hasControllingElements(elements: List<String>): Boolean {
        for (i in 0 until elements.size - 1) {
            if (isControlling(elements[i], elements[i + 1])) {
                return true
            }
        }
        return false
    }

    private fun isGenerative(from: String, to: String): Boolean {
        val elementsOrder = ElementConstants.ELEMENTS
        val fromIdx = elementsOrder.indexOf(from)
        val toIdx = elementsOrder.indexOf(to)
        val elementsCount = strings.magicNumbers["elements_count"]!!
        val diff = (toIdx - fromIdx + elementsCount) % elementsCount
        return diff == strings.magicNumbers["generative_diff_1"] || diff == strings.magicNumbers["generative_diff_2"]
    }

    private fun isControlling(from: String, to: String): Boolean {
        val elementsOrder = ElementConstants.ELEMENTS
        val fromIdx = elementsOrder.indexOf(from)
        val toIdx = elementsOrder.indexOf(to)
        val elementsCount = strings.magicNumbers["elements_count"]!!
        val diff = (toIdx - fromIdx + elementsCount) % elementsCount
        return diff == strings.magicNumbers["controlling_diff_1"] || diff == strings.magicNumbers["controlling_diff_2"]
    }

    private fun getCycleInterpretation(cycleType: String, elements: List<String>): String {
        val lifeFlowData = ReportDataHolder.lifeFlowData
        val interpretation = lifeFlowData.cycleInterpretations[cycleType] ?: strings.cycleAnalysis["default_interpretation"]!!
        return interpretation.replace("{elements}", elements.joinToString("→"))
    }

    private fun analyzeStrength(nameElements: List<String>, sajuElements: Map<String, Int>): Map<String, String> {
        val namePositions = ReportDataHolder.constantsData.namePositions
        val strengthDescriptions = elementRelationsData.elementStrengthDescriptions

        return nameElements.mapIndexed { index, element ->
            val position = namePositions[index.toString()] ?: strings.defaultPosition
            val sajuCount = sajuElements[element] ?: 0

            val strength = when (sajuCount) {
                0 -> strengthDescriptions["0"]
                1 -> strengthDescriptions["1"]
                2 -> strengthDescriptions["2"]
                3 -> strengthDescriptions["3"]
                else -> strengthDescriptions["4_above"]
            } ?: strings.strengthError

            strings.nameFormat
                .replace("{position}", position)
                .replace("{element}", element) to strength
        }.toMap()
    }
}