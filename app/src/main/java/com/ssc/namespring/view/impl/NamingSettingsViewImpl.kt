// view/impl/NamingSettingsViewImpl.kt
package com.ssc.namespring.view.impl

import android.app.Activity
import com.ssc.namespring.model.data.*
import com.ssc.namespring.utils.logger.AndroidLogger
import com.ssc.namespring.view.NamingSettingsView

class NamingSettingsViewImpl(private val activity: Activity) : NamingSettingsView {

    private val logger = AndroidLogger("NamingSettingsView")

    private var filterMode = FilterMode.DETAIL
    private val filters = mutableListOf<CharacterFilter>()

    override fun showDynamicFilterInput() {
        logger.d("=== 작명 조건 설정 ===")
        logger.d("")
        logger.d("글자별 조건 설정:")
        logger.d("┌─────┬─────┬─────┬─────┐")
        logger.d("│ 성  │이름1│이름2│이름3│")
        logger.d("├─────┼─────┼─────┼─────┤")
        logger.d("│ [_] │ [_] │ [+] │     │")
        logger.d("└─────┴─────┴─────┴─────┘")
        logger.d("")
        logger.d("각 위치 클릭 시 선택 가능:")
        logger.d("- 초성 (ㄱ~ㅎ)")
        logger.d("- 글자 (특정 한글)")
        logger.d("- 한자 (특정 한자)")
        logger.d("- 빈칸 (자유 생성)")
        logger.d("")
        logger.d("[+] 버튼으로 글자 추가 (최대 성 2자, 이름 4자)")
    }

    override fun showFilterModeToggle() {
        logger.d("")
        logger.d("필터 모드: [간편] [상세] ← 현재: ${if (filterMode == FilterMode.SIMPLE) "간편" else "상세"}")
    }

    override fun getFilterSettings(): NameFilter {
        return NameFilter(filters.toList(), filterMode)
    }

    override fun showSimpleMode() {
        filterMode = FilterMode.SIMPLE
        logger.d("✨ 간편 모드 활성화 - 모든 좋은 이름을 자동으로 생성합니다")
    }

    override fun showDetailMode() {
        filterMode = FilterMode.DETAIL
        logger.d("🔍 상세 모드 활성화 - 세부 조건을 설정할 수 있습니다")
    }

    override fun validateFilters(): Boolean {
        var isValid = true
        filters.forEach { filter ->
            if (!filter.isValid()) {
                showError("${filter.position + 1}번째 글자 조건이 올바르지 않습니다")
                isValid = false
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
        filters.clear()
        filters.add(CharacterFilter(0, FilterType.CHARACTER, surname, surnameHanja))
        filters.add(CharacterFilter(1, FilterType.EMPTY))
        filters.add(CharacterFilter(2, FilterType.EMPTY))
        logger.d("테스트 필터 설정: ${surname}○○")
    }
}