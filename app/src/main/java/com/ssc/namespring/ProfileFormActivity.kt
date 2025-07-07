package com.ssc.namespring

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputButtonUpdater
import com.ssc.namespring.ui.profileform.*
import kotlinx.coroutines.launch

class ProfileFormActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_CONFIG = "profile_form_config"

        fun newIntent(context: Context, config: ProfileFormConfig): Intent {
            return Intent(context, ProfileFormActivity::class.java).apply {
                putExtra(EXTRA_CONFIG, config)
            }
        }
    }

    private lateinit var config: ProfileFormConfig
    private lateinit var initializer: ProfileFormInitializer
    private lateinit var stateHandler: ProfileFormStateHandler
    private lateinit var saveHandler: ProfileSaveHandler
    private lateinit var namingHandler: NamingModeHandler
    private lateinit var evaluationHandler: EvaluationModeHandler
    private lateinit var coordinator: ProfileFormCoordinator
    private var parentProfileId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_form)

        config = intent.getSerializableExtra(EXTRA_CONFIG) as? ProfileFormConfig
            ?: ProfileFormConfig(ProfileFormMode.CREATE)
        parentProfileId = config.parentProfileId

        initializeHandlers()
        initializeUI()

        lifecycleScope.launch {
            performAsyncInitialization()
        }
    }

    private fun initializeHandlers() {
        initializer = ProfileFormInitializer(this, config)
        saveHandler = ProfileSaveHandler(this, initializer.profileFormService)
        namingHandler = NamingModeHandler(initializer.taskWorkManager)
        evaluationHandler = EvaluationModeHandler(initializer.taskWorkManager)
        coordinator = ProfileFormCoordinator(this, initializer.profileManager)
    }

    private fun initializeUI() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        initializer.initializeComponents()

        stateHandler = ProfileFormStateHandler(
            progressBar,
            initializer.uiComponents,
            initializer.formManager,
            initializer.uiUpdater
        )

        stateHandler.observeFormState(this)
    }

    private suspend fun performAsyncInitialization() {
        stateHandler.showLoading(true)

        val success = initializer.initializeAsync()
        if (success) {
            stateHandler.setInitialized(true)
            coordinator.loadTempProfileIfExists(initializer.formManager)
        } else {
            finish()
        }

        stateHandler.showLoading(false)
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
        val profileName = coordinator.validateAndGetProfileName(initializer.uiComponents, config)
            ?: return

        when (config.mode) {
            ProfileFormMode.NAMING -> handleNamingMode()
            ProfileFormMode.EVALUATION -> handleEvaluationMode()
            else -> performNormalSave(profileName)
        }
    }

    private fun handleNamingMode() {
        parentProfileId?.let { parentId ->
            namingHandler.handleNamingMode(
                parentId,
                initializer.formManager,
                initializer.uiComponents,
                config.getDefaultName()
            ).fold(
                onSuccess = {
                    Toast.makeText(this, "작명 기록 생성", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                },
                onFailure = { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun handleEvaluationMode() {
        parentProfileId?.let { parentId ->
            evaluationHandler.handleEvaluationMode(
                parentId,
                initializer.formManager,
                initializer.uiComponents,
                config.getDefaultName()
            ).fold(
                onSuccess = {
                    Toast.makeText(this, "평가 기록 생성", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                },
                onFailure = { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun performNormalSave(profileName: String) {
        saveHandler.saveProfile(
            initializer.formManager,
            profileName,
            config.profileId,
            onSuccess = {
                Toast.makeText(this, config.successMessage, Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            },
            onFailure = {
                Toast.makeText(this, "프로필 저장에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        initializer.nameInputHandler.cleanup()
        NameInputButtonUpdater.cleanup()
    }
}
