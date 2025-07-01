// view/impl/EvaluationInputViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.EvaluationInputView

class EvaluationInputViewImpl(private val activity: Activity) : EvaluationInputView {

    private val logger = AndroidLogger("EvaluationInputView")

    private var evaluationName = ""
    private var evaluationHanja = ""
    private var selectedProfile: Profile? = null

    override fun showDynamicEvaluationInput() {
        logger.d("=== 이름 평가 입력 ===")
        logger.d("평가할 이름을 입력하세요")
        logger.d("한글: [___]")
        logger.d("한자: [___]")
    }

    override fun showProfileSelector(profiles: List<Profile>) {
        logger.d("=== 평가 대상 선택 ===")
        profiles.forEachIndexed { index, profile ->
            logger.d("${index + 1}. ${profile.profileName}")
        }
    }

    override fun getEvaluationName(): String = evaluationName

    override fun getEvaluationHanja(): String = evaluationHanja

    override fun getSelectedProfile(): Profile? = selectedProfile

    override fun validateInput(): Boolean {
        if (evaluationName.isEmpty() || evaluationHanja.isEmpty()) {
            showError("이름을 입력하세요")
            return false
        }
        if (selectedProfile == null) {
            showError("평가 대상을 선택하세요")
            return false
        }
        return true
    }

    override fun showError(message: String) {
        logger.e("입력 오류: $message")
    }

    override fun clearInputs() {
        evaluationName = ""
        evaluationHanja = ""
        selectedProfile = null
    }

    // 테스트용 입력 설정
    fun setTestInput(name: String, hanja: String, profile: Profile?) {
        evaluationName = name
        evaluationHanja = hanja
        selectedProfile = profile
    }
}