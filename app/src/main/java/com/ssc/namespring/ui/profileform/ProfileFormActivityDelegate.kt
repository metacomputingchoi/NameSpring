// ui/profileform/ProfileFormActivityDelegate.kt
package com.ssc.namespring.ui.profileform

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ssc.namespring.ProfileFormActivity
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputButtonUpdater
import kotlinx.coroutines.launch

class ProfileFormActivityDelegate(
    private val activity: ProfileFormActivity,
    private val config: ProfileFormConfig
) {
    private lateinit var initializer: ProfileFormInitializer
    private lateinit var stateHandler: ProfileFormStateHandler
    private lateinit var saveHandler: ProfileSaveHandler
    private lateinit var namingHandler: NamingModeHandler
    private lateinit var evaluationHandler: EvaluationModeHandler
    private lateinit var coordinator: ProfileFormCoordinator
    private lateinit var modeHandler: ProfileFormModeHandler

    val parentProfileId: String? = config.parentProfileId

    fun onCreate() {
        initializeHandlers()
        initializeUI()
        performAsyncInitialization()
    }

    private fun initializeHandlers() {
        initializer = ProfileFormInitializer(activity, config)
        saveHandler = ProfileSaveHandler(activity, initializer.profileFormService)
        namingHandler = NamingModeHandler(initializer.taskWorkManager)
        evaluationHandler = EvaluationModeHandler(initializer.taskWorkManager)
        coordinator = ProfileFormCoordinator(activity, initializer.profileManager)
        modeHandler = ProfileFormModeHandler(activity, config)
    }

    private fun initializeUI() {
        val progressBar = activity.findViewById<android.widget.ProgressBar>(com.ssc.namespring.R.id.progressBar)
        initializer.initializeComponents()

        stateHandler = ProfileFormStateHandler(
            progressBar,
            initializer.uiComponents,
            initializer.formManager,
            initializer.uiUpdater
        )

        stateHandler.observeFormState(activity)
    }

    private fun performAsyncInitialization() {
        activity.lifecycleScope.launch {
            stateHandler.showLoading(true)

            val success = initializer.initializeAsync()
            if (success) {
                stateHandler.setInitialized(true)

                // 편집 모드일 때 기존 프로필 데이터 로드
                if (config.mode == ProfileFormMode.EDIT && config.profileId != null) {
                    coordinator.loadProfileForEdit(
                        config.profileId,
                        initializer.formManager,
                        initializer.uiComponents
                    )
                } else {
                    coordinator.loadTempProfileIfExists(initializer.formManager)
                }
            } else {
                activity.finish()
            }

            stateHandler.showLoading(false)
        }
    }

    fun syncUiStateWithInput(position: Int, korean: String, hanja: String) {
        stateHandler.syncUiStateWithInput(position, korean, hanja)
    }

    fun loadParentProfileData() {
        coordinator.loadParentProfileData(
            parentProfileId,
            initializer.formManager,
            initializer.nameInputHandler,
            initializer.uiComponents
        )
    }

    fun saveProfile() {
        modeHandler.handleSaveProfile(
            initializer,
            coordinator,
            namingHandler,
            evaluationHandler,
            saveHandler,
            parentProfileId
        )
    }

    fun onDestroy() {
        initializer.nameInputHandler.cleanup()
        NameInputButtonUpdater.cleanup()
    }
}