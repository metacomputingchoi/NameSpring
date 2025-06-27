// model/core/NamingSystemInitializer.kt
package com.ssc.namespring.model.core

import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.repository.DataRepository
import com.ssc.namespring.model.util.logger.Logger

internal class NamingSystemInitializer(
    private val logger: Logger
) {
    fun initialize(
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
    ): DataRepository {
        try {
            val dataRepository = DataRepository().apply {
                loadFromJson(
                    ymdJson, nameCharTripleJson, surnameCharTripleJson,
                    nameKoreanToTripleJson, nameHanjaToTripleJson,
                    surnameKoreanToTripleJson, surnameHanjaToTripleJson,
                    surnameHanjaPairJson, dictHangulGivenNamesJson,
                    surnameChosungToKoreanJson
                )
            }

            logger.d("DataRepository initialized successfully")
            return dataRepository

        } catch (e: Exception) {
            logger.e("Failed to initialize DataRepository", e)
            throw NamingException.ConfigurationException(
                "데이터 저장소 초기화 실패",
                cause = e
            )
        }
    }
}