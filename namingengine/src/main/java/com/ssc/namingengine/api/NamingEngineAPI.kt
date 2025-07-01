// api/NamingEngineAPI.kt
package com.ssc.namingengine.api

import com.ssc.namingengine.core.NamingSystem
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.exception.NamingException
import java.time.LocalDateTime

class NamingEngineAPI(
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
            throw ApiException.fromNamingException(e)
        } catch (e: Exception) {
            throw ApiException.fromException(e)
        }
    }

    fun validateInput(userInput: String): ValidationResult {
        return try {
            // 기본적인 패턴 검증
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

    fun getApiInfo(): ApiInfo {
        return ApiInfo(
            version = "0.1.0",
            capabilities = listOf(
                "generateNames",
                "validateInput"
            )
        )
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

data class ApiInfo(
    val version: String,
    val capabilities: List<String>
)