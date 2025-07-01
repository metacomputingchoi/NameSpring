// view/impl/ProfileInputViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.ProfileInputView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProfileInputViewImpl(private val activity: Activity) : ProfileInputView {

    private val logger = AndroidLogger("ProfileInputView")

    // 임시 저장 변수들 (실제 UI에서는 EditText 등에서 가져옴)
    private var profileName = ""
    private var surname = ""
    private var surnameHanja = ""
    private var givenName = ""
    private var givenNameHanja = ""
    private var birthDateTime = LocalDateTime.now()
    private var useYajasi = false

    override fun showDynamicNameInput(maxSurname: Int, maxGivenName: Int) {
        logger.d("=== 이름 입력 폼 ===")
        logger.d("")
        logger.d("프로필명: [_______] (예: 나, 배우자, 첫째)")
        logger.d("")
        logger.d("성 (최대 ${maxSurname}자):")
        logger.d("┌─────┬─────┐")
        logger.d("│한글 │한자 │")
        logger.d("├─────┼─────┤")
        logger.d("│ [_] │ [_] │ [+]")
        logger.d("└─────┴─────┘")
        logger.d("")
        logger.d("이름 (최대 ${maxGivenName}자):")
        logger.d("┌─────┬─────┬─────┬─────┐")
        logger.d("│한글 │한글 │     │     │")
        logger.d("├─────┼─────┼─────┼─────┤")
        logger.d("│ [_] │ [_] │ [+] │     │")
        logger.d("├─────┼─────┼─────┼─────┤")
        logger.d("│한자 │한자 │     │     │")
        logger.d("└─────┴─────┴─────┴─────┘")
        logger.d("")
        logger.d("[+] 클릭으로 글자 추가")
    }

    override fun showDateTimePicker(onDateTimeSelected: (LocalDateTime) -> Unit) {
        logger.d("")
        logger.d("=== 생년월일시 선택 ===")
        logger.d("📅 날짜: [____년__월__일]")
        logger.d("⏰ 시간: [__시__분]")
        logger.d("")

        // 실제로는 DatePicker, TimePicker 다이얼로그 표시
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")
        logger.d("선택된 시간: ${birthDateTime.format(formatter)}")
        onDateTimeSelected(birthDateTime)
    }

    override fun showYajasiOption() {
        logger.d("")
        logger.d("☐ 야자시 적용 (23:30 이후 출생)")
        logger.d("   └─ 다음날 일진으로 계산됩니다")
    }

    override fun validateInput(): Boolean {
        var isValid = true
        val errors = mutableListOf<String>()

        if (profileName.isEmpty()) {
            errors.add("프로필명을 입력하세요")
            isValid = false
        }
        if (surname.isEmpty() || surnameHanja.isEmpty()) {
            errors.add("성씨(한글/한자)를 모두 입력하세요")
            isValid = false
        }
        if (givenName.isEmpty() || givenNameHanja.isEmpty()) {
            errors.add("이름(한글/한자)을 모두 입력하세요")
            isValid = false
        }
        if (surname.length != surnameHanja.length) {
            errors.add("성의 한글과 한자 글자 수가 일치하지 않습니다")
            isValid = false
        }
        if (givenName.length != givenNameHanja.length) {
            errors.add("이름의 한글과 한자 글자 수가 일치하지 않습니다")
            isValid = false
        }

        if (!isValid) {
            errors.forEach { showError(it) }
        }

        return isValid
    }

    override fun getProfileName(): String = profileName
    override fun getSurname(): String = surname
    override fun getSurnameHanja(): String = surnameHanja
    override fun getGivenName(): String = givenName
    override fun getGivenNameHanja(): String = givenNameHanja
    override fun getBirthDateTime(): LocalDateTime = birthDateTime
    override fun getUseYajasi(): Boolean = useYajasi

    override fun showError(message: String) {
        logger.e("❌ 입력 오류: $message")
    }

    override fun showSuccess(message: String) {
        logger.d("✅ $message")
    }

    override fun clearInputs() {
        profileName = ""
        surname = ""
        surnameHanja = ""
        givenName = ""
        givenNameHanja = ""
        birthDateTime = LocalDateTime.now()
        useYajasi = false
        logger.d("📝 입력 폼이 초기화되었습니다")
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
        this.surname = surname
        this.surnameHanja = surnameHanja
        this.givenName = givenName
        this.givenNameHanja = givenNameHanja
        this.birthDateTime = birthDateTime
        this.useYajasi = useYajasi

        logger.d("테스트 데이터 입력됨:")
        logger.d("- 프로필명: $profileName")
        logger.d("- 이름: ${surname}${givenName} (${surnameHanja}${givenNameHanja})")
    }
}