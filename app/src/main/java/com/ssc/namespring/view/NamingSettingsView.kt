// view/NamingSettingsView.kt
package com.ssc.namespring.view

import com.ssc.namespring.model.data.NameFilter

/**
 * 작명 설정 화면 View 인터페이스
 *
 * UI 개발 가이드:
 * 1. 동적 필터 입력: '+' 버튼으로 글자 추가
 * 2. 각 위치별 조건 설정 (초성/글자/한자/빈칸)
 * 3. 모드 선택 (간편/상세)
 */
interface NamingSettingsView {

    /**
     * 동적 필터 입력 UI 표시
     *
     * 구현 가이드:
     * - 기본: 성 1자 + 이름 1자
     * - '+' 버튼으로 글자 추가 (성 최대 2자, 이름 최대 4자)
     * - 각 입력란에 드롭다운: 초성/글자/한자/빈칸 선택
     * - 선택에 따라 적절한 입력 UI 표시
     */
    fun showDynamicFilterInput()

    /**
     * 필터 모드 토글 표시
     * - 간편 모드: 모든 필터 OFF
     * - 상세 모드: 세부 조건 설정 가능
     */
    fun showFilterModeToggle()

    /**
     * 현재 설정된 필터 가져오기
     */
    fun getFilterSettings(): NameFilter

    /**
     * 간편 모드 UI 표시
     * 필터 입력란 비활성화
     */
    fun showSimpleMode()

    /**
     * 상세 모드 UI 표시
     * 필터 입력란 활성화
     */
    fun showDetailMode()

    /**
     * 필터 유효성 검증
     * - 초성: ㄱ~ㅎ만 허용
     * - 글자: 한글만 허용
     * - 한자: 한자만 허용
     */
    fun validateFilters(): Boolean

    /**
     * 에러 메시지 표시
     */
    fun showError(message: String)

    /**
     * 현재 필터 설명 표시
     * 예: "김○○ (성은 김씨로 고정, 이름은 자유)"
     */
    fun showFilterDescription(description: String)

    /**
     * 로딩 상태 표시
     */
    fun showLoading(isLoading: Boolean)

    /**
     * 생성 시작 버튼 활성화/비활성화
     */
    fun enableGenerateButton(enabled: Boolean)
}