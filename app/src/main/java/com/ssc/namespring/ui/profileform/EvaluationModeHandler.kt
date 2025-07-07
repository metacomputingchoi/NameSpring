// ui/profileform/EvaluationModeHandler.kt
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

class EvaluationModeHandler(
    private val taskWorkManager: TaskWorkManager
) {
    companion object {
        private const val TAG = "EvaluationModeHandler"
    }

    fun handleEvaluationMode(
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

        if (hasEmptyField) {
            return Result.failure(
                Exception("평가 모드에서는 모든 이름의 한글과 한자를 입력해야 합니다")
            )
        }

        val evaluationTask = createEvaluationTask(
            parentProfileId, uiState, surname, currentNameData, defaultName, formManager
        )

        taskWorkManager.enqueueTask(evaluationTask)
        return Result.success(Unit)
    }

    private fun extractNameDataFromUI(uiComponents: ProfileFormUIComponents): List<Pair<String, String>> {
        val currentNameData = mutableListOf<Pair<String, String>>()
        val container = uiComponents.nameInputContainer

        for (i in 0 until container.childCount) {
            val itemView = container.getChildAt(i)
            val etKorean = itemView?.findViewById<EditText>(R.id.etKorean)
            val etHanja = itemView?.findViewById<EditText>(R.id.etHanja)

            val korean = etKorean?.text?.toString() ?: ""
            val hanja = etHanja?.text?.toString() ?: ""

            currentNameData.add(Pair(korean, hanja))
        }

        return currentNameData
    }

    private fun createEvaluationTask(
        parentProfileId: String,
        uiState: ProfileFormUiState,
        surname: SurnameInfo,
        currentNameData: List<Pair<String, String>>,
        defaultName: String,
        formManager: ProfileFormManager
    ): Task {
        val birthDateTime = formManager.getSelectedDate()
        val birthDateTimeMillis = birthDateTime.timeInMillis

        val givenNameKorean = currentNameData.joinToString("") { it.first }
        val givenNameHanja = currentNameData.joinToString("") { it.second }

        Log.d(TAG, "Evaluation data - Surname: ${surname.korean}/${surname.hanja}, Given: $givenNameKorean/$givenNameHanja")

        return Task(
            profileId = parentProfileId,
            type = TaskType.EVALUATION,
            inputData = mapOf(
                "profileName" to (uiState.profileName.ifEmpty { defaultName }),
                "birthDateTime" to birthDateTimeMillis.toString(),
                "isYajaTime" to uiState.isYajaTime,
                "surname" to mapOf(
                    "korean" to surname.korean,
                    "hanja" to surname.hanja
                ),
                "givenName" to mapOf(
                    "korean" to givenNameKorean,
                    "hanja" to givenNameHanja
                ),
                "nameCharCount" to currentNameData.size
            )
        )
    }
}