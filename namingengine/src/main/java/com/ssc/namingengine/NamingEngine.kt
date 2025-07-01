// NamingEngine.kt
package com.ssc.namingengine

import com.ssc.namingengine.core.NamingSystem
import com.ssc.namingengine.core.NamingSystemConfig
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.data.ValidationResult
import com.ssc.namingengine.exception.NamingEngineException
import com.ssc.namingengine.exception.NamingException
import com.ssc.namingengine.util.logger.Logger
import com.ssc.namingengine.util.logger.PrintLogger
import java.time.LocalDateTime

class NamingEngine private constructor(
    private val namingSystem: NamingSystem
) {

    fun generateNames(
        userInput: String,
        birthDateTime: LocalDateTime,
        useYajasi: Boolean = true,
        verbose: Boolean = false,
        withoutFilter: Boolean = false
    ): List<GeneratedName> {
        return try {
            namingSystem.generateKoreanNames(
                userInput = userInput,
                birthDateTime = birthDateTime,
                useYajasi = useYajasi,
                verbose = verbose,
                withoutFilter = withoutFilter
            )
        } catch (e: NamingException) {
            throw NamingEngineException("이름 생성 중 오류 발생: ${e.message}", e)
        } catch (e: Exception) {
            throw NamingEngineException("예상치 못한 오류 발생: ${e.message}", e)
        }
    }

    fun validateInput(userInput: String): ValidationResult {
        return try {
            val pattern = "\\[([^/]+)/([^]]+)]".toRegex()
            val matches = pattern.findAll(userInput)

            if (!matches.any()) {
                ValidationResult(
                    isValid = false,
                    errorMessage = "올바른 입력 형식이 아닙니다. 예: [김/金][_/_]"
                )
            } else {
                ValidationResult(isValid = true)
            }
        } catch (e: Exception) {
            ValidationResult(
                isValid = false,
                errorMessage = "입력 검증 중 오류 발생: ${e.message}"
            )
        }
    }

    companion object {
        private const val TAG = "NamingEngine"
        private const val RESOURCE_BASE_PATH = "/namingengine/data"
        const val VERSION = "0.1.0"

        @JvmStatic
        @JvmOverloads
        fun create(logger: Logger? = null): NamingEngine {
            val actualLogger = logger ?: PrintLogger(TAG)

            try {
                val config = loadConfiguration()

                val namingSystem = NamingSystem.builder()
                    .withConfig(config)
                    .withLogger(actualLogger)
                    .build()

                return NamingEngine(namingSystem)

            } catch (e: Exception) {
                throw NamingEngineException("Failed to initialize NamingEngine: ${e.message}", e)
            }
        }

        private fun loadConfiguration(): NamingSystemConfig {
            return NamingSystemConfig(
                ymdJson = loadResource("ymd_data.json"),
                nameCharTripleJson = loadResource("name_char_triple_dict_effective.json"),
                surnameCharTripleJson = loadResource("surname_char_triple_dict.json"),
                nameKoreanToTripleJson = loadResource("name_korean_to_triple_keys_mapping_effective.json"),
                nameHanjaToTripleJson = loadResource("name_hanja_to_triple_keys_mapping_effective.json"),
                surnameKoreanToTripleJson = loadResource("surname_korean_to_triple_keys_mapping.json"),
                surnameHanjaToTripleJson = loadResource("surname_hanja_to_triple_keys_mapping.json"),
                surnameHanjaPairJson = loadResource("surname_hanja_pair_mapping_dict.json"),
                dictHangulGivenNamesJson = loadResource("dict_hangul_given_names.json"),
                surnameChosungToKoreanJson = loadResource("surname_chosung_to_korean_mapping.json")
            )
        }

        private fun loadResource(filename: String): String {
            val path = "$RESOURCE_BASE_PATH/$filename"
            return NamingEngine::class.java.getResourceAsStream(path)?.use { stream ->
                stream.bufferedReader().readText()
            } ?: throw NamingEngineException("Required resource not found: $filename")
        }
    }
}