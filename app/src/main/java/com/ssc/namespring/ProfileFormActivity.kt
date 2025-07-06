// ProfileFormActivity.kt
package com.ssc.namespring

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode
import com.ssc.namespring.model.domain.service.profile.ProfileFormService
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.domain.usecase.ProfileManager
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.model.presentation.components.SearchDialogManager
import com.ssc.namespring.ui.profileform.*

class ProfileFormActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ProfileFormActivity"
        private const val EXTRA_CONFIG = "profile_form_config"

        fun newIntent(context: Context, config: ProfileFormConfig): Intent {
            return Intent(context, ProfileFormActivity::class.java).apply {
                putExtra(EXTRA_CONFIG, config)
            }
        }
    }

    private lateinit var config: ProfileFormConfig
    private lateinit var formManager: ProfileFormManager
    private lateinit var searchDialogManager: SearchDialogManager
    private lateinit var profileFormService: ProfileFormService
    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    private lateinit var uiComponents: ProfileFormUIComponents
    private lateinit var eventHandler: ProfileFormEventHandler
    private lateinit var uiUpdater: ProfileFormUIUpdater
    private lateinit var nameInputHandler: ProfileFormNameInputHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_form)

        config = intent.getSerializableExtra(EXTRA_CONFIG) as? ProfileFormConfig
            ?: ProfileFormConfig(ProfileFormMode.CREATE)

        initializeManagers()
        initializeComponents()
        observeFormState()

        formManager.initialize()
    }

    private fun initializeManagers() {
        formManager = ProfileFormManager(config.profileId)
        searchDialogManager = SearchDialogManager()
        profileFormService = ProfileFormService()
    }

    private fun initializeComponents() {
        uiComponents = ProfileFormUIComponents(this, config)

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
            nameInputHandler,
            config
        )

        uiUpdater = ProfileFormUIUpdater(
            uiComponents,
            nameInputHandler,
            config
        )

        eventHandler.setupListeners()
    }

    private fun loadParentProfileData() {
        config.parentProfileId?.let { parentId ->
            profileManager.getProfile(parentId)?.let { parentProfile ->
                // 부모 프로필의 데이터를 formManager에 로드
                formManager.loadFromParentProfile(parentProfile)
            }
        }
    }

    private fun observeFormState() {
        formManager.uiState.observe(this) { state ->
            uiUpdater.updateUI(state)
        }
    }

    fun saveProfile() {
        // 작명/평가 모드에서는 프로필 이름이 비어있어도 기본값 사용
        val profileName = uiComponents.etProfileName.text?.toString()?.takeIf { it.isNotEmpty() }
            ?: if (config.mode in listOf(ProfileFormMode.NAMING, ProfileFormMode.EVALUATION)) {
                config.getDefaultName()
            } else {
                ""
            }

        if (profileName.isEmpty()) {
            Toast.makeText(this, "${config.profileLabelText}을(를) 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Saving profile: name=$profileName, mode=${config.mode}, profileId=${config.profileId}")

        // 작명/평가 모드일 때 실제 기능은 아직 미구현
        if (config.mode in listOf(ProfileFormMode.NAMING, ProfileFormMode.EVALUATION)) {
            // TODO: 실제 작명/평가 로직 구현
            Toast.makeText(this, "기능 구현 예정입니다", Toast.LENGTH_SHORT).show()
            return
        }

        // 기존 프로필 저장 로직
        profileFormService.saveProfile(
            this,
            formManager,
            profileName,
            config.profileId
        ) { success ->
            if (success) {
                Log.d(TAG, "Profile saved successfully")
                Toast.makeText(this, config.successMessage, Toast.LENGTH_SHORT).show()
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