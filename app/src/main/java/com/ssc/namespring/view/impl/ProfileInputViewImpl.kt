// view/impl/ProfileInputViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.DynamicNameInput
import com.ssc.namespring.model.data.CharacterInput
import com.ssc.namespring.model.data.CharacterInputType
import com.ssc.namespring.utils.JsonLoader
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.ProfileInputView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProfileInputViewImpl(private val activity: Activity) : ProfileInputView {

    private val logger = AndroidLogger("ProfileInputView")

    // 동적 입력을 위한 데이터
    private var dynamicNameInput = DynamicNameInput.createDefault()
    private var profileName = ""
    private var birthDateTime = LocalDateTime.now()
    private var useYajasi = false

    override fun showDynamicNameInput(maxSurname: Int, maxGivenName: Int) {
        logger.d("=== 이름 입력 폼 ===")

        // 온보딩 가이드 표시
        showOnboardingGuide()

        logger.d("")
        logger.d("프로필명: [${if (profileName.isEmpty()) "_______" else profileName}] (예: 나, 배우자, 첫째)")
        logger.d("")

        // 성 입력
        logger.d("성 (${dynamicNameInput.surnameInputs.size}/${maxSurname}자):")

        // 한글 입력 행
        val surnameHangulRow = dynamicNameInput.surnameInputs.joinToString(" ") { input ->
            "[${if (input.hangul.isEmpty()) "_" else input.hangul}]"
        }

        val surnameButtons = buildString {
            if (dynamicNameInput.surnameInputs.size > 1) append("[-] ")
            if (dynamicNameInput.surnameInputs.size < maxSurname) append("[+]")
        }.trim()

        logger.d("한글: $surnameHangulRow  $surnameButtons")

        // 한자 입력 행
        val surnameHanjaRow = dynamicNameInput.surnameInputs.joinToString(" ") { input ->
            "[${if (input.hanja.isEmpty()) "_" else input.hanja}]"
        }
        logger.d("한자: $surnameHanjaRow")

        logger.d("")

        // 이름 입력
        logger.d("이름 (${dynamicNameInput.givenNameInputs.size}/${maxGivenName}자):")

        // 한글 입력 행
        val givenNameHangulRow = dynamicNameInput.givenNameInputs.joinToString(" ") { input ->
            "[${if (input.hangul.isEmpty()) "_" else input.hangul}]"
        }

        val givenNameButtons = buildString {
            if (dynamicNameInput.givenNameInputs.size > 1) append("[-] ")
            if (dynamicNameInput.givenNameInputs.size < maxGivenName) append("[+]")
        }.trim()

        logger.d("한글: $givenNameHangulRow  $givenNameButtons")

        // 한자 입력 행
        val givenNameHanjaRow = dynamicNameInput.givenNameInputs.joinToString(" ") { input ->
            "[${if (input.hanja.isEmpty()) "_" else input.hanja}]"
        }
        logger.d("한자: $givenNameHanjaRow")

        logger.d("")
        logger.d("[+] 글자 추가, [-] 글자 제거")

        // 입력 도움말
        showInputHelp()

        // 현재 입력된 전체 이름 표시
        val fullName = getSurname() + getGivenName()
        val fullHanja = getSurnameHanja() + getGivenNameHanja()
        if (fullName.isNotEmpty() && fullName.all { it != '_' && !it.isWhitespace() }) {
            logger.d("")
            logger.d("현재 입력: $fullName ($fullHanja)")

            // 입력된 이름 분석
            analyzeInputName()
        }
    }

    private fun showOnboardingGuide() {
        val firstSteps = JsonLoader.userGuideStrings.onboardingGuide["first_steps"]
        if (firstSteps != null && firstSteps.steps != null) {
            logger.d("")
            logger.d("【${firstSteps.title}】")
            firstSteps.steps.forEach { step ->
                logger.d("• $step")
            }
        }
    }

    private fun showInputHelp() {
        logger.d("")
        logger.d("💡 입력 도움말:")
        logger.d("• 한글과 한자를 모두 정확히 입력해주세요")
        logger.d("• 한자를 모르시면 네이버 한자사전을 참고하세요")
        logger.d("• 복자성(복성)도 지원합니다 (예: 남궁, 선우)")
    }

    private fun analyzeInputName() {
        val givenNameHanja = getGivenNameHanja()

        // 한자가 입력된 경우만 분석
        if (givenNameHanja.isNotEmpty() && !givenNameHanja.contains("_")) {
            logger.d("")
            logger.d("【입력 이름 간단 분석】")

            // 각 한자의 의미 표시
            givenNameHanja.forEach { hanja ->
                JsonLoader.getHanjaMeaning(hanja.toString())?.let { hanjaInfo ->
                    hanjaInfo.origin?.let { origin ->
                        logger.d("• $hanja: $origin")
                    }
                }
            }

            // 긍정적인 의미 체크
            val hasPositiveMeaning = givenNameHanja.any { hanja ->
                JsonLoader.hanjaMeanings.positiveMeanings.any { positive ->
                    hanja.toString() == positive
                }
            }

            if (hasPositiveMeaning) {
                logger.d("✨ 긍정적인 의미를 담은 좋은 한자가 포함되어 있습니다")
            }
        }
    }

    override fun showDateTimePicker(onDateTimeSelected: (LocalDateTime) -> Unit) {
        logger.d("")
        logger.d("=== 생년월일시 선택 ===")
        logger.d("📅 날짜: [____년__월__일]")
        logger.d("⏰ 시간: [__시__분]")

        // 야자시 도움말
        JsonLoader.getHelpTooltip("yajasi")?.let { tooltip ->
            logger.d("")
            logger.d("💡 $tooltip")
        }

        logger.d("")

        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")
        logger.d("선택된 시간: ${birthDateTime.format(formatter)}")

        // 시간대별 메시지
        val hour = birthDateTime.hour
        JsonLoader.getTimeBasedMessage(hour)?.let { timeMessage ->
            logger.d("⏰ $timeMessage")
        }

        onDateTimeSelected(birthDateTime)
    }

    override fun showYajasiOption() {
        logger.d("")
        logger.d("${if (useYajasi) "☑" else "☐"} 야자시 적용 (23:30 이후 출생)")
        logger.d("   └─ 다음날 일진으로 계산됩니다")

        if (birthDateTime.hour >= 23 && birthDateTime.minute >= 30) {
            logger.d("")
            logger.d("⚠️ 현재 입력된 시간은 야자시입니다. 야자시 적용을 권장합니다.")
        }
    }

    override fun validateInput() : Boolean {
        var isValid = true
        val errors = mutableListOf<String>()

        if (profileName.isEmpty()) {
            errors.add("프로필명을 입력하세요")
            isValid = false
        }

        // 동적 입력 검증
        val surname = getSurname()
        val surnameHanja = getSurnameHanja()
        val givenName = getGivenName()
        val givenNameHanja = getGivenNameHanja()

        if (surname.isEmpty() || surname.contains("_")) {
            errors.add("성씨(한글)를 모두 입력하세요")
            isValid = false
        }
        if (surnameHanja.isEmpty() || surnameHanja.contains("_")) {
            errors.add("성씨(한자)를 모두 입력하세요")
            isValid = false
        }
        if (givenName.isEmpty() || givenName.contains("_")) {
            errors.add("이름(한글)을 모두 입력하세요")
            isValid = false
        }
        if (givenNameHanja.isEmpty() || givenNameHanja.contains("_")) {
            errors.add("이름(한자)를 모두 입력하세요")
            isValid = false
        }

        // 각 입력의 유효성 검증
        dynamicNameInput.surnameInputs.forEach { input ->
            val validated = input.validate()
            if (!validated.isValid) {
                errors.add("성 ${input.position + 1}번째: ${validated.errorMessage}")
                isValid = false
            }
        }

        dynamicNameInput.givenNameInputs.forEach { input ->
            val validated = input.validate()
            if (!validated.isValid) {
                errors.add("이름 ${input.position + 1}번째: ${validated.errorMessage}")
                isValid = false
            }
        }

        if (!isValid) {
            errors.forEach { showError(it) }

            // 에러 해결 도움말
            showErrorHelp(errors)
        } else {
            // 입력 완료 축하
            logger.d("")
            logger.d("✅ 모든 정보가 올바르게 입력되었습니다!")
        }

        return isValid
    }

    private fun showErrorHelp(errors: List<String>) {
        logger.d("")
        logger.d("【입력 오류 해결 방법】")

        if (errors.any { it.contains("한글") }) {
            logger.d("• 한글은 완성된 글자로 입력하세요 (예: 김, 이, 박)")
        }
        if (errors.any { it.contains("한자") }) {
            logger.d("• 한자는 정확한 한자를 입력하세요")
            logger.d("• 한자를 모르시면 온라인 한자사전을 이용하세요")
        }

        // 에러 메시지 매핑
        val errorGuide = JsonLoader.userGuideStrings.errorMessages["input_errors"]
        errors.forEach { error ->
            when {
                error.contains("프로필명") -> errorGuide?.get("empty_name")?.let {
                    logger.d("• $it")
                }
                error.contains("한글") -> errorGuide?.get("invalid_hangul")?.let {
                    logger.d("• $it")
                }
                error.contains("한자") -> errorGuide?.get("invalid_hanja")?.let {
                    logger.d("• $it")
                }
            }
        }
    }

    override fun getProfileName(): String = profileName
    override fun getSurname(): String = dynamicNameInput.surnameInputs.joinToString("") { it.hangul }
    override fun getSurnameHanja(): String = dynamicNameInput.surnameInputs.joinToString("") { it.hanja }
    override fun getGivenName(): String = dynamicNameInput.givenNameInputs.joinToString("") { it.hangul }
    override fun getGivenNameHanja(): String = dynamicNameInput.givenNameInputs.joinToString("") { it.hanja }
    override fun getBirthDateTime(): LocalDateTime = birthDateTime
    override fun getUseYajasi(): Boolean = useYajasi

    override fun showError(message: String) {
        logger.e("❌ 입력 오류: $message")
    }

    override fun showSuccess(message: String) {
        logger.d("✅ $message")

        // 프로필 생성 성공 축하
        logger.d("")
        logger.d("🎉 축하합니다! 프로필이 생성되었습니다!")
        logger.d("🌱 이제 이름봄과 함께 최고의 이름을 찾아보세요!")
    }

    override fun clearInputs() {
        dynamicNameInput = DynamicNameInput.createDefault()
        profileName = ""
        birthDateTime = LocalDateTime.now()
        useYajasi = false
        logger.d("📝 입력 폼이 초기화되었습니다")
    }

    // 글자 추가/제거 시뮬레이션
    fun simulateAddSurnameChar() {
        if (dynamicNameInput.canAddSurnameChar()) {
            dynamicNameInput = dynamicNameInput.addSurnameChar()
            logger.d("성씨 글자 추가됨 (현재 ${dynamicNameInput.surnameInputs.size}자)")
            showDynamicNameInput(2, 4)
        } else {
            logger.d("성씨는 최대 2자까지만 가능합니다")
        }
    }

    fun simulateRemoveSurnameChar() {
        if (dynamicNameInput.surnameInputs.size > 1) {
            dynamicNameInput = dynamicNameInput.removeSurnameChar()
            logger.d("성씨 글자 제거됨 (현재 ${dynamicNameInput.surnameInputs.size}자)")
            showDynamicNameInput(2, 4)
        } else {
            logger.d("성씨는 최소 1자는 필요합니다")
        }
    }

    fun simulateAddGivenNameChar() {
        if (dynamicNameInput.canAddGivenNameChar()) {
            dynamicNameInput = dynamicNameInput.addGivenNameChar()
            logger.d("이름 글자 추가됨 (현재 ${dynamicNameInput.givenNameInputs.size}자)")
            showDynamicNameInput(2, 4)
        } else {
            logger.d("이름은 최대 4자까지만 가능합니다")
        }
    }

    fun simulateRemoveGivenNameChar() {
        if (dynamicNameInput.givenNameInputs.size > 1) {
            dynamicNameInput = dynamicNameInput.removeGivenNameChar()
            logger.d("이름 글자 제거됨 (현재 ${dynamicNameInput.givenNameInputs.size}자)")
            showDynamicNameInput(2, 4)
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
        showDynamicNameInput(2, 4)
    }

    // 테스트용 입력 설정 메서드
    fun setTestInput(
        profileName: String = "나",
        surname: String = "김",
        surnameHanja: String = "金",
        givenName: String = "민수",
        givenNameHanja: String = "民秀",
        birthDateTime: LocalDateTime = LocalDateTime.of(1990, 1, 1, 12, 0),
        useYajasi: Boolean = false
    ) {
        this.profileName = profileName
        this.birthDateTime = birthDateTime
        this.useYajasi = useYajasi

        // 동적 입력 생성
        val surnameInputs = surname.mapIndexed { index, char ->
            CharacterInput(
                position = index,
                isSurname = true,
                hangul = char.toString(),
                hanja = surnameHanja.getOrNull(index)?.toString() ?: "",
                inputType = CharacterInputType.CHARACTER
            )
        }

        val givenNameInputs = givenName.mapIndexed { index, char ->
            CharacterInput(
                position = index,
                isSurname = false,
                hangul = char.toString(),
                hanja = givenNameHanja.getOrNull(index)?.toString() ?: "",
                inputType = CharacterInputType.CHARACTER
            )
        }

        dynamicNameInput = DynamicNameInput(
            surnameInputs = surnameInputs,
            givenNameInputs = givenNameInputs
        )

        logger.d("테스트 데이터 입력됨:")
        logger.d("- 프로필명: $profileName")
        logger.d("- 이름: ${surname}${givenName} (${surnameHanja}${givenNameHanja})")
        showDynamicNameInput(2, 4)
    }
}