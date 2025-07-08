// model/domain/builder/NamingInputBuilder.kt
package com.ssc.namespring.model.domain.builder

import android.util.Log
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.usecase.profileform.NamingEngineInput
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import java.util.Calendar

/**
 * NamingEngine 입력 생성을 담당하는 빌더
 */
internal class NamingInputBuilder(
    private val userInputFormatter: UserInputFormatter = UserInputFormatter(),
    private val dateTimeConverter: DateTimeConverter = DateTimeConverter()
) {

    companion object {
        private const val TAG = "NamingInputBuilder"
    }

    fun buildForNaming(
        surname: SurnameInfo,
        uiState: ProfileFormUiState,
        calendar: Calendar,
        isYajaTime: Boolean
    ): NamingEngineInput? {

        if (surname.korean.isEmpty()) {
            Log.w(TAG, "No surname Korean for naming")
            return null
        }

        val userInput = userInputFormatter.formatForNaming(surname, uiState)

        return NamingEngineInput(
            userInput = userInput,
            birthDateTime = dateTimeConverter.convert(calendar),
            useYajasi = isYajaTime
        )
    }

    fun buildForEvaluation(
        surname: SurnameInfo,
        givenNameInfo: GivenNameInfo?,
        calendar: Calendar,
        isYajaTime: Boolean
    ): NamingEngineInput? {

        if (givenNameInfo == null) {
            Log.w(TAG, "No given name info for evaluation")
            return null
        }

        val userInput = userInputFormatter.formatForEvaluation(surname, givenNameInfo)

        return NamingEngineInput(
            userInput = userInput,
            birthDateTime = dateTimeConverter.convert(calendar),
            useYajasi = isYajaTime
        )
    }
}