// view/impl/NamingSettingsViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.*
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.NamingSettingsView

class NamingSettingsViewImpl(private val activity: Activity) : NamingSettingsView {

    private val logger = AndroidLogger("NamingSettingsView")

    private var filterMode = FilterMode.DETAIL
    private var dynamicFilterInput = DynamicFilterInput(
        surnameFilters = listOf(
            FilterInput(0, true, filterType = FilterInputType.FIXED)
        ),
        givenNameFilters = listOf(
            FilterInput(0, false, filterType = FilterInputType.EMPTY)
        )
    )

    override fun showDynamicFilterInput() {
        logger.d("=== 작명 조건 설정 ===")
        logger.d("")

        // 성씨 필터 표시
        logger.d("성 (${dynamicFilterInput.surnameFilters.size}/2자):")

        // 필터 내용 행
        val surnameFilters = dynamicFilterInput.surnameFilters.joinToString(" ") { filter ->
            when (filter.filterType) {
                FilterInputType.FIXED -> "[${filter.hangul}/${filter.hanja}]"
                FilterInputType.INITIAL_SOUND -> "[${filter.hangul}__]"
                FilterInputType.CHARACTER -> "[${filter.hangul}_]"
                FilterInputType.HANJA_ONLY -> "[_/${filter.hanja}]"
                FilterInputType.EMPTY -> "[___]"
            }
        }

        val surnameButtons = buildString {
            if (dynamicFilterInput.surnameFilters.size > 1) append("[-] ")
            if (dynamicFilterInput.surnameFilters.size < 2) append("[+]")
        }.trim()

        logger.d("$surnameFilters  $surnameButtons")

        logger.d("")

        // 이름 필터 표시
        logger.d("이름 (${dynamicFilterInput.givenNameFilters.size}/4자):")

        // 필터 내용 행
        val givenNameFilters = dynamicFilterInput.givenNameFilters.joinToString(" ") { filter ->
            when (filter.filterType) {
                FilterInputType.INITIAL_SOUND -> "[${filter.hangul}__]"
                FilterInputType.CHARACTER -> "[${filter.hangul}_]"
                FilterInputType.HANJA_ONLY -> "[_/${filter.hanja}]"
                FilterInputType.EMPTY -> "[___]"
                FilterInputType.FIXED -> "[${filter.hangul}]"
            }
        }

        val givenNameButtons = buildString {
            if (dynamicFilterInput.givenNameFilters.size > 1) append("[-] ")
            if (dynamicFilterInput.givenNameFilters.size < 4) append("[+]")
        }.trim()

        logger.d("$givenNameFilters  $givenNameButtons")

        logger.d("")
        logger.d("각 위치 클릭 시 선택 가능:")
        logger.d("- 초성 (ㄱ~ㅎ)")
        logger.d("- 글자 (특정 한글)")
        logger.d("- 한자 (특정 한자)")
        logger.d("- 빈칸 (자유 생성)")
        logger.d("")
        logger.d("[+] 글자 추가, [-] 글자 제거")

        // 현재 필터 설명
        showFilterDescription(dynamicFilterInput.toNameFilter().getFilterDescription())
    }

    override fun showFilterModeToggle() {
        logger.d("")
        logger.d("필터 모드: ${if (filterMode == FilterMode.SIMPLE) "[간편]" else "(간편)"} ${if (filterMode == FilterMode.DETAIL) "[상세]" else "(상세)"}")
    }

    override fun getFilterSettings(): NameFilter {
        return dynamicFilterInput.toNameFilter()
    }

    override fun showSimpleMode() {
        filterMode = FilterMode.SIMPLE
        dynamicFilterInput = dynamicFilterInput.copy(filterMode = FilterMode.SIMPLE)
        logger.d("✨ 간편 모드 활성화 - 모든 좋은 이름을 자동으로 생성합니다")
    }

    override fun showDetailMode() {
        filterMode = FilterMode.DETAIL
        dynamicFilterInput = dynamicFilterInput.copy(filterMode = FilterMode.DETAIL)
        logger.d("🔍 상세 모드 활성화 - 세부 조건을 설정할 수 있습니다")
    }

    override fun validateFilters(): Boolean {
        var isValid = true

        // 성씨 필터 검증
        dynamicFilterInput.surnameFilters.forEachIndexed { index, filter ->
            when (filter.filterType) {
                FilterInputType.INITIAL_SOUND -> {
                    if (filter.hangul.isNotEmpty() && !filter.hangul.matches(Regex("[ㄱ-ㅎ]"))) {
                        showError("성 ${index + 1}번째: 초성만 입력 가능합니다")
                        isValid = false
                    }
                }
                FilterInputType.CHARACTER, FilterInputType.FIXED -> {
                    if (filter.hangul.isNotEmpty() && !filter.hangul.matches(Regex("[가-힣]"))) {
                        showError("성 ${index + 1}번째: 한글만 입력 가능합니다")
                        isValid = false
                    }
                    if (filter.hanja.isNotEmpty() && !filter.hanja.matches(Regex("[\u4e00-\u9fff]"))) {
                        showError("성 ${index + 1}번째: 올바른 한자를 입력하세요")
                        isValid = false
                    }
                }
                FilterInputType.HANJA_ONLY -> {
                    if (filter.hanja.isEmpty() || !filter.hanja.matches(Regex("[\u4e00-\u9fff]"))) {
                        showError("성 ${index + 1}번째: 한자를 입력하세요")
                        isValid = false
                    }
                }
                FilterInputType.EMPTY -> {} // 빈칸은 항상 유효
            }
        }

        // 이름 필터 검증
        dynamicFilterInput.givenNameFilters.forEachIndexed { index, filter ->
            when (filter.filterType) {
                FilterInputType.INITIAL_SOUND -> {
                    if (filter.hangul.isNotEmpty() && !filter.hangul.matches(Regex("[ㄱ-ㅎ]"))) {
                        showError("이름 ${index + 1}번째: 초성만 입력 가능합니다")
                        isValid = false
                    }
                }
                FilterInputType.CHARACTER -> {
                    if (filter.hangul.isNotEmpty() && !filter.hangul.matches(Regex("[가-힣]"))) {
                        showError("이름 ${index + 1}번째: 한글만 입력 가능합니다")
                        isValid = false
                    }
                }
                FilterInputType.HANJA_ONLY -> {
                    if (filter.hanja.isEmpty() || !filter.hanja.matches(Regex("[\u4e00-\u9fff]"))) {
                        showError("이름 ${index + 1}번째: 한자를 입력하세요")
                        isValid = false
                    }
                }
                FilterInputType.EMPTY, FilterInputType.FIXED -> {} // 빈칸과 고정값은 항상 유효
            }
        }

        return isValid
    }

    override fun showError(message: String) {
        logger.e("❌ 설정 오류: $message")
    }

    override fun showFilterDescription(description: String) {
        logger.d("📝 현재 조건: $description")
    }

    override fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            logger.d("⏳ 조건 검증 중...")
        }
    }

    override fun enableGenerateButton(enabled: Boolean) {
        if (enabled) {
            logger.d("[🌱 이름 생성하기] 버튼 활성화")
        } else {
            logger.d("[⏳ 생성 중...] 버튼 비활성화")
        }
    }

    // 테스트용 필터 설정
    fun setTestFilter(surname: String = "김", surnameHanja: String = "金") {
        dynamicFilterInput = DynamicFilterInput(
            surnameFilters = listOf(
                FilterInput(0, true, surname, surnameHanja, FilterInputType.FIXED)
            ),
            givenNameFilters = listOf(
                FilterInput(0, false, filterType = FilterInputType.EMPTY),
                FilterInput(1, false, filterType = FilterInputType.EMPTY)
            ),
            filterMode = filterMode
        )
        logger.d("테스트 필터 설정: ${surname}○○")
        showDynamicFilterInput()
    }

    // 글자 추가/제거 시뮬레이션
    fun simulateAddSurnameChar() {
        if (dynamicFilterInput.surnameFilters.size < 2) {
            val newFilters = dynamicFilterInput.surnameFilters +
                    FilterInput(dynamicFilterInput.surnameFilters.size, true, filterType = FilterInputType.EMPTY)
            dynamicFilterInput = dynamicFilterInput.copy(surnameFilters = newFilters)
            logger.d("성씨 글자 추가됨 (현재 ${dynamicFilterInput.surnameFilters.size}자)")
            showDynamicFilterInput()
        } else {
            logger.d("성씨는 최대 2자까지만 가능합니다")
        }
    }

    fun simulateRemoveSurnameChar() {
        if (dynamicFilterInput.surnameFilters.size > 1) {
            val newFilters = dynamicFilterInput.surnameFilters.dropLast(1)
            dynamicFilterInput = dynamicFilterInput.copy(surnameFilters = newFilters)
            logger.d("성씨 글자 제거됨 (현재 ${dynamicFilterInput.surnameFilters.size}자)")
            showDynamicFilterInput()
        } else {
            logger.d("성씨는 최소 1자는 필요합니다")
        }
    }

    fun simulateAddGivenNameChar() {
        if (dynamicFilterInput.givenNameFilters.size < 4) {
            val newFilters = dynamicFilterInput.givenNameFilters +
                    FilterInput(dynamicFilterInput.givenNameFilters.size, false, filterType = FilterInputType.EMPTY)
            dynamicFilterInput = dynamicFilterInput.copy(givenNameFilters = newFilters)
            logger.d("이름 글자 추가됨 (현재 ${dynamicFilterInput.givenNameFilters.size}자)")
            showDynamicFilterInput()
        } else {
            logger.d("이름은 최대 4자까지만 가능합니다")
        }
    }

    fun simulateRemoveGivenNameChar() {
        if (dynamicFilterInput.givenNameFilters.size > 1) {
            val newFilters = dynamicFilterInput.givenNameFilters.dropLast(1)
            dynamicFilterInput = dynamicFilterInput.copy(givenNameFilters = newFilters)
            logger.d("이름 글자 제거됨 (현재 ${dynamicFilterInput.givenNameFilters.size}자)")
            showDynamicFilterInput()
        } else {
            logger.d("이름은 최소 1자는 필요합니다")
        }
    }

    // 필터 타입 변경 시뮬레이션
    fun simulateChangeFilterType(isSurname: Boolean, position: Int, newType: FilterInputType) {
        if (isSurname) {
            val updated = dynamicFilterInput.surnameFilters.mapIndexed { index, filter ->
                if (index == position) filter.copy(filterType = newType) else filter
            }
            dynamicFilterInput = dynamicFilterInput.copy(surnameFilters = updated)
        } else {
            val updated = dynamicFilterInput.givenNameFilters.mapIndexed { index, filter ->
                if (index == position) filter.copy(filterType = newType) else filter
            }
            dynamicFilterInput = dynamicFilterInput.copy(givenNameFilters = updated)
        }

        val typeStr = when (newType) {
            FilterInputType.INITIAL_SOUND -> "초성"
            FilterInputType.CHARACTER -> "글자"
            FilterInputType.HANJA_ONLY -> "한자"
            FilterInputType.EMPTY -> "빈칸"
            FilterInputType.FIXED -> "고정"
        }

        logger.d("${if (isSurname) "성" else "이름"} ${position + 1}번째 조건 변경: $typeStr")
        showDynamicFilterInput()
    }

    // 필터 값 업데이트 시뮬레이션
    fun simulateUpdateFilterValue(isSurname: Boolean, position: Int, hangul: String = "", hanja: String = "") {
        if (isSurname) {
            val updated = dynamicFilterInput.surnameFilters.mapIndexed { index, filter ->
                if (index == position) filter.copy(hangul = hangul, hanja = hanja) else filter
            }
            dynamicFilterInput = dynamicFilterInput.copy(surnameFilters = updated)
        } else {
            val updated = dynamicFilterInput.givenNameFilters.mapIndexed { index, filter ->
                if (index == position) filter.copy(hangul = hangul, hanja = hanja) else filter
            }
            dynamicFilterInput = dynamicFilterInput.copy(givenNameFilters = updated)
        }

        logger.d("${if (isSurname) "성" else "이름"} ${position + 1}번째 값 업데이트")
        showDynamicFilterInput()
    }
}