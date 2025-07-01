// model/NameGeneratorModel.kt
package com.ssc.namespring.model

import com.ssc.namingengine.NamingEngine
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.data.ValidationResult
import com.ssc.namingengine.exception.NamingEngineException
import java.time.LocalDateTime

class NameGeneratorModel(private val namingEngine: NamingEngine) {

    fun validateInput(input: String): ValidationResult {
        return namingEngine.validateInput(input)
    }

    @Throws(NamingEngineException::class)
    fun generateNames(
        userInput: String,
        birthDateTime: LocalDateTime,
        useYajasi: Boolean = true,
        verbose: Boolean = true,
        withoutFilter: Boolean = false
    ): List<GeneratedName> {
        return namingEngine.generateNames(
            userInput = userInput,
            birthDateTime = birthDateTime,
            useYajasi = useYajasi,
            verbose = verbose,
            withoutFilter = withoutFilter
        )
    }

    fun formatDateTime(dateTime: LocalDateTime): String {
        return "${dateTime.year}년 ${dateTime.monthValue}월 ${dateTime.dayOfMonth}일 " +
                "${if (dateTime.hour < 12) "오전" else "오후"} ${dateTime.hour % 12}시 ${dateTime.minute}분"
    }
}