// view/MainView.kt
package com.ssc.namespring.view

import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.data.SajuInfo
import com.ssc.namespring.model.data.Theme

/**
 * 메인 화면 View 인터페이스
 *
 * UI 개발 가이드:
 * 1. 상단: 프로필 정보와 이름봄 점수 표시
 * 2. 중앙: 사주 정보 시각화
 * 3. 하단: 4개의 주요 기능 버튼
 * 4. 배경: 점수에 따른 봄 테마 적용
 */
interface MainView {

    /**
     * 테마 적용
     * - 배경색/이미지 변경
     * - 새싹 아이콘 상태 변경
     * - 전체적인 UI 톤 조정
     */
    fun applyTheme(theme: Theme)

    /**
     * 프로필 정보 표시
     * 표시할 내용:
     * - 이름 (한글/한자)
     * - 프로필명
     * - 이름봄 점수 (새싹 아이콘과 함께)
     */
    fun showProfileInfo(profile: Profile)

    /**
     * 사주 정보 요약 표시
     * 표시할 내용:
     * - 사주팔자 시각화 (원형 차트 권장)
     * - 부족한 오행 강조
     * - 오행 균형도
     */
    fun showSajuSummary(sajuInfo: SajuInfo?)

    /**
     * 이름의 주요 특징 표시
     * 카드 형태로 2-3개 특징 표시
     */
    fun showNameFeatures(features: List<String>)

    /**
     * 기능 버튼 활성화
     * 4개 버튼: 작명, 평가, 비교, 기록
     * 각 버튼에 아이콘과 설명 포함
     */
    fun enableFeatureButtons()

    /**
     * 프로필 전환 UI 표시
     * - 좌우 스와이프 힌트
     * - 현재 프로필 인디케이터
     */
    fun showProfileSwitch(profiles: List<Profile>, currentIndex: Int)

    /**
     * 테마 전환 애니메이션
     * 부드러운 전환 효과 (fade, slide 등)
     */
    fun showThemeTransition(oldTheme: Theme, newTheme: Theme)

    /**
     * 에러 메시지 표시
     * 토스트나 스낵바 형태 권장
     */
    fun showError(message: String)

    /**
     * 로딩 상태 표시
     * 새싹이 자라는 애니메이션 권장
     */
    fun showLoading(isLoading: Boolean)
}