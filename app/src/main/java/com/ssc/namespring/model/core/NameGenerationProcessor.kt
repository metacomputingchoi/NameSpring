// model/core/NameGenerationProcessor.kt
package com.ssc.namespring.model.core

import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.service.NameGenerator
import com.ssc.namespring.model.service.NameParser
import com.ssc.namespring.model.service.SajuCalculator
import com.ssc.namespring.model.util.logger.Logger
import java.time.LocalDateTime

internal class NameGenerationProcessor(
    private val services: Services,
    private val surnameProcessor: SurnameProcessor,
    private val filteringProcessor: FilteringProcessor,
    private val logger: Logger
) {
    fun generateNames(
        userInput: String,
        birthDateTime: LocalDateTime,
        useYajasi: Boolean,
        verbose: Boolean,
        withoutFilter: Boolean
    ): List<GeneratedName> {
        try {
            // 1. 입력 파싱
            val parsed = services.nameParser.parseNameInput(userInput)
            if (verbose) logger.v("파싱된 입력: $parsed")

            // 2. 성씨 후보 찾기
            val surnameCandidates = surnameProcessor.findSurnameCandidates(parsed)
            if (surnameCandidates.isEmpty()) {
                throw NamingException.InvalidInputException(
                    ParsingConstants.ErrorMessages.INVALID_SURNAME,
                    input = userInput
                )
            }

            // 3. 사주 계산
            val fourPillars = services.sajuCalculator.getSaju(birthDateTime, useYajasi)
            val sajuOhaengCount = services.sajuCalculator.getSajuOhaengCount(
                fourPillars[0], fourPillars[1], fourPillars[2], fourPillars[3]
            )
            val sajuInfo = services.sajuCalculator.analyzeSaju(fourPillars, sajuOhaengCount)

            if (verbose) {
                logger.v("사주: ${fourPillars.joinToString(", ")}")
                logger.v("사주 오행: $sajuOhaengCount")
                logger.v("부족한 오행: ${sajuInfo.missingElements}")
            }

            // 4. 각 성씨 후보에 대해 이름 생성
            return surnameCandidates.flatMap { candidate ->
                processSurnameCandidate(
                    candidate,
                    sajuOhaengCount,
                    sajuInfo,
                    verbose,
                    withoutFilter
                )
            }

        } catch (e: NamingException) {
            logger.e("Name generation failed", e)
            if (verbose) e.printStackTrace()
            throw e
        } catch (e: Exception) {
            logger.e("Unexpected error", e)
            if (verbose) e.printStackTrace()
            throw NamingException.ConfigurationException(
                "이름 생성 중 예기치 않은 오류 발생",
                cause = e
            )
        }
    }

    private fun processSurnameCandidate(
        candidate: SurnameProcessor.SurnameCandidate,
        sajuOhaengCount: Map<String, Int>,
        sajuInfo: SajuAnalysisInfo,
        verbose: Boolean,
        withoutFilter: Boolean
    ): List<GeneratedName> {
        // 이름 길이 검증
        if (!surnameProcessor.validateNameLength(candidate, verbose)) {
            return emptyList()
        }

        val nameConstraints = services.nameParser.extractConstraintsFromInput(candidate.nameParts)
        val surLength = candidate.surHanja.length
        val nameLength = candidate.nameParts.size

        if (verbose) {
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
            sajuOhaengCount,
            requireMinScore = !withoutFilter
        )

        if (verbose) {
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
            sajuOhaengCount,
            sajuInfo,
            verbose,
            withoutFilter
        )
    }
}