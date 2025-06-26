// utils/NamingSystemBuilder.kt
package com.ssc.namespring.utils

import com.ssc.namespring.model.core.NamingSystem
import com.ssc.namespring.model.util.logger.Logger
import com.ssc.namespring.model.util.logger.PrintLogger
import com.ssc.namespring.model.common.parsing.ParsingConstants

class NamingSystemBuilder {
    private var logger: Logger? = null
    private var jsonFileLoader: JsonFileLoader? = null

    fun withLogger(logger: Logger): NamingSystemBuilder {
        this.logger = logger
        return this
    }

    fun withJsonFileLoader(loader: JsonFileLoader): NamingSystemBuilder {
        this.jsonFileLoader = loader
        return this
    }

    fun build(): NamingSystem {
        val finalLogger = logger ?: PrintLogger(ParsingConstants.LOG_TAG)
        return NamingSystem(finalLogger)
    }

    fun buildAndInitialize(): NamingSystem {
        requireNotNull(jsonFileLoader) { "JsonFileLoader must be set before initialization" }

        val namingSystem = build()
        val jsonData = jsonFileLoader!!.loadAllJsonFiles()

        namingSystem.initializeFromJson(
            ymdJson = jsonData["ymd"]!!,
            nameCharTripleJson = jsonData["nameCharTriple"]!!,
            surnameCharTripleJson = jsonData["surnameCharTriple"]!!,
            nameKoreanToTripleJson = jsonData["nameKoreanToTriple"]!!,
            nameHanjaToTripleJson = jsonData["nameHanjaToTriple"]!!,
            surnameKoreanToTripleJson = jsonData["surnameKoreanToTriple"]!!,
            surnameHanjaToTripleJson = jsonData["surnameHanjaToTriple"]!!,
            surnameHanjaPairJson = jsonData["surnameHanjaPair"]!!,
            dictHangulGivenNamesJson = jsonData["dictHangulGivenNames"]!!,
            surnameChosungToKoreanJson = jsonData["surnameChosungToKorean"]!!
        )

        return namingSystem
    }
}