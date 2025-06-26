// model/core/NamingSystem.kt
package com.ssc.namespring.model.core

import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.FilteringStep
import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.filter.*
import com.ssc.namespring.model.util.logger.Logger
import com.ssc.namespring.model.util.logger.PrintLogger
import com.ssc.namespring.model.repository.DataRepository
import com.ssc.namespring.model.repository.HanjaRepository
import com.ssc.namespring.model.service.*
import java.time.LocalDateTime

class NamingSystem(
    private val logger: Logger = PrintLogger(ParsingConstants.LOG_TAG)
) {
    private lateinit var dataRepository: DataRepository
    private lateinit var hanjaRepository: HanjaRepository

    private lateinit var nameParser: NameParser
    private lateinit var surnameValidator: SurnameValidator
    private lateinit var sajuCalculator: SajuCalculator
    private lateinit var baleumOhaengCalculator: BaleumOhaengCalculator
    private lateinit var multiOhaengHarmonyAnalyzer: MultiOhaengHarmonyAnalyzer
    private lateinit var hanjaHoeksuAnalyzer: HanjaHoeksuAnalyzer
    private lateinit var nameGenerator: NameGenerator
    private lateinit var nameSuriAnalyzer: NameSuriAnalyzer
    private lateinit var analysisInfoGenerator: AnalysisInfoGenerator

    private val filters: List<NameFilterStrategy> by lazy {
        listOf(
            BaleumOhaengEumyangFilter(
                baleumOhaengCalculator::getBaleumOhaeng,
                baleumOhaengCalculator::getBaleumEumyang,
                multiOhaengHarmonyAnalyzer::checkBaleumOhaengHarmony
            ),
            JawonOhaengFilter(),
            BaleumNaturalFilter { dataRepository.dictHangulGivenNames }
        )
    }

    fun initializeFromJson(
        ymdJson: String,
        nameCharTripleJson: String,
        surnameCharTripleJson: String,
        nameKoreanToTripleJson: String,
        nameHanjaToTripleJson: String,
        surnameKoreanToTripleJson: String,
        surnameHanjaToTripleJson: String,
        surnameHanjaPairJson: String,
        dictHangulGivenNamesJson: String,
        surnameChosungToKoreanJson: String
    ) {
        try {
            dataRepository = DataRepository().apply {
                loadFromJson(
                    ymdJson, nameCharTripleJson, surnameCharTripleJson,
                    nameKoreanToTripleJson, nameHanjaToTripleJson,
                    surnameKoreanToTripleJson, surnameHanjaToTripleJson,
                    surnameHanjaPairJson, dictHangulGivenNamesJson,
                    surnameChosungToKoreanJson
                )
            }

            hanjaRepository = HanjaRepository(dataRepository)

            val cacheManager = CacheManager()
            nameParser = NameParser()
            surnameValidator = SurnameValidator(dataRepository)
            sajuCalculator = SajuCalculator(dataRepository)
            baleumOhaengCalculator = BaleumOhaengCalculator(cacheManager)
            multiOhaengHarmonyAnalyzer = MultiOhaengHarmonyAnalyzer(cacheManager)
            hanjaHoeksuAnalyzer = HanjaHoeksuAnalyzer(dataRepository, hanjaRepository)
            nameSuriAnalyzer = NameSuriAnalyzer(hanjaHoeksuAnalyzer, multiOhaengHarmonyAnalyzer)

            nameGenerator = NameGenerator(
                hanjaRepository,
                nameSuriAnalyzer,
                hanjaHoeksuAnalyzer,
                multiOhaengHarmonyAnalyzer
            )

            analysisInfoGenerator = AnalysisInfoGenerator(baleumOhaengCalculator, multiOhaengHarmonyAnalyzer)

            logger.d("NamingSystem initialized successfully")
        } catch (e: Exception) {
            logger.e("Failed to initialize NamingSystem", e)
            throw NamingException.ConfigurationException(
                "시스템 초기화 실패",
                cause = e
            )
        }
    }

    fun generateKoreanNames(
        userInput: String,
        birthDateTime: LocalDateTime,
        useYajasi: Boolean = true,
        verbose: Boolean = false,
        withoutFilter: Boolean = false
    ): List<GeneratedName> {
        return try {
            val parsed = nameParser.parseNameInput(userInput)
            if (verbose) logger.v("파싱된 입력: $parsed")

            val surnameCandidates = findSurnameCandidates(parsed)
            if (surnameCandidates.isEmpty()) {
                throw NamingException.InvalidInputException(
                    ParsingConstants.ErrorMessages.INVALID_SURNAME,
                    input = userInput
                )
            }

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

            surnameCandidates.flatMap { candidate ->
                processSurnameCandidate(candidate, sajuOhaengCount, sajuInfo, verbose, withoutFilter)
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

    private fun findSurnameCandidates(parsed: List<Pair<String, String>>): List<Map<String, Any>> {
        return (1..minOf(2, parsed.size)).mapNotNull { i ->
            val surnameParts = parsed.take(i)
            val nameParts = parsed.drop(i)

            val validation = surnameValidator.validateSurname(surnameParts)
            if (validation.first) {
                mapOf(
                    "surnameParts" to surnameParts,
                    "nameParts" to nameParts,
                    "surHangul" to validation.second!!,
                    "surHanja" to validation.third!!
                )
            } else null
        }
    }

    private fun processSurnameCandidate(
        candidate: Map<String, Any>,
        sajuOhaengCount: Map<String, Int>,
        sajuInfo: SajuAnalysisInfo,
        verbose: Boolean,
        withoutFilter: Boolean
    ): List<GeneratedName> {
        val nameParts = candidate["nameParts"] as List<Pair<String, String>>
        val surHangul = candidate["surHangul"] as String
        val surHanja = candidate["surHanja"] as String

        if (!nameParser.validateNameLengthConstraint(nameParts)) {
            if (verbose) logger.v("${ParsingConstants.ErrorMessages.NAME_LENGTH_CONSTRAINT}$surHangul")
            return emptyList()
        }

        val nameConstraints = nameParser.extractConstraintsFromInput(nameParts)
        val surLength = surHanja.length
        val nameLength = nameParts.size

        if (verbose) {
            logger.v("성: $surHangul($surHanja)")
            logger.v("이름 길이: ${nameLength}글자")
            logger.v("이름 제약조건: ${nameConstraints.map { "${it.hangulType}:${it.hangulValue}/${it.hanjaType}:${it.hanjaValue}" }}")
        }

        val results = nameGenerator.generateNames(
            surHangul, surHanja, nameConstraints, nameLength, sajuOhaengCount,
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

        return processNamesWithFilters(
            results, surHangul, surLength, nameLength, sajuOhaengCount, sajuInfo, verbose, withoutFilter
        )
    }

    private fun processNamesWithFilters(
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
            // 분석 정보 추가
            val filteringSteps = if (withoutFilter) {
                filteringStepsByName[name] ?: emptyList()
            } else {
                // 필터링 모드에서는 모든 필터를 통과했다는 정보 생성
                filters.map { filter ->
                    FilteringStep(
                        filterName = when (filter) {
                            is BaleumOhaengEumyangFilter -> "발음오행음양필터"
                            is JawonOhaengFilter -> "자원오행필터"
                            is BaleumNaturalFilter -> "발음자연스러움필터"
                            else -> "알수없는필터"
                        },
                        passed = true,
                        reason = "필터 통과",
                        details = emptyMap()
                    )
                }
            }

            name.analysisInfo = analysisInfoGenerator.generateAnalysisInfo(name, sajuInfo, filteringSteps)
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
}