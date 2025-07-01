// view/impl/EvaluationInputViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.data.DynamicNameInput
import com.ssc.namespring.model.data.CharacterInput
import com.ssc.namespring.model.data.CharacterInputType
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.EvaluationInputView

class EvaluationInputViewImpl(private val activity: Activity) : EvaluationInputView {

    private val logger = AndroidLogger("EvaluationInputView")

    private var selectedProfile: Profile? = null
    private var dynamicNameInput = DynamicNameInput.createDefault()

    override fun showDynamicEvaluationInput() {
        logger.d("=== 이름 평가 입력 ===")
        logger.d("")

        // 성씨 입력 표시
        logger.d("성 (${dynamicNameInput.surnameInputs.size}/2자):")

        // 한글 입력 행
        val surnameHangul = dynamicNameInput.surnameInputs.joinToString(" ") { input ->
            "[${if (input.hangul.isEmpty()) "_" else input.hangul}]"
        }

        val surnameButtons = buildString {
            if (dynamicNameInput.surnameInputs.size > 1) append("[-] ")
            if (dynamicNameInput.surnameInputs.size < 2) append("[+]")
        }.trim()

        logger.d("한글: $surnameHangul  $surnameButtons")

        // 한자 입력 행
        val surnameHanja = dynamicNameInput.surnameInputs.joinToString(" ") { input ->
            "[${if (input.hanja.isEmpty()) "_" else input.hanja}]"
        }
        logger.d("한자: $surnameHanja")

        logger.d("")

        // 이름 입력 표시
        logger.d("이름 (${dynamicNameInput.givenNameInputs.size}/4자):")

        // 한글 입력 행
        val givenNameHangul = dynamicNameInput.givenNameInputs.joinToString(" ") { input ->
            "[${if (input.hangul.isEmpty()) "_" else input.hangul}]"
        }

        val givenNameButtons = buildString {
            if (dynamicNameInput.givenNameInputs.size > 1) append("[-] ")
            if (dynamicNameInput.givenNameInputs.size < 4) append("[+]")
        }.trim()

        logger.d("한글: $givenNameHangul  $givenNameButtons")

        // 한자 입력 행
        val givenNameHanja = dynamicNameInput.givenNameInputs.joinToString(" ") { input ->
            "[${if (input.hanja.isEmpty()) "_" else input.hanja}]"
        }
        logger.d("한자: $givenNameHanja")

        logger.d("")
        logger.d("[+] 글자 추가, [-] 글자 제거")

        // 현재 입력된 이름 표시
        val currentName = getEvaluationName()
        val currentHanja = getEvaluationHanja()
        if (currentName.isNotEmpty() && !currentName.contains("_")) {
            logger.d("")
            logger.d("현재 입력: $currentName ($currentHanja)")
        }
    }

    override fun showProfileSelector(profiles: List<Profile>) {
        logger.d("")
        logger.d("=== 평가 대상 선택 ===")
        profiles.forEachIndexed { index, profile ->
            logger.d("${index + 1}. ${profile.profileName} - ${profile.getFullName()}")
        }
    }

    override fun getEvaluationName(): String {
        val surname = dynamicNameInput.surnameInputs.joinToString("") { it.hangul.ifEmpty { "_" } }
        val givenName = dynamicNameInput.givenNameInputs.joinToString("") { it.hangul.ifEmpty { "_" } }
        return surname + givenName
    }

    override fun getEvaluationHanja(): String {
        val surnameHanja = dynamicNameInput.surnameInputs.joinToString("") { it.hanja.ifEmpty { "_" } }
        val givenNameHanja = dynamicNameInput.givenNameInputs.joinToString("") { it.hanja.ifEmpty { "_" } }
        return surnameHanja + givenNameHanja
    }

    override fun getSelectedProfile(): Profile? = selectedProfile

    override fun validateInput(): Boolean {
        // 각 입력 검증
        val allInputs = dynamicNameInput.surnameInputs + dynamicNameInput.givenNameInputs
        val validatedInputs = allInputs.map { it.validate() }

        // 에러 체크
        val hasError = validatedInputs.any { !it.isValid }
        if (hasError) {
            validatedInputs.filter { !it.isValid }.forEach { input ->
                input.errorMessage?.let { showError(it) }
            }
            return false
        }

        // 빈 입력 체크
        val name = getEvaluationName()
        val hanja = getEvaluationHanja()

        if (name.contains("_") || hanja.contains("_")) {
            showError("이름을 모두 입력하세요")
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
        dynamicNameInput = DynamicNameInput.createDefault()
        selectedProfile = null
        logger.d("입력 폼이 초기화되었습니다")
    }

    // 테스트용 입력 설정
    fun setTestInput(name: String, hanja: String, profile: Profile?) {
        selectedProfile = profile

        // 이름을 성씨와 이름으로 분리 (간단히 첫 글자를 성씨로 가정)
        val surnameLength = 1  // 테스트에서는 1자 성씨로 가정
        val surname = name.take(surnameLength)
        val surnameHanjaStr = hanja.take(surnameLength)
        val givenName = name.drop(surnameLength)
        val givenNameHanjaStr = hanja.drop(surnameLength)

        // 동적 입력 생성
        val surnameInputs = surname.mapIndexed { index, char ->
            CharacterInput(
                position = index,
                isSurname = true,
                hangul = char.toString(),
                hanja = surnameHanjaStr.getOrNull(index)?.toString() ?: "",
                inputType = CharacterInputType.CHARACTER
            )
        }

        val givenNameInputs = givenName.mapIndexed { index, char ->
            CharacterInput(
                position = index,
                isSurname = false,
                hangul = char.toString(),
                hanja = givenNameHanjaStr.getOrNull(index)?.toString() ?: "",
                inputType = CharacterInputType.CHARACTER
            )
        }

        dynamicNameInput = DynamicNameInput(
            surnameInputs = surnameInputs,
            givenNameInputs = givenNameInputs
        )

        logger.d("테스트 입력 설정: $name ($hanja)")
        showDynamicEvaluationInput()
    }

    // 글자 추가/제거 시뮬레이션
    fun simulateAddSurnameChar() {
        if (dynamicNameInput.canAddSurnameChar()) {
            dynamicNameInput = dynamicNameInput.addSurnameChar()
            logger.d("성씨 글자 추가됨 (현재 ${dynamicNameInput.surnameInputs.size}자)")
            showDynamicEvaluationInput()
        } else {
            logger.d("성씨는 최대 2자까지만 가능합니다")
        }
    }

    fun simulateRemoveSurnameChar() {
        if (dynamicNameInput.surnameInputs.size > 1) {
            dynamicNameInput = dynamicNameInput.removeSurnameChar()
            logger.d("성씨 글자 제거됨 (현재 ${dynamicNameInput.surnameInputs.size}자)")
            showDynamicEvaluationInput()
        } else {
            logger.d("성씨는 최소 1자는 필요합니다")
        }
    }

    fun simulateAddGivenNameChar() {
        if (dynamicNameInput.canAddGivenNameChar()) {
            dynamicNameInput = dynamicNameInput.addGivenNameChar()
            logger.d("이름 글자 추가됨 (현재 ${dynamicNameInput.givenNameInputs.size}자)")
            showDynamicEvaluationInput()
        } else {
            logger.d("이름은 최대 4자까지만 가능합니다")
        }
    }

    fun simulateRemoveGivenNameChar() {
        if (dynamicNameInput.givenNameInputs.size > 1) {
            dynamicNameInput = dynamicNameInput.removeGivenNameChar()
            logger.d("이름 글자 제거됨 (현재 ${dynamicNameInput.givenNameInputs.size}자)")
            showDynamicEvaluationInput()
        } else {
            logger.d("이름은 최소 1자는 필요합니다")
        }
    }

    // 입력 업데이트 시뮬레이션
    fun simulateUpdateInput(isSurname: Boolean, position: Int, hangul: String, hanja: String) {
        dynamicNameInput = dynamicNameInput.updateInput(position, isSurname) {
            copy(hangul = hangul, hanja = hanja)
        }
        logger.d("${if (isSurname) "성" else "이름"} ${position + 1}번째 글자 업데이트: $hangul/$hanja")
        showDynamicEvaluationInput()
    }
}