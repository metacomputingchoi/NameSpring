// model/core/NamingSystemInitializer.kt
package com.ssc.namespring.model.core

import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.repository.DataRepository
import com.ssc.namespring.model.util.logger.Logger

internal class NamingSystemInitializer private constructor(
    private val logger: Logger,
    private val config: NamingSystemConfig
) {

    companion object {
        fun builder() = Builder()
    }

    fun initialize(): DataRepository {
        return try {
            DataRepository()
                .apply {
                    loadFromJson(
                        config.ymdJson,
                        config.nameCharTripleJson,
                        config.surnameCharTripleJson,
                        config.nameKoreanToTripleJson,
                        config.nameHanjaToTripleJson,
                        config.surnameKoreanToTripleJson,
                        config.surnameHanjaToTripleJson,
                        config.surnameHanjaPairJson,
                        config.dictHangulGivenNamesJson,
                        config.surnameChosungToKoreanJson
                    )
                }
                .also { logger.d("DataRepository initialized successfully") }
        } catch (e: Exception) {
            logger.e("Failed to initialize DataRepository", e)
            throw NamingException.ConfigurationException(
                "데이터 저장소 초기화 실패",
                cause = e
            )
        }
    }

    class Builder {
        private lateinit var logger: Logger
        private lateinit var config: NamingSystemConfig

        fun withLogger(logger: Logger) = apply { this.logger = logger }
        fun withConfig(config: NamingSystemConfig) = apply { this.config = config }

        fun build() = NamingSystemInitializer(logger, config)
    }
}