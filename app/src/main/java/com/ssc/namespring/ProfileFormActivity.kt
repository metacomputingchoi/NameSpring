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
import com.ssc.namingengine.NamingEngine
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namespring.model.domain.service.evaluation.ProfileScoreCalculator
import java.time.LocalDateTime
import java.time.ZoneId

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
    private lateinit var namingEngine: NamingEngine

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

        // NamingEngine 초기화
        namingEngine = NamingEngine.create()
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
        // 작명 모드 검증
        if (!formManager.isValidForNaming()) {
            Toast.makeText(this, "성씨 정보(한글+한자)를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val namingInput = formManager.createNamingInput()
        if (namingInput == null) {
            Toast.makeText(this, "입력 정보를 확인해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "=== NAMING Mode Input ===")
        Log.d(TAG, "userInput: ${namingInput.userInput}")
        Log.d(TAG, "birthDateTime: ${namingInput.birthDateTime}")
        Log.d(TAG, "useYajasi: ${namingInput.useYajasi}")

        try {
            // NamingEngine 호출
            val generatedNames = namingEngine.generateNames(
                userInput = namingInput.userInput,
                birthDateTime = namingInput.birthDateTime,
                useYajasi = namingInput.useYajasi,
                verbose = true,
            )

            Log.d(TAG, "=== NAMING Results ===")
            Log.d(TAG, "Generated ${generatedNames.size} names")

            // 상위 5개 결과만 출력
            generatedNames.take(5).forEachIndexed { index, name ->
                Log.d(TAG, "[$index] ${name.combinedPronounciation}(${name.combinedHanja})")
                name.analysisInfo?.let { info ->
                    Log.d(TAG, "  - 총점: ${info.totalScore}")
                    Log.d(TAG, "  - 음양: ${info.eumYangInfo.pattern}")
                    Log.d(TAG, "  - 오행: ${info.ohaengInfo.overallHarmony}")
                }
            }
            Log.d(TAG, "=== End of NAMING Results ===")

            Toast.makeText(this, "${generatedNames.size}개의 이름이 생성되었습니다", Toast.LENGTH_LONG).show()

            // TODO: 결과를 화면에 표시하거나 저장

        } catch (e: Exception) {
            Log.e(TAG, "작명 실행 중 오류", e)
            Toast.makeText(this, "작명 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleEvaluationMode() {
        // 평가 모드 검증
        if (!formManager.isValidForEvaluation()) {
            Toast.makeText(
                this,
                "성씨와 이름 모두 한글과 한자를 입력해주세요\n(이름은 1~4글자)",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val evaluationInput = formManager.createEvaluationInput()
        if (evaluationInput == null) {
            Toast.makeText(this, "입력 정보를 확인해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "=== EVALUATION Mode Input ===")
        Log.d(TAG, "userInput: ${evaluationInput.userInput}")
        Log.d(TAG, "birthDateTime: ${evaluationInput.birthDateTime}")
        Log.d(TAG, "useYajasi: ${evaluationInput.useYajasi}")

        try {
            // NamingEngine 호출 (평가 모드에서는 입력한 이름만 평가)
            val generatedNames = namingEngine.generateNames(
                userInput = evaluationInput.userInput,
                birthDateTime = evaluationInput.birthDateTime,
                useYajasi = evaluationInput.useYajasi,
                verbose = true,
                withoutFilter = true  // 필터링 없이 입력한 이름 그대로 평가
            )

            if (generatedNames.isNotEmpty()) {
                val evaluatedName = generatedNames.first()

                Log.d(TAG, "=== EVALUATION Result ===")
                Log.d(TAG, "Name: ${evaluatedName.combinedPronounciation}(${evaluatedName.combinedHanja})")

                evaluatedName.analysisInfo?.let { info ->
                    Log.d(TAG, "총점: ${info.totalScore}")
                    Log.d(TAG, "음양: ${info.eumYangInfo.pattern} (균형: ${info.eumYangInfo.isBalanced})")
                    Log.d(TAG, "오행: ${info.ohaengInfo.overallHarmony}")

                    // 상세 점수
                    Log.d(TAG, "=== 점수 상세 ===")
                    info.scoreBreakdown.forEach { (key, value) ->
                        Log.d(TAG, "$key: $value")
                    }

                    // 사격 정보
                    evaluatedName.sagyeok?.let { sagyeok ->
                        Log.d(TAG, "=== 사격 정보 ===")
                        Log.d(TAG, "형격: ${sagyeok.hyeong}")
                        Log.d(TAG, "원격: ${sagyeok.won}")
                        Log.d(TAG, "이격: ${sagyeok.i}")
                        Log.d(TAG, "정격: ${sagyeok.jeong}")
                    }
                }
                Log.d(TAG, "=== End of EVALUATION Result ===")

                // 이름봄 점수 계산
                val namebomScore = ProfileScoreCalculator.calculateNamebomScore(evaluatedName)
                Toast.makeText(
                    this,
                    "평가 완료\n이름봄 점수: ${namebomScore}점",
                    Toast.LENGTH_LONG
                ).show()

                // TODO: 평가 결과를 프로필에 저장하거나 화면에 표시

            } else {
                Log.e(TAG, "평가 결과가 없습니다")
                Toast.makeText(this, "평가를 수행할 수 없습니다", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "평가 실행 중 오류", e)
            Toast.makeText(this, "평가 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nameInputHandler.cleanup()
    }
}