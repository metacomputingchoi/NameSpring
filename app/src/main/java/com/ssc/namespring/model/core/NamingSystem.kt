// model/core/NamingSystem.kt
package com.ssc.namespring.model.core

import com.ssc.namespring.model.common.naming.NamingCalculationConstants
import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.filter.*
import com.ssc.namespring.model.util.logger.Logger
import com.ssc.namespring.model.util.logger.PrintLogger
import com.ssc.namespring.model.repository.DataRepository
import com.ssc.namespring.model.repository.HanjaRepository
import com.ssc.namespring.model.service.*

// 피드백: 싱글톤 패턴 제거, 생성자 주입으로 변경
class NamingSystem(
    private val logger: Logger = PrintLogger(ParsingConstants.LOG_TAG)  // 기본값으로 PrintLogger 제공
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
            nameGenerator = NameGenerator(hanjaRepository, nameSuriAnalyzer)
            analysisInfoGenerator = AnalysisInfoGenerator(baleumOhaengCalculator, multiOhaengHarmonyAnalyzer)

            logger.d("NamingSystem initialized successfully")
        } catch (e: Exception) {
            logger.e("Failed to initialize NamingSystem", e)
            throw e
        }
    }

    fun generateKoreanNames(
        userInput: String,
        birthYear: Int,
        birthMonth: Int,
        birthDay: Int,
        birthHour: Int,
        birthMinute: Int,
        birthSecond: Int = 0,
        useYajasi: Boolean = true,
        verbose: Boolean = false
    ): List<GeneratedName> {
        return try {
            val parsed = nameParser.parseNameInput(userInput)
            if (verbose) logger.v("파싱된 입력: $parsed")

            val surnameCandidates = findSurnameCandidates(parsed)
            if (surnameCandidates.isEmpty()) {
                throw NamingException.InvalidInputException(ParsingConstants.ErrorMessages.INVALID_SURNAME)
            }

            val fourPillars = sajuCalculator.getSaju(
                birthYear, birthMonth, birthDay, birthHour, birthMinute, birthSecond, useYajasi
            )
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
                processSurnameCandidate(candidate, sajuOhaengCount, sajuInfo, verbose)
            }

        } catch (e: NamingException) {
            logger.e("Name generation failed", e)
            if (verbose) e.printStackTrace()
            throw e
        } catch (e: Exception) {
            logger.e("Unexpected error", e)
            if (verbose) e.printStackTrace()
            throw e
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
        verbose: Boolean
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
        }

        val results = nameGenerator.generateNames(
            surHangul, surHanja, nameConstraints, nameLength, sajuOhaengCount
        )

        return applyFiltersWithBatch(results, surHangul, surLength, nameLength, sajuOhaengCount, sajuInfo, verbose)
    }

    private fun applyFiltersWithBatch(
        names: Sequence<GeneratedName>,
        surHangul: String,
        surLength: Int,
        nameLength: Int,
        sajuOhaengCount: Map<String, Int>,
        sajuInfo: SajuAnalysisInfo,
        verbose: Boolean
    ): List<GeneratedName> {
        val context = FilterContext(surHangul, surLength, nameLength, sajuOhaengCount)

        var filteredNames = names

        // 각 필터를 순차적으로 적용
        filters.forEach { filter ->
            filteredNames = filter.filterBatch(filteredNames, context)
        }

        // 결과를 리스트로 수집
        val results = mutableListOf<GeneratedName>()
        var count = 0

        for (name in filteredNames) {
            results.add(name)
            count++

            // 주기적으로 로그 출력
            if (verbose && count % 10000 == 0) {
                logger.v("처리 중: ${count}개 필터링 완료")
            }
        }

        if (verbose) logger.v("필터링 완료: ${results.size}개")

        // 분석 정보 추가 (선택적)
        return results.map { name ->
            name.apply {
                analysisInfo = analysisInfoGenerator.generateAnalysisInfo(name, sajuInfo)
            }
        }
    }
}
