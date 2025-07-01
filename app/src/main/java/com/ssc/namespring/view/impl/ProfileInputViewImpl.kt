// view/impl/ProfileInputViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.DynamicNameInput
import com.ssc.namespring.model.data.CharacterInput
import com.ssc.namespring.model.data.CharacterInputType
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

        // 현재 입력된 전체 이름 표시
        val fullName = getSurname() + getGivenName()
        val fullHanja = getSurnameHanja() + getGivenNameHanja()
        if (fullName.isNotEmpty() && fullName.all { it != '_' && !it.isWhitespace() }) {
            logger.d("")
            logger.d("현재 입력: $fullName ($fullHanja)")
        }
    }

    override fun showDateTimePicker(onDateTimeSelected: (LocalDateTime) -> Unit) {
        logger.d("")
        logger.d("=== 생년월일시 선택 ===")
        logger.d("📅 날짜: [____년__월__일]")
        logger.d("⏰ 시간: [__시__분]")
        logger.d("")

        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")
        logger.d("선택된 시간: ${birthDateTime.format(formatter)}")
        onDateTimeSelected(birthDateTime)
    }

    override fun showYajasiOption() {
        logger.d("")
        logger.d("${if (useYajasi) "☑" else "☐"} 야자시 적용 (23:30 이후 출생)")
        logger.d("   └─ 다음날 일진으로 계산됩니다")
    }

    override fun validateInput(): Boolean {
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
        }

        return isValid
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