// model/core/NamingSystem.kt
package com.ssc.namespring.model.core

import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.util.logger.Logger
import com.ssc.namespring.model.util.logger.PrintLogger
import java.time.LocalDateTime

class NamingSystem(
    private val logger: Logger = PrintLogger(ParsingConstants.LOG_TAG)
) {
    private var isInitialized = false
    private lateinit var nameGenerationProcessor: NameGenerationProcessor

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
        // 초기화 위임
        val initializer = NamingSystemInitializer(logger)
        val dataRepository = initializer.initialize(
            ymdJson, nameCharTripleJson, surnameCharTripleJson,
            nameKoreanToTripleJson, nameHanjaToTripleJson,
            surnameKoreanToTripleJson, surnameHanjaToTripleJson,
            surnameHanjaPairJson, dictHangulGivenNamesJson,
            surnameChosungToKoreanJson
        )

        // 팩토리를 통한 의존성 생성
        val factory = NamingSystemFactory(dataRepository, logger)
        val services = factory.createServices()
        val filters = factory.createFilters(services)

        // 프로세서 생성
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

        nameGenerationProcessor = NameGenerationProcessor(
            services,
            surnameProcessor,
            filteringProcessor,
            logger
        )

        isInitialized = true
        logger.d("NamingSystem initialized successfully")
    }

    fun generateKoreanNames(
        userInput: String,
        birthDateTime: LocalDateTime,
        useYajasi: Boolean = true,
        verbose: Boolean = false,
        withoutFilter: Boolean = false
    ): List<GeneratedName> {
        require(isInitialized) { "NamingSystem이 초기화되지 않았습니다." }

        return nameGenerationProcessor.generateNames(
            userInput,
            birthDateTime,
            useYajasi,
            verbose,
            withoutFilter
        )
    }
}