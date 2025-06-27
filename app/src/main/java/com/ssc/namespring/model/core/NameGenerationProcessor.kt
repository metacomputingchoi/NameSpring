// model/core/NameGenerationProcessor.kt
package com.ssc.namespring.model.core

import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.filter.NameFilterStrategy
import com.ssc.namespring.model.service.NameParser
import com.ssc.namespring.model.service.SajuCalculator
import com.ssc.namespring.model.service.SurnameValidator
import com.ssc.namespring.model.util.logger.Logger
import java.time.LocalDateTime

internal class NameGenerationProcessor private constructor(
    private val services: Services,
    private val surnameProcessor: SurnameProcessor,
    private val filteringProcessor: FilteringProcessor,
    private val logger: Logger
) {

    companion object {
        fun builder() = Builder()
    }

    fun generateNames(
        userInput: String,
        birthDateTime: LocalDateTime,
        useYajasi: Boolean,
        verbose: Boolean,
        withoutFilter: Boolean
    ): List<GeneratedName> {
        return NameGenerationContext.builder()
            .withUserInput(userInput)
            .withBirthDateTime(birthDateTime)
            .withUseYajasi(useYajasi)
            .withVerbose(verbose)
            .withoutFilter(withoutFilter)
            .build()
            .let { context ->
                processWithContext(context)
            }
    }

    private fun processWithContext(context: NameGenerationContext): List<GeneratedName> {
        return try {
            ParsedInputProcessor(services.nameParser, logger)
                .parse(context.userInput, context.verbose)
                .let { parsed ->
                    SurnameProcessor(services.nameParser, services.surnameValidator, logger)
                        .findSurnameCandidates(parsed)
                }
                .also { candidates ->
                    if (candidates.isEmpty()) {
                        throw NamingException.InvalidInputException(
                            ParsingConstants.ErrorMessages.INVALID_SURNAME,
                            input = context.userInput
                        )
                    }
                }
                .let { candidates ->
                    SajuProcessor(services.sajuCalculator, logger)
                        .process(context.birthDateTime, context.useYajasi, context.verbose)
                        .let { sajuData ->
                            candidates.flatMap { candidate ->
                                processSurnameCandidate(
                                    candidate,
                                    sajuData,
                                    context
                                )
                            }
                        }
                }
        } catch (e: NamingException) {
            logger.e("Name generation failed", e)
            if (context.verbose) e.printStackTrace()
            throw e
        } catch (e: Exception) {
            logger.e("Unexpected error", e)
            if (context.verbose) e.printStackTrace()
            throw NamingException.ConfigurationException(
                "이름 생성 중 예기치 않은 오류 발생",
                cause = e
            )
        }
    }

    private fun processSurnameCandidate(
        candidate: SurnameProcessor.SurnameCandidate,
        sajuData: SajuData,
        context: NameGenerationContext
    ): List<GeneratedName> {
        return CandidateProcessor(services, surnameProcessor, filteringProcessor, logger)
            .process(candidate, sajuData, context)
    }

    class Builder {
        private lateinit var services: Services
        private lateinit var filters: List<NameFilterStrategy>
        private lateinit var logger: Logger

        fun withServices(services: Services) = apply { this.services = services }
        fun withFilters(filters: List<NameFilterStrategy>) = apply { this.filters = filters }
        fun withLogger(logger: Logger) = apply { this.logger = logger }

        fun build(): NameGenerationProcessor {
            val surnameProcessor = SurnameProcessor(
                services.nameParser,
                services.surnameValidator,
                logger
            )

            val filteringProcessor = FilteringProcessor(
                filters,
                services.analysisInfoGenerator,
                logger
            )

            return NameGenerationProcessor(
                services,
                surnameProcessor,
                filteringProcessor,
                logger
            )
        }
    }
}

// 추가 데이터 클래스들
internal data class NameGenerationContext(
    val userInput: String,
    val birthDateTime: LocalDateTime,
    val useYajasi: Boolean,
    val verbose: Boolean,
    val withoutFilter: Boolean
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private lateinit var userInput: String
        private lateinit var birthDateTime: LocalDateTime
        private var useYajasi: Boolean = true
        private var verbose: Boolean = false
        private var withoutFilter: Boolean = false

        fun withUserInput(input: String) = apply { this.userInput = input }
        fun withBirthDateTime(dateTime: LocalDateTime) = apply { this.birthDateTime = dateTime }
        fun withUseYajasi(use: Boolean) = apply { this.useYajasi = use }
        fun withVerbose(verbose: Boolean) = apply { this.verbose = verbose }
        fun withoutFilter(without: Boolean) = apply { this.withoutFilter = without }

        fun build() = NameGenerationContext(
            userInput, birthDateTime, useYajasi, verbose, withoutFilter
        )
    }
}

internal data class SajuData(
    val fourPillars: Array<String>,
    val sajuOhaengCount: Map<String, Int>,
    val sajuInfo: SajuAnalysisInfo
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SajuData

        if (!fourPillars.contentEquals(other.fourPillars)) return false
        if (sajuOhaengCount != other.sajuOhaengCount) return false
        if (sajuInfo != other.sajuInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fourPillars.contentHashCode()
        result = 31 * result + sajuOhaengCount.hashCode()
        result = 31 * result + sajuInfo.hashCode()
        return result
    }
}

// 보조 프로세서들
internal class ParsedInputProcessor(
    private val nameParser: NameParser,
    private val logger: Logger
) {
    fun parse(userInput: String, verbose: Boolean): List<Pair<String, String>> {
        return nameParser.parseNameInput(userInput).also { parsed ->
            if (verbose) logger.v("파싱된 입력: $parsed")
        }
    }
}

internal class SajuProcessor(
    private val sajuCalculator: SajuCalculator,
    private val logger: Logger
) {
    fun process(
        birthDateTime: LocalDateTime,
        useYajasi: Boolean,
        verbose: Boolean
    ): SajuData {
        val fourPillars = sajuCalculator.getSaju(birthDateTime, useYajasi)
        val sajuOhaengCount = sajuCalculator.getSajuOhaengCount(
            fourPillars[0], fourPillars[1], fourPillars[2], fourPillars[3]
        )
        val sajuInfo = sajuCalculator.analyzeSaju(fourPillars, sajuOhaengCount)

        if (verbose) {
            logger.v("사주: ${fourPillars.joinToString(", ")}")
            logger.v("사주 오행: $sajuOhaengCount")
            logger.v("부족한 오행: ${sajuInfo.missingElements}")
        }

        return SajuData(fourPillars, sajuOhaengCount, sajuInfo)
    }
}

internal class CandidateProcessor(
    private val services: Services,
    private val surnameProcessor: SurnameProcessor,
    private val filteringProcessor: FilteringProcessor,
    private val logger: Logger
) {
    fun process(
        candidate: SurnameProcessor.SurnameCandidate,
        sajuData: SajuData,
        context: NameGenerationContext
    ): List<GeneratedName> {
        // 이름 길이 검증
        if (!surnameProcessor.validateNameLength(candidate, context.verbose)) {
            return emptyList()
        }

        val nameConstraints = services.nameParser.extractConstraintsFromInput(candidate.nameParts)
        val surLength = candidate.surHanja.length
        val nameLength = candidate.nameParts.size

        if (context.verbose) {
            logger.v("성: ${candidate.surHangul}(${candidate.surHanja})")
            logger.v("이름 길이: ${nameLength}글자")
            logger.v("이름 제약조건: ${nameConstraints.map {
                "${it.hangulType}:${it.hangulValue}/${it.hanjaType}:${it.hanjaValue}"
            }}")
        }

        // 이름 생성
        val results = services.nameGenerator.generateNames(
            candidate.surHangul,
            candidate.surHanja,
            nameConstraints,
            nameLength,
            sajuData.sajuOhaengCount,
            requireMinScore = !context.withoutFilter
        )

        if (context.verbose) {
            var count = 0
            for (name in results) {
                count++
                if (count == 1) {
                    logger.v("첫 번째 생성된 이름: ${name.combinedHanja}")
                }
            }
            if (count == 0) {
                logger.v("nameGenerator에서 생성된 이름이 없음")
            }
        }

        // 필터링 처리
        return filteringProcessor.processNamesWithFilters(
            results,
            candidate.surHangul,
            surLength,
            nameLength,
            sajuData.sajuOhaengCount,
            sajuData.sajuInfo,
            context.verbose,
            context.withoutFilter
        )
    }
}