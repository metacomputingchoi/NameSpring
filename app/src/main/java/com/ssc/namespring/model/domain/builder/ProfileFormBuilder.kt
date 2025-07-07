// model/domain/builder/ProfileFormBuilder.kt
package com.ssc.namespring.model.domain.builder

import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFactory
import com.ssc.namespring.model.domain.usecase.profileform.NamingEngineInput
import com.ssc.namespring.model.presentation.components.ProfileFormUiState
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

/**
 * Profile 및 NamingEngine 입력 생성을 담당하는 빌더
 */
class ProfileFormBuilder(
    private val profileFactory: ProfileFactory = ProfileFactory()
) {

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

        Log.d(TAG, "Creating profile with name: $profileName")
        logGivenNameInfo(givenNameInfo)

        val profile = profileFactory.createProfile(
            profileId = profileId,
            profileName = profileName,
            birthDate = birthDate,
            isYajaTime = isYajaTime,
            surname = surname,
            givenName = givenNameInfo,
            existingProfile = existingProfile
        )

        Log.d(TAG, "Created profile: surname=${profile.surname?.korean}(${profile.surname?.hanja}), " +
                "givenName=${profile.givenName?.korean}(${profile.givenName?.hanja})")

        return profile
    }

    fun buildNamingInput(
        surname: SurnameInfo,
        uiState: ProfileFormUiState,
        calendar: Calendar,
        isYajaTime: Boolean
    ): NamingEngineInput {

        var userInput = "[${surname.korean}/${surname.hanja}]"

        Log.d(TAG, "=== buildNamingInput Debug ===")
        Log.d(TAG, "Surname: ${surname.korean}/${surname.hanja}")
        Log.d(TAG, "UI State CharDataList size: ${uiState.nameCharDataList.size}")

        uiState.nameCharDataList.forEachIndexed { index, charData ->
            Log.d(TAG, "UI CharData[$index]: korean='${charData.korean}', hanja='${charData.hanja}'")

            val korean = if (charData.korean.isNotEmpty()) charData.korean else "_"
            val hanja = if (charData.hanja.isNotEmpty()) charData.hanja else "_"
            userInput += "[$korean/$hanja]"
        }

        Log.d(TAG, "Final userInput: $userInput")

        return NamingEngineInput(
            userInput = userInput,
            birthDateTime = convertToLocalDateTime(calendar),
            useYajasi = isYajaTime
        )
    }

    fun buildEvaluationInput(
        surname: SurnameInfo,
        givenNameInfo: GivenNameInfo?,
        calendar: Calendar,
        isYajaTime: Boolean
    ): NamingEngineInput {

        val userInput = buildUserInput(surname, givenNameInfo)

        return NamingEngineInput(
            userInput = userInput,
            birthDateTime = convertToLocalDateTime(calendar),
            useYajasi = isYajaTime
        )
    }

    private fun buildUserInput(surname: SurnameInfo, givenNameInfo: GivenNameInfo?): String {
        val surnameInput = "[${surname.korean}/${surname.hanja}]"

        val givenNameInput = givenNameInfo?.charInfos?.joinToString("") { charInfo ->
            if (charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()) {
                "[${charInfo.korean}/${charInfo.hanja}]"
            } else ""
        } ?: ""

        return surnameInput + givenNameInput
    }

    private fun convertToLocalDateTime(calendar: Calendar): LocalDateTime {
        return calendar.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    private fun logGivenNameInfo(givenNameInfo: GivenNameInfo?) {
        if (givenNameInfo != null) {
            Log.d(TAG, "GivenNameInfo:")
            Log.d(TAG, "  Korean: '${givenNameInfo.korean}'")
            Log.d(TAG, "  Hanja: '${givenNameInfo.hanja}'")
            givenNameInfo.charInfos.forEachIndexed { index, charInfo ->
                Log.d(TAG, "  CharInfo[$index]: korean='${charInfo.korean}', hanja='${charInfo.hanja}'")
            }
        } else {
            Log.d(TAG, "  GivenNameInfo is null")
        }
    }
}
