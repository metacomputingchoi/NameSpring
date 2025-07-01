// controller/NameGeneratorController.kt
package com.ssc.namespring.controller

import com.ssc.namespring.model.NameGeneratorModel
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.view.NameGeneratorView
import java.time.LocalDateTime

class NameGeneratorController(
    private val model: NameGeneratorModel,
    private val view: NameGeneratorView
) {

    fun generateNames(
        userInput: String,
        birthDateTime: LocalDateTime,
        withoutFilter: Boolean = false
    ) {
        view.showLoading(true)

        val validationResult = model.validateInput(userInput)
        if (!validationResult.isValid) {
            view.showError(validationResult.errorMessage ?: "Unknown error")
            view.showLoading(false)
            return
        }

        try {
            val startTime = System.currentTimeMillis()

            val names = model.generateNames(
                userInput = userInput,
                birthDateTime = birthDateTime,
                withoutFilter = withoutFilter
            )

            val elapsedTime = System.currentTimeMillis() - startTime

            view.showLoading(false)
            view.showResults(names, elapsedTime)

        } catch (e: Exception) {
            view.showLoading(false)
            view.showError(e.message ?: "Unknown error")
        }
    }
}

sealed class GenerateNameResult {
    data class Success(val names: List<GeneratedName>) : GenerateNameResult()
    data class Error(val message: String) : GenerateNameResult()
}