// model/utils/ProfileEvaluator.kt
package com.ssc.namespring.model.utils

import com.ssc.namingengine.NamingEngine
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.data.SurnameInfo
import com.ssc.namespring.model.data.GivenNameInfo
import java.time.LocalDateTime

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
