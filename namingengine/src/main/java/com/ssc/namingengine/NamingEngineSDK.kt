// NamingEngineSDK.kt
package com.ssc.namingengine

import android.content.Context
import com.ssc.namingengine.core.NamingSystem
import com.ssc.namingengine.core.NamingSystemConfig
import com.ssc.namingengine.util.logger.Logger
import com.ssc.namingengine.util.logger.PrintLogger

object NamingEngineSDK {

    private const val TAG = "NamingEngine"

    @JvmStatic
    @JvmOverloads
    fun create(
        context: Context,
        logger: Logger? = null
    ): NamingSystem {
        val assetManager = context.assets
        val actualLogger = logger ?: PrintLogger(TAG)

        try {
            val config = NamingSystemConfig(
                ymdJson = assetManager.open("ymd_data.json").bufferedReader().use { it.readText() },
                nameCharTripleJson = assetManager.open("name_char_triple_dict_effective.json").bufferedReader().use { it.readText() },
                surnameCharTripleJson = assetManager.open("surname_char_triple_dict.json").bufferedReader().use { it.readText() },
                nameKoreanToTripleJson = assetManager.open("name_korean_to_triple_keys_mapping_effective.json").bufferedReader().use { it.readText() },
                nameHanjaToTripleJson = assetManager.open("name_hanja_to_triple_keys_mapping_effective.json").bufferedReader().use { it.readText() },
                surnameKoreanToTripleJson = assetManager.open("surname_korean_to_triple_keys_mapping.json").bufferedReader().use { it.readText() },
                surnameHanjaToTripleJson = assetManager.open("surname_hanja_to_triple_keys_mapping.json").bufferedReader().use { it.readText() },
                surnameHanjaPairJson = assetManager.open("surname_hanja_pair_mapping_dict.json").bufferedReader().use { it.readText() },
                dictHangulGivenNamesJson = assetManager.open("dict_hangul_given_names.json").bufferedReader().use { it.readText() },
                surnameChosungToKoreanJson = assetManager.open("surname_chosung_to_korean_mapping.json").bufferedReader().use { it.readText() }
            )

            val namingSystem = NamingSystem.builder()
                .withConfig(config)
                .withLogger(actualLogger)
                .build()

            return namingSystem

        } catch (e: Exception) {
            throw NamingEngineException("Failed to initialize NamingEngine: ${e.message}", e)
        }
    }

    @JvmStatic
    fun getVersion(): String = "0.1.0"
}

class NamingEngineException(message: String, cause: Throwable? = null) : Exception(message, cause)