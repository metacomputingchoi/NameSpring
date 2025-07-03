// model/utils/ProfileEvaluator.kt
package com.ssc.namespring.model.utils

import com.ssc.namingengine.NamingEngine
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.Profile
import com.ssc.namespring.model.SurnameInfo
import com.ssc.namespring.model.GivenNameInfo
import java.time.LocalDateTime

/**
 * 프로필 평가를 담당하는 유틸리티 클래스
 * NamingEngine을 사용하여 이름을 평가하고 결과를 Profile에 저장
 */
object ProfileEvaluator {

    suspend fun evaluateProfile(
        profile: Profile,
        namingEngine: NamingEngine
    ): GeneratedName? {
        val surname = profile.surname ?: return null
        val givenName = profile.givenName ?: return null

        val nameInput = buildNameInput(surname, givenName)

        return try {
            val evaluatedNames = namingEngine.generateNames(
                userInput = nameInput,
                birthDateTime = convertToLocalDateTime(profile.birthDate),
                useYajasi = profile.isYajaTime,
                verbose = false,
                withoutFilter = true
            )

            evaluatedNames.firstOrNull()?.also { generatedName ->
                profile.updateFromGeneratedName(generatedName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildNameInput(surname: SurnameInfo, givenName: GivenNameInfo): String {
        val surnameInput = "[${surname.korean}/${surname.hanja}]"
        val givenNameInput = givenName.charInfos.joinToString("") { charInfo ->
            "[${charInfo.korean}/${charInfo.hanja}]"
        }
        return surnameInput + givenNameInput
    }

    private fun convertToLocalDateTime(calendar: java.util.Calendar): LocalDateTime {
        return LocalDateTime.of(
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.DAY_OF_MONTH),
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE)
        )
    }
}