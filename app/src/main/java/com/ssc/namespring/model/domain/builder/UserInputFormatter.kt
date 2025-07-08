// model/domain/builder/UserInputFormatter.kt
package com.ssc.namespring.model.domain.builder

import android.util.Log
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.presentation.components.ProfileFormUiState

/**
 * 사용자 입력 문자열 포맷팅을 담당
 */
internal class UserInputFormatter {

    companion object {
        private const val TAG = "UserInputFormatter"
    }

    fun formatForNaming(surname: SurnameInfo, uiState: ProfileFormUiState): String {
        var userInput = "[$${surname.korean}/$${surname.hanja}]"

        Log.d(TAG, "=== formatForNaming Debug ===")
        Log.d(TAG, "Surname: $${surname.korean}/$${surname.hanja}")
        Log.d(TAG, "UI State CharDataList size: $${uiState.nameCharDataList.size}")

        uiState.nameCharDataList.forEachIndexed { index, charData ->
            Log.d(TAG, "UI CharData[$$index]: korean='$${charData.korean}', hanja='$${charData.hanja}'")

            val korean = if (charData.korean.isNotEmpty()) charData.korean else "_"
            val hanja = if (charData.hanja.isNotEmpty()) charData.hanja else "_"
            userInput += "[$$korean/$$hanja]"
        }

        Log.d(TAG, "Final userInput: $$userInput")

        return userInput
    }

    fun formatForEvaluation(surname: SurnameInfo, givenNameInfo: GivenNameInfo): String {
        val surnameInput = "[$${surname.korean}/$${surname.hanja}]"

        val givenNameInput = givenNameInfo.charInfos.joinToString("") { charInfo ->
            when {
                charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty() ->
                    "[$${charInfo.korean}/$${charInfo.hanja}]"
                charInfo.korean.isNotEmpty() ->
                    "[$${charInfo.korean}/_]"
                else -> ""
            }
        }

        return surnameInput + givenNameInput
    }
}