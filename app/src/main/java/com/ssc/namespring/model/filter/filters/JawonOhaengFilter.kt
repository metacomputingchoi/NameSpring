// model/filter/filters/JawonOhaengFilter.kt
package com.ssc.namespring.model.filter.filters

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.constants.FilterConstants
import com.ssc.namespring.model.filter.core.AbstractNameFilter
import com.ssc.namespring.model.filter.utils.FilterValidationHelper
import com.ssc.namespring.model.filter.validation.strategies.NameLengthStrategyFactory
import com.ssc.namespring.model.util.normalizeNFC

class JawonOhaengFilter : AbstractNameFilter() {

    override fun getName(): String = FilterConstants.JAWON_OHAENG_FILTER

    override fun getValidationDetails(
        name: GeneratedName,
        context: FilterContext
    ): ValidationResult {
        val zeroElements = extractElementsByThreshold(context.sajuOhaengCount, 0)
        val oneElements = extractElementsByThreshold(context.sajuOhaengCount, 1)
        val jawonElements = extractJawonElements(name)

        val details = createDetailsMap(context, jawonElements, zeroElements, oneElements)

        val strategy = NameLengthStrategyFactory.getStrategy(context.surLength, context.nameLength)
        return strategy.validateJawonOhaeng(jawonElements, zeroElements, oneElements, details)
    }

    private fun extractElementsByThreshold(
        sajuOhaengCount: Map<String, Int>,
        threshold: Int
    ): List<String> {
        return FilterValidationHelper.extractElementsByThreshold(sajuOhaengCount, threshold)
    }

    private fun extractJawonElements(name: GeneratedName): List<String> {
        return name.hanjaDetails.map { it.jawonOhaeng.normalizeNFC() }
    }

    private fun createDetailsMap(
        context: FilterContext,
        jawonElements: List<String>,
        zeroElements: List<String>,
        oneElements: List<String>
    ): MutableMap<String, Any> {
        return FilterValidationHelper.createDetails(
            "자원오행" to jawonElements,
            "사주부족오행" to zeroElements,
            "사주약한오행" to oneElements,
            "성명구조" to "${context.surLength}자성 ${context.nameLength}자이름"
        )
    }
}