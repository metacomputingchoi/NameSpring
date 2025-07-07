// ui/profileform/NamingModeHandler.kt
package com.ssc.namespring.ui.profileform

import android.util.Log
import android.widget.EditText
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.model.domain.service.workmanager.TaskWorkManager
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.presentation.components.ProfileFormUiState

class NamingModeHandler(
    private val taskWorkManager: TaskWorkManager
) {
    companion object {
        private const val TAG = "NamingModeHandler"
    }

    fun handleNamingMode(
        parentProfileId: String,
        formManager: ProfileFormManager,
        uiComponents: ProfileFormUIComponents,
        defaultName: String
    ): Result<Unit> {
        val uiState = formManager.uiState.value ?: return Result.failure(
            Exception("UI State is null")
        )

        val surname = uiState.selectedSurname
        if (surname == null || surname.korean.isEmpty() || surname.hanja.isEmpty()) {
            return Result.failure(Exception("성씨 정보(한글+한자)를 모두 입력해주세요"))
        }

        val currentNameData = extractNameDataFromUI(uiComponents)

        val hasEmptyField = currentNameData.any { (korean, hanja) ->
            korean.isEmpty() || hanja.isEmpty()
        }

        if (!hasEmptyField) {
            return Result.failure(
                Exception("작명 모드에서는 이름 부분에 최소 하나 이상의 빈 입력란이 있어야 합니다")
            )
        }

        val namingTask = createNamingTask(
            parentProfileId, uiState, surname, currentNameData, defaultName, formManager
        )

        taskWorkManager.enqueueTask(namingTask)
        return Result.success(Unit)
    }

    private fun extractNameDataFromUI(uiComponents: ProfileFormUIComponents): List<Pair<String, String>> {
        val currentNameData = mutableListOf<Pair<String, String>>()
        val container = uiComponents.nameInputContainer

        Log.d(TAG, "Container child count: ${container.childCount}")

        for (i in 0 until container.childCount) {
            val itemView = container.getChildAt(i)
            val etKorean = itemView?.findViewById<EditText>(R.id.etKorean)
            val etHanja = itemView?.findViewById<EditText>(R.id.etHanja)

            val korean = etKorean?.text?.toString() ?: ""
            val hanja = etHanja?.text?.toString() ?: ""

            Log.d(TAG, "Position $i - EditText values: korean='$korean', hanja='$hanja'")
            currentNameData.add(Pair(korean, hanja))
        }

        return currentNameData
    }

    private fun createNamingTask(
        parentProfileId: String,
        uiState: ProfileFormUiState,
        surname: SurnameInfo,
        currentNameData: List<Pair<String, String>>,
        defaultName: String,
        formManager: ProfileFormManager
    ): Task {
        val birthDateTime = formManager.getSelectedDate()
        val birthDateTimeMillis = birthDateTime.timeInMillis

        val nameInput = currentNameData.joinToString("") { (korean, hanja) ->
            when {
                korean.isEmpty() && hanja.isEmpty() -> "[_/_]"
                korean.isNotEmpty() && hanja.isEmpty() -> "[${korean}/_]"
                korean.isEmpty() && hanja.isNotEmpty() -> "[_/${hanja}]"
                else -> "[${korean}/${hanja}]"
            }
        }

        Log.d(TAG, "Naming input format: [${surname.korean}/${surname.hanja}]$nameInput")

        return Task(
            profileId = parentProfileId,
            type = TaskType.NAMING,
            inputData = mapOf(
                "profileName" to (uiState.profileName.ifEmpty { defaultName }),
                "birthDateTime" to birthDateTimeMillis.toString(),
                "isYajaTime" to uiState.isYajaTime,
                "nameCharCount" to currentNameData.size,
                "nameInputFormat" to nameInput,
                "surname" to mapOf(
                    "korean" to surname.korean,
                    "hanja" to surname.hanja,
                    "strokes" to (surname.strokes ?: 0)
                )
            )
        )
    }
}