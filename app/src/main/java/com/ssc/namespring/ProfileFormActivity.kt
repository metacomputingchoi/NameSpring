// ProfileFormActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.service.profile.ProfileFormService
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.presentation.components.SearchDialogManager
import com.ssc.namespring.ui.profileform.*

class ProfileFormActivity : AppCompatActivity() {
    private lateinit var formManager: ProfileFormManager
    private lateinit var searchDialogManager: SearchDialogManager
    private lateinit var profileFormService: ProfileFormService

    private lateinit var uiComponents: ProfileFormUIComponents
    private lateinit var eventHandler: ProfileFormEventHandler
    private lateinit var uiUpdater: ProfileFormUIUpdater
    private lateinit var nameInputHandler: ProfileFormNameInputHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_form)

        val profileId = intent.getStringExtra("profileId")
        initializeManagers(profileId)
        initializeComponents()
        observeFormState()
        formManager.initialize()
    }

    private fun initializeManagers(profileId: String?) {
        formManager = ProfileFormManager(profileId)
        searchDialogManager = SearchDialogManager()
        profileFormService = ProfileFormService()
    }

    private fun initializeComponents() {
        uiComponents = ProfileFormUIComponents(this)
        nameInputHandler = ProfileFormNameInputHandler(
            formManager,
            searchDialogManager
        )
        eventHandler = ProfileFormEventHandler(
            this,
            formManager,
            searchDialogManager,
            profileFormService,
            uiComponents,
            nameInputHandler
        )
        uiUpdater = ProfileFormUIUpdater(
            uiComponents,
            nameInputHandler
        )

        eventHandler.setupListeners()
    }

    private fun observeFormState() {
        formManager.uiState.observe(this) { state ->
            uiUpdater.updateUI(state)
        }
    }

    fun saveProfile() {
        val profileName = uiComponents.etProfileName.text?.toString() ?: ""

        if (profileName.isEmpty()) {
            Toast.makeText(this, "프로필 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val profileId = intent.getStringExtra("profileId")

        profileFormService.saveProfile(
            this,
            formManager,
            profileName,
            profileId
        ) { success ->
            if (success) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nameInputHandler.cleanup()
    }
}