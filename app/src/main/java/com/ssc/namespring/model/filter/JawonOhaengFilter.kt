// model/filter/JawonOhaengFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.ValidationResult
import com.ssc.namespring.model.filter.strategy.NameLengthStrategyFactory
import com.ssc.namespring.model.util.normalizeNFC

class JawonOhaengFilter : AbstractNameFilter() {

    override fun getFilterName(): String = FilterConstants.JAWON_OHAENG_FILTER

    override fun getValidationDetails(
        name: GeneratedName,
        context: FilterContext
    ): ValidationResult {
        val zeroElements = FilterValidationHelper.extractElementsByThreshold(context.sajuOhaengCount, 0)
        val oneElements = FilterValidationHelper.extractElementsByThreshold(context.sajuOhaengCount, 1)
        val jawonElements = name.hanjaDetails.map { it.jawonOhaeng.normalizeNFC() }

        val details = FilterValidationHelper.createDetails(
            "자원오행" to jawonElements,
            "사주부족오행" to zeroElements,
            "사주약한오행" to oneElements,
            "성명구조" to "${context.surLength}자성 ${context.nameLength}자이름"
        )

        // 이름 길이별 전략 적용
        val strategy = NameLengthStrategyFactory.getStrategy(context.surLength, context.nameLength)
        return strategy.validateJawonOhaeng(jawonElements, zeroElements, oneElements, details)
    }
}