// ProfileFormActivity.kt
package com.ssc.namespring

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.model.domain.entity.TaskType
import com.ssc.namespring.model.domain.service.profile.ProfileFormService
import com.ssc.namespring.model.domain.service.workmanager.TaskWorkManager
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.domain.usecase.ProfileManager
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.model.presentation.components.SearchDialogManager
import com.ssc.namespring.ui.profileform.*
import com.ssc.namespring.model.domain.service.factory.NamingEngineProvider
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputButtonUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private lateinit var taskWorkManager: TaskWorkManager

    private lateinit var uiComponents: ProfileFormUIComponents
    private lateinit var eventHandler: ProfileFormEventHandler
    private lateinit var uiUpdater: ProfileFormUIUpdater
    private lateinit var nameInputHandler: ProfileFormNameInputHandler

    private lateinit var progressBar: ProgressBar
    private var isInitialized = false
    private var parentProfileId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_form)

        config = intent.getSerializableExtra(EXTRA_CONFIG) as? ProfileFormConfig
            ?: ProfileFormConfig(ProfileFormMode.CREATE)

        parentProfileId = config.parentProfileId

        initializeProgressBar()
        initializeManagers()
        initializeComponents()
        observeFormState()

        // 비동기로 초기화
        lifecycleScope.launch {
            initializeAsync()
        }

        val prefs = getSharedPreferences("temp_profile", MODE_PRIVATE)
        val profileData = prefs.getString("profile_data", null)
        if (profileData != null) {
            val profile = Gson().fromJson(profileData, Profile::class.java)
            prefs.edit().clear().apply()  // 사용 후 삭제

            lifecycleScope.launch {
                delay(500)  // UI 초기화 대기
                formManager.loadFromParentProfile(profile)
            }
        }
    }

    private fun initializeProgressBar() {
        progressBar = findViewById(R.id.progressBar)
    }

    fun syncUiStateWithInput(position: Int, korean: String, hanja: String) {
        formManager.setHanjaInfo(position, korean, hanja)
    }

    private fun initializeManagers() {
        formManager = ProfileFormManager(config.profileId)
        searchDialogManager = SearchDialogManager()
        profileFormService = ProfileFormService()
        taskWorkManager = TaskWorkManager.getInstance(this)
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

        // formManager 파라미터 추가
        uiUpdater = ProfileFormUIUpdater(
            uiComponents,
            nameInputHandler,
            config,
            formManager  // 추가
        )

        uiComponents.btnSave.isEnabled = false
    }

    fun loadParentProfileData() {
        parentProfileId?.let { parentId ->
            profileManager.getProfile(parentId)?.let { parentProfile ->
                // TextWatcher 임시 비활성화
                nameInputHandler.cleanup()

                // 프로필 로드
                formManager.loadFromParentProfile(parentProfile)

                // UI가 완전히 업데이트된 후 TextWatcher 재활성화
                uiComponents.nameInputContainer.postDelayed({
                    formManager.forceUpdateUiState()
                    Toast.makeText(this, "프로필 데이터를 불러왔습니다", Toast.LENGTH_SHORT).show()
                }, 100)
            }
        }
    }

    private suspend fun initializeAsync() {
        showLoading(true)

        try {
            withContext(Dispatchers.Main) {
                eventHandler.setupListeners()
                formManager.initialize()

                // 작명/평가 모드에서는 데이터 로드 버튼을 통해서만 부모 프로필 데이터 로드
                // 자동 로드 제거!

                isInitialized = true
                uiComponents.btnSave.isEnabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed", e)
            Toast.makeText(this, "초기화 실패: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        } finally {
            showLoading(false)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        uiComponents.scrollView?.isEnabled = !show
        uiComponents.btnSave.isEnabled = !show && isInitialized
        uiComponents.btnReset.isEnabled = !show
    }

    private fun observeFormState() {
        formManager.uiState.observe(this) { state ->
            uiUpdater.updateUI(state)
        }
    }

    fun saveProfile() {
        if (!isInitialized) {
            Toast.makeText(this, "초기화가 완료되지 않았습니다", Toast.LENGTH_SHORT).show()
            return
        }

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

        // 작명 모드 처리
        if (config.mode == ProfileFormMode.NAMING) {
            handleNamingMode()
            return
        }

        // 평가 모드 처리
        if (config.mode == ProfileFormMode.EVALUATION) {
            handleEvaluationMode()
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

    private fun handleNamingMode() {
        parentProfileId?.let { parentId ->
            val uiState = formManager.uiState.value ?: return

            // 성씨 검증 (한글+한자 모두 필수)
            val surname = uiState.selectedSurname
            if (surname == null || surname.korean.isEmpty() || surname.hanja.isEmpty()) {
                Toast.makeText(this, "성씨 정보(한글+한자)를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                return
            }

            // 현재 UI의 실제 값들을 저장
            val currentNameData = mutableListOf<Pair<String, String>>()
            val container = uiComponents.nameInputContainer

            Log.d(TAG, "=== NAMING MODE UI VALUES DEBUG ===")
            Log.d(TAG, "Container child count: ${container.childCount}")

            // UI에서 직접 값 읽기
            for (i in 0 until container.childCount) {
                val itemView = container.getChildAt(i)
                val etKorean = itemView?.findViewById<EditText>(R.id.etKorean)
                val etHanja = itemView?.findViewById<EditText>(R.id.etHanja)

                val korean = etKorean?.text?.toString() ?: ""
                val hanja = etHanja?.text?.toString() ?: ""

                Log.d(TAG, "Position $i - EditText values: korean='$korean', hanja='$hanja'")
                currentNameData.add(Pair(korean, hanja))
            }

            // 작명 모드 검증: 이름 부분에 최소 하나 이상의 빈 필드가 있어야 함
            val hasEmptyField = currentNameData.any { (korean, hanja) ->
                korean.isEmpty() || hanja.isEmpty()
            }

            if (!hasEmptyField) {
                Toast.makeText(
                    this,
                    "작명 모드에서는 이름 부분에 최소 하나 이상의 빈 입력란이 있어야 합니다",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // 생년월일과 야자시 정보
            val birthDateTime = formManager.getSelectedDate()
            val birthDateTimeMillis = birthDateTime.timeInMillis
            val isYajaTime = uiState.isYajaTime

            // 이름 입력 부분을 NamingEngine 형식으로 구성
            val nameInput = currentNameData.joinToString("") { (korean, hanja) ->
                when {
                    korean.isEmpty() && hanja.isEmpty() -> "[_/_]"
                    korean.isNotEmpty() && hanja.isEmpty() -> "[${korean}/_]"
                    korean.isEmpty() && hanja.isNotEmpty() -> "[_/${hanja}]"
                    else -> "[${korean}/${hanja}]"
                }
            }

            Log.d(TAG, "Final currentNameData: $currentNameData")
            Log.d(TAG, "Naming input format: [${surname.korean}/${surname.hanja}]$nameInput")

            // 작명 작업을 WorkManager에 추가
            val namingTask = Task(
                profileId = parentId,
                type = TaskType.NAMING,
                inputData = mapOf(
                    "profileName" to (uiState.profileName.ifEmpty { config.getDefaultName() }),
                    "birthDateTime" to birthDateTimeMillis.toString(),
                    "isYajaTime" to isYajaTime,
                    "nameCharCount" to currentNameData.size,
                    "nameInputFormat" to nameInput,
                    "surname" to mapOf(
                        "korean" to surname.korean,
                        "hanja" to surname.hanja,
                        "strokes" to (surname.strokes ?: 0)
                    )
                )
            )

            taskWorkManager.enqueueTask(namingTask)
            Toast.makeText(this, "작명 기록 생성", Toast.LENGTH_SHORT).show()

            setResult(RESULT_OK)
            finish()
        }
    }

    private fun handleEvaluationMode() {
        parentProfileId?.let { parentId ->
            val uiState = formManager.uiState.value ?: return

            // 성씨 검증 (한글+한자 모두 필수)
            val surname = uiState.selectedSurname
            if (surname == null || surname.korean.isEmpty() || surname.hanja.isEmpty()) {
                Toast.makeText(this, "성씨 정보(한글+한자)를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                return
            }

            // 현재 UI의 실제 값들을 저장
            val currentNameData = mutableListOf<Pair<String, String>>()
            val container = uiComponents.nameInputContainer

            Log.d(TAG, "=== EVALUATION MODE UI VALUES DEBUG ===")
            Log.d(TAG, "Container child count: ${container.childCount}")

            // UI에서 직접 값 읽기
            for (i in 0 until container.childCount) {
                val itemView = container.getChildAt(i)
                val etKorean = itemView?.findViewById<EditText>(R.id.etKorean)
                val etHanja = itemView?.findViewById<EditText>(R.id.etHanja)

                val korean = etKorean?.text?.toString() ?: ""
                val hanja = etHanja?.text?.toString() ?: ""

                Log.d(TAG, "Position $i - EditText values: korean='$korean', hanja='$hanja'")
                currentNameData.add(Pair(korean, hanja))
            }

            // 평가 모드 검증: 모든 이름 필드가 한글+한자 모두 채워져 있어야 함
            val hasEmptyField = currentNameData.any { (korean, hanja) ->
                korean.isEmpty() || hanja.isEmpty()
            }

            if (hasEmptyField) {
                Toast.makeText(
                    this,
                    "평가 모드에서는 모든 이름의 한글과 한자를 입력해야 합니다",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // 생년월일과 야자시 정보
            val birthDateTime = formManager.getSelectedDate()
            val birthDateTimeMillis = birthDateTime.timeInMillis
            val isYajaTime = uiState.isYajaTime

            // 이름 정보 구성
            val givenNameKorean = currentNameData.joinToString("") { it.first }
            val givenNameHanja = currentNameData.joinToString("") { it.second }

            Log.d(TAG, "Evaluation data - Surname: ${surname.korean}/${surname.hanja}, Given: $givenNameKorean/$givenNameHanja")

            // 평가 작업을 WorkManager에 추가
            val evaluationTask = Task(
                profileId = parentId,
                type = TaskType.EVALUATION,
                inputData = mapOf(
                    "profileName" to (uiState.profileName.ifEmpty { config.getDefaultName() }),
                    "birthDateTime" to birthDateTimeMillis.toString(),
                    "isYajaTime" to isYajaTime,
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

            taskWorkManager.enqueueTask(evaluationTask)
            Toast.makeText(this, "평가 기록 생성", Toast.LENGTH_SHORT).show()

            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nameInputHandler.cleanup()
        NameInputButtonUpdater.cleanup()
    }
}