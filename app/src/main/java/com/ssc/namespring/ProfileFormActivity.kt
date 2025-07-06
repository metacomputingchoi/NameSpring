// ProfileFormActivity.kt
package com.ssc.namespring

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.service.profile.ProfileFormService
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.presentation.components.SearchDialogManager
import com.ssc.namespring.ui.profileform.*

class ProfileFormActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ProfileFormActivity"
    }

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

        Log.d(TAG, "Saving profile: name=$profileName, profileId=$profileId")

        // 프로필 생성 전 현재 상태 로그
        val givenNameInfo = formManager.getNameDataManager().createGivenNameInfo()
        Log.d(TAG, "GivenNameInfo before save: ${givenNameInfo?.korean}/${givenNameInfo?.hanja}")

        profileFormService.saveProfile(
            this,
            formManager,
            profileName,
            profileId
        ) { success ->
            if (success) {
                Log.d(TAG, "Profile saved successfully")
                val message = if (profileId.isNullOrEmpty()) {
                    "프로필이 생성되었습니다"
                } else {
                    "프로필이 수정되었습니다"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Log.e(TAG, "Failed to save profile")
                Toast.makeText(this, "프로필 저장에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nameInputHandler.cleanup()
    }
}