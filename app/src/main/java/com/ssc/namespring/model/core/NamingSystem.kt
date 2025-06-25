// model/core/NamingSystem.kt
package com.ssc.namespring.model.core

import com.ssc.namespring.model.Constants
import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.filter.*
import com.ssc.namespring.model.logger.AndroidLogger
import com.ssc.namespring.model.logger.Logger
import com.ssc.namespring.model.repository.DataRepository
import com.ssc.namespring.model.repository.HanjaRepository
import com.ssc.namespring.model.service.*
import com.ssc.namespring.model.util.normalizeNFC

class NamingSystem private constructor() {

    companion object {
        val instance by lazy { NamingSystem() }
    }

    private val logger: Logger = AndroidLogger(Constants.LOG_TAG)

    // Repositories
    private lateinit var dataRepository: DataRepository
    private lateinit var hanjaRepository: HanjaRepository

    // Services
    private lateinit var nameParser: NameParser
    private lateinit var surnameValidator: SurnameValidator
    private lateinit var fourPillarsCalculator: FourPillarsCalculator
    private lateinit var elementCalculator: ElementCalculator
    private lateinit var harmonyAnalyzer: HarmonyAnalyzer
    private lateinit var strokeAnalyzer: StrokeAnalyzer
    private lateinit var nameGenerator: NameGenerator
    private lateinit var nameCombinationAnalyzer: NameCombinationAnalyzer

    // Filters
    private val filters: List<NameFilterStrategy> by lazy {
        listOf(
            ElementsAndYinYangFilter(
                elementCalculator::getHangulElement,
                elementCalculator::getHangulPn,
                harmonyAnalyzer::isHarmoniousElementCombination
            ),
            JawonOhengFilter(),
            HangulNaturalFilter { dataRepository.dictHangulGivenNames }
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
            // Initialize repositories
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

            // Initialize services
            val cacheManager = CacheManager()
            nameParser = NameParser()
            surnameValidator = SurnameValidator(dataRepository)
            fourPillarsCalculator = FourPillarsCalculator(dataRepository)
            elementCalculator = ElementCalculator(cacheManager)
            harmonyAnalyzer = HarmonyAnalyzer(cacheManager)
            strokeAnalyzer = StrokeAnalyzer(dataRepository, hanjaRepository)
            nameCombinationAnalyzer = NameCombinationAnalyzer(strokeAnalyzer, harmonyAnalyzer)
            nameGenerator = NameGenerator(hanjaRepository, nameCombinationAnalyzer)

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
                throw NamingException.InvalidInputException(Constants.ErrorMessages.INVALID_SURNAME)
            }

            val fourPillars = fourPillarsCalculator.get4ju(
                birthYear, birthMonth, birthDay, birthHour, birthMinute, birthSecond, useYajasi
            )
            val dictElementsCount = fourPillarsCalculator.getDictElementsCount(
                fourPillars[0], fourPillars[1], fourPillars[2], fourPillars[3]
            )
            if (verbose) logger.v("사주 오행: $dictElementsCount")

            surnameCandidates.flatMap { candidate ->
                processSurnameCandidate(candidate, dictElementsCount, verbose)
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
        dictElementsCount: Map<String, Int>,
        verbose: Boolean
    ): List<GeneratedName> {
        val nameParts = candidate["nameParts"] as List<Pair<String, String>>
        val surHangul = candidate["surHangul"] as String
        val surHanja = candidate["surHanja"] as String

        if (!nameParser.validateNameLengthConstraint(nameParts)) {
            if (verbose) logger.v("${Constants.ErrorMessages.NAME_LENGTH_CONSTRAINT}$surHangul")
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
            surHangul, surHanja, nameConstraints, nameLength, dictElementsCount
        )

        return applyFilters(results, surHangul, surLength, nameLength, dictElementsCount, verbose)
    }

    private fun applyFilters(
        names: List<GeneratedName>,
        surHangul: String,
        surLength: Int,
        nameLength: Int,
        dictElementsCount: Map<String, Int>,
        verbose: Boolean
    ): List<GeneratedName> {
        if (verbose) logger.v("필터링 전 조합 개수: ${names.size}")

        val context = FilterContext(surHangul, surLength, nameLength, dictElementsCount)

        return filters.fold(names) { currentNames, filter ->
            filter.filter(currentNames, context).also { filtered ->
                if (verbose) {
                    logger.v("${filter.javaClass.simpleName} 후: ${filtered.size}개")
                }
            }
        }
    }
}