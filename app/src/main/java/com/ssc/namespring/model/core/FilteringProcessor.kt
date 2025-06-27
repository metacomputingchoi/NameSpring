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
        return FilteringContext.builder()
            .withNames(names)
            .withFilterContext(FilterContext(surHangul, surLength, nameLength, sajuOhaengCount))
            .withSajuInfo(sajuInfo)
            .withVerbose(verbose)
            .withoutFilter(withoutFilter)
            .build()
            .let { context ->
                processWithContext(context)
            }
    }

    private fun processWithContext(context: FilteringContext): List<GeneratedName> {
        return context.names
            .let { names ->
                if (context.withoutFilter) {
                    evaluateWithoutFiltering(names, context)
                } else {
                    applyFilters(names, context)
                }
            }
            .map { (name, filteringSteps) ->
                name.withAnalysisInfo(
                    analysisInfoGenerator.generateAnalysisInfo(
                        name,
                        context.sajuInfo,
                        filteringSteps
                    )
                )
            }
            .toList()
            .also { results ->
                if (context.verbose) {
                    val modeStr = if (context.withoutFilter) "평가" else "필터링"
                    logger.v("$modeStr 완료: 총 ${results.size}개")
                }
            }
    }

    private fun evaluateWithoutFiltering(
        names: Sequence<GeneratedName>,
        context: FilteringContext
    ): Sequence<Pair<GeneratedName, List<FilteringStep>>> {
        return names.map { name ->
            val filteringSteps = filters.map { filter ->
                filter.evaluate(name, context.filterContext)
            }
            name to filteringSteps
        }.also { sequence ->
            logProgress(sequence, context.verbose)
        }
    }

    private fun applyFilters(
        names: Sequence<GeneratedName>,
        context: FilteringContext
    ): Sequence<Pair<GeneratedName, List<FilteringStep>>> {
        return filters
            .fold(names) { acc, filter ->
                filter.filterBatch(acc, context.filterContext)
            }
            .map { name ->
                name to createPassedFilteringSteps()
            }
            .also { sequence ->
                logProgress(sequence, context.verbose)
            }
    }

    private fun logProgress(
        sequence: Sequence<Pair<GeneratedName, List<FilteringStep>>>,
        verbose: Boolean
    ): Sequence<Pair<GeneratedName, List<FilteringStep>>> {
        if (!verbose) return sequence

        var count = 0
        return sequence.onEach {
            count++
            if (count % 10000 == 0) {
                logger.v("처리 중: ${count}개 완료")
            }
        }
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

    private data class FilteringContext(
        val names: Sequence<GeneratedName>,
        val filterContext: FilterContext,
        val sajuInfo: SajuAnalysisInfo,
        val verbose: Boolean,
        val withoutFilter: Boolean
    ) {
        companion object {
            fun builder() = Builder()
        }

        class Builder {
            private lateinit var names: Sequence<GeneratedName>
            private lateinit var filterContext: FilterContext
            private lateinit var sajuInfo: SajuAnalysisInfo
            private var verbose: Boolean = false
            private var withoutFilter: Boolean = false

            fun withNames(names: Sequence<GeneratedName>) = apply { this.names = names }
            fun withFilterContext(context: FilterContext) = apply { this.filterContext = context }
            fun withSajuInfo(info: SajuAnalysisInfo) = apply { this.sajuInfo = info }
            fun withVerbose(verbose: Boolean) = apply { this.verbose = verbose }
            fun withoutFilter(without: Boolean) = apply { this.withoutFilter = without }

            fun build() = FilteringContext(names, filterContext, sajuInfo, verbose, withoutFilter)
        }
    }
}