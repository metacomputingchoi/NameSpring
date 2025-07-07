// model/domain/usecase/profileform/ProfileFormInputFactory.kt
package com.ssc.namespring.model.domain.usecase.profileform

import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.builder.ProfileFormBuilder
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import java.util.Calendar

internal class ProfileFormInputFactory {
    private val profileBuilder = ProfileFormBuilder()

    fun createNamingInput(
        surname: SurnameInfo,
        uiState: ProfileFormUiState,
        calendar: Calendar,
        isYajaTime: Boolean
    ): NamingEngineInput? {
        return profileBuilder.buildNamingInput(
            surname = surname,
            uiState = uiState,
            calendar = calendar,
            isYajaTime = isYajaTime
        )
    }

    fun createEvaluationInput(
        surname: SurnameInfo,
        givenNameInfo: GivenNameInfo?,
        calendar: Calendar,
        isYajaTime: Boolean
    ): NamingEngineInput? {
        return profileBuilder.buildEvaluationInput(
            surname = surname,
            givenNameInfo = givenNameInfo,
            calendar = calendar,
            isYajaTime = isYajaTime
        )
    }
}