// ui/profileform/ProfileFormInitializer.kt
package com.ssc.namespring.ui.profileform

import android.util.Log
import android.widget.Toast
import com.ssc.namespring.ProfileFormActivity
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.service.profile.ProfileFormService
import com.ssc.namespring.model.domain.service.workmanager.TaskWorkManager
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.domain.usecase.ProfileManager
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.model.presentation.components.SearchDialogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileFormInitializer(
    private val activity: ProfileFormActivity,
    private val config: ProfileFormConfig
) {
    companion object {
        private const val TAG = "ProfileFormInitializer"
    }

    val formManager = ProfileFormManager(config.profileId)
    val searchDialogManager = SearchDialogManager()
    val profileFormService = ProfileFormService()
    val taskWorkManager = TaskWorkManager.getInstance(activity)
    val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    lateinit var uiComponents: ProfileFormUIComponents
    lateinit var eventHandler: ProfileFormEventHandler
    lateinit var uiUpdater: ProfileFormUIUpdater
    lateinit var nameInputHandler: ProfileFormNameInputHandler

    fun initializeComponents() {
        uiComponents = ProfileFormUIComponents(activity, config)

        nameInputHandler = ProfileFormNameInputHandler(
            formManager,
            searchDialogManager
        )

        eventHandler = ProfileFormEventHandler(
            activity,
            formManager,
            searchDialogManager,
            profileFormService,
            uiComponents,
            nameInputHandler,
            config
        )

        uiUpdater = ProfileFormUIUpdater(
            uiComponents,
            nameInputHandler,
            config,
            formManager
        )

        uiComponents.btnSave.isEnabled = false
    }

    suspend fun initializeAsync(): Boolean {
        return try {
            withContext(Dispatchers.Main) {
                eventHandler.setupListeners()
                formManager.initialize()
                uiComponents.btnSave.isEnabled = true
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed", e)
            Toast.makeText(activity, "초기화 실패: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
}