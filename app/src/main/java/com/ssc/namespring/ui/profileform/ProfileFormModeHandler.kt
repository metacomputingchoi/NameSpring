// ui/profileform/ProfileFormModeHandler.kt
package com.ssc.namespring.ui.profileform

import android.app.Activity
import android.widget.Toast
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode

class ProfileFormModeHandler(
    private val activity: Activity,
    private val config: ProfileFormConfig
) {
    fun handleSaveProfile(
        initializer: ProfileFormInitializer,
        coordinator: ProfileFormCoordinator,
        namingHandler: NamingModeHandler,
        evaluationHandler: EvaluationModeHandler,
        saveHandler: ProfileSaveHandler,
        parentProfileId: String?
    ) {
        val profileName = coordinator.validateAndGetProfileName(initializer.uiComponents, config)
            ?: return

        when (config.mode) {
            ProfileFormMode.NAMING -> handleNamingMode(
                parentProfileId, namingHandler, initializer, config
            )
            ProfileFormMode.EVALUATION -> handleEvaluationMode(
                parentProfileId, evaluationHandler, initializer, config
            )
            else -> performNormalSave(profileName, saveHandler, initializer, config)
        }
    }

    private fun handleNamingMode(
        parentProfileId: String?,
        namingHandler: NamingModeHandler,
        initializer: ProfileFormInitializer,
        config: ProfileFormConfig
    ) {
        parentProfileId?.let { parentId ->
            namingHandler.handleNamingMode(
                parentId,
                initializer.formManager,
                initializer.uiComponents,
                config.getDefaultName()
            ).fold(
                onSuccess = {
                    Toast.makeText(activity, "작명 기록 생성", Toast.LENGTH_SHORT).show()
                    activity.setResult(Activity.RESULT_OK)
                    activity.finish()
                },
                onFailure = { e ->
                    Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun handleEvaluationMode(
        parentProfileId: String?,
        evaluationHandler: EvaluationModeHandler,
        initializer: ProfileFormInitializer,
        config: ProfileFormConfig
    ) {
        parentProfileId?.let { parentId ->
            evaluationHandler.handleEvaluationMode(
                parentId,
                initializer.formManager,
                initializer.uiComponents,
                config.getDefaultName()
            ).fold(
                onSuccess = {
                    Toast.makeText(activity, "평가 기록 생성", Toast.LENGTH_SHORT).show()
                    activity.setResult(Activity.RESULT_OK)
                    activity.finish()
                },
                onFailure = { e ->
                    Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun performNormalSave(
        profileName: String,
        saveHandler: ProfileSaveHandler,
        initializer: ProfileFormInitializer,
        config: ProfileFormConfig
    ) {
        saveHandler.saveProfile(
            initializer.formManager,
            profileName,
            config.profileId,
            onSuccess = {
                Toast.makeText(activity, config.successMessage, Toast.LENGTH_SHORT).show()
                activity.setResult(Activity.RESULT_OK)
                activity.finish()
            },
            onFailure = {
                Toast.makeText(activity, "프로필 저장에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
