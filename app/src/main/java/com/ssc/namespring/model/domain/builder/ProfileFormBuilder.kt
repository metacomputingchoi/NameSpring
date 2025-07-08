// model/domain/builder/ProfileFormBuilder.kt
package com.ssc.namespring.model.domain.builder

import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFactory
import com.ssc.namespring.model.domain.usecase.profileform.NamingEngineInput
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import java.util.Calendar

/**
 * Profile 및 NamingEngine 입력 생성을 담당하는 빌더
 * Facade 패턴을 사용하여 복잡한 생성 로직을 단순화
 */
class ProfileFormBuilder(
    profileFactory: ProfileFactory = ProfileFactory()
) {

    private val profileBuilder = ProfileBuilder(profileFactory)
    private val namingInputBuilder = NamingInputBuilder()

    companion object {
        private const val TAG = "ProfileFormBuilder"
    }

    fun buildProfile(
        profileId: String?,
        profileName: String,
        birthDate: Calendar,
        isYajaTime: Boolean,
        surname: SurnameInfo?,
        givenNameInfo: GivenNameInfo?,
        existingProfile: Profile?
    ): Profile {
        return profileBuilder.build(
            profileId = profileId,
            profileName = profileName,
            birthDate = birthDate,
            isYajaTime = isYajaTime,
            surname = surname,
            givenNameInfo = givenNameInfo,
            existingProfile = existingProfile
        )
    }

    fun buildNamingInput(
        surname: SurnameInfo,
        uiState: ProfileFormUiState,
        calendar: Calendar,
        isYajaTime: Boolean
    ): NamingEngineInput? {
        return namingInputBuilder.buildForNaming(
            surname = surname,
            uiState = uiState,
            calendar = calendar,
            isYajaTime = isYajaTime
        )
    }

    fun buildEvaluationInput(
        surname: SurnameInfo,
        givenNameInfo: GivenNameInfo?,
        calendar: Calendar,
        isYajaTime: Boolean
    ): NamingEngineInput? {
        return namingInputBuilder.buildForEvaluation(
            surname = surname,
            givenNameInfo = givenNameInfo,
            calendar = calendar,
            isYajaTime = isYajaTime
        )
    }
}