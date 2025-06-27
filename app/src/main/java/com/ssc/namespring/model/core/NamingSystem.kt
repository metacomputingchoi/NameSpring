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

    companion object {
        fun builder() = Builder()
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
        val config = NamingSystemConfig(
            ymdJson, nameCharTripleJson, surnameCharTripleJson,
            nameKoreanToTripleJson, nameHanjaToTripleJson,
            surnameKoreanToTripleJson, surnameHanjaToTripleJson,
            surnameHanjaPairJson, dictHangulGivenNamesJson,
            surnameChosungToKoreanJson
        )

        initializeWithConfig(config)
    }

    private fun initializeWithConfig(config: NamingSystemConfig) {
        val initializer = NamingSystemInitializer.builder()
            .withLogger(logger)
            .withConfig(config)
            .build()

        val dataRepository = initializer.initialize()

        val factory = NamingSystemFactory.builder()
            .withDataRepository(dataRepository)
            .withLogger(logger)
            .build()

        val services = factory.createServices()
        val filters = factory.createFilters(services)

        nameGenerationProcessor = NameGenerationProcessor.builder()
            .withServices(services)
            .withFilters(filters)
            .withLogger(logger)
            .build()

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

    class Builder {
        private var logger: Logger = PrintLogger(ParsingConstants.LOG_TAG)
        private var config: NamingSystemConfig? = null

        fun withLogger(logger: Logger) = apply { this.logger = logger }

        fun withJsonData(
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
        ) = apply {
            config = NamingSystemConfig(
                ymdJson, nameCharTripleJson, surnameCharTripleJson,
                nameKoreanToTripleJson, nameHanjaToTripleJson,
                surnameKoreanToTripleJson, surnameHanjaToTripleJson,
                surnameHanjaPairJson, dictHangulGivenNamesJson,
                surnameChosungToKoreanJson
            )
        }

        fun withConfig(config: NamingSystemConfig) = apply {
            this.config = config
        }

        fun build(): NamingSystem {
            val namingSystem = NamingSystem(logger)
            config?.let { namingSystem.initializeWithConfig(it) }
            return namingSystem
        }
    }
}