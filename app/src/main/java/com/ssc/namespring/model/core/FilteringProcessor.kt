// model/core/FilteringProcessor.kt
package com.ssc.namespring.model.core

import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.FilteringStep
import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.filter.*
import com.ssc.namespring.model.service.AnalysisInfoGenerator
import com.ssc.namespring.model.util.logger.Logger

internal class FilteringProcessor(
    private val filters: List<NameFilterStrategy>,
    private val analysisInfoGenerator: AnalysisInfoGenerator,
    private val logger: Logger
) {
    fun processNamesWithFilters(
        names: Sequence<GeneratedName>,
        surHangul: String,
        surLength: Int,
        nameLength: Int,
        sajuOhaengCount: Map<String, Int>,
        sajuInfo: SajuAnalysisInfo,
        verbose: Boolean,
        withoutFilter: Boolean
    ): List<GeneratedName> {
        val context = FilterContext(surHangul, surLength, nameLength, sajuOhaengCount)

        var processedNames = names
        val filteringStepsByName = mutableMapOf<GeneratedName, List<FilteringStep>>()

        if (withoutFilter) {
            // 평가 모드: 필터링하지 않고 각 이름에 대한 평가 정보만 수집
            processedNames = processedNames.map { name ->
                val filteringSteps = filters.map { filter ->
                    filter.evaluate(name, context)
                }
                filteringStepsByName[name] = filteringSteps
                name
            }
        } else {
            // 필터링 모드: 필터를 적용하여 통과한 이름만 선택
            filters.forEach { filter ->
                processedNames = filter.filterBatch(processedNames, context)
            }
        }

        val results = mutableListOf<GeneratedName>()
        var count = 0

        for (name in processedNames) {
            val filteringSteps = if (withoutFilter) {
                filteringStepsByName[name] ?: emptyList()
            } else {
                createPassedFilteringSteps()
            }

            name.analysisInfo = analysisInfoGenerator.generateAnalysisInfo(
                name, sajuInfo, filteringSteps
            )
            results.add(name)
            count++

            if (verbose && count % 10000 == 0) {
                logger.v("처리 중: ${count}개 완료")
            }
        }

        if (verbose) {
            val modeStr = if (withoutFilter) "평가" else "필터링"
            logger.v("$modeStr 완료: 총 ${results.size}개")
        }

        return results
    }

    private fun createPassedFilteringSteps(): List<FilteringStep> {
        return filters.map { filter ->
            FilteringStep(
                filterName = when (filter) {
                    is BaleumOhaengEumyangFilter -> FilterConstants.BALEUM_OHAENG_EUMYANG_FILTER
                    is JawonOhaengFilter -> FilterConstants.JAWON_OHAENG_FILTER
                    is BaleumNaturalFilter -> FilterConstants.BALEUM_NATURAL_FILTER
                    else -> FilterConstants.UNKNOWN_FILTER
                },
                passed = true,
                reason = "필터 통과",
                details = emptyMap()
            )
        }
    }
}