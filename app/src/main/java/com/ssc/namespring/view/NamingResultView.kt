// view/NamingResultView.kt
package com.ssc.namespring.view

import com.ssc.namingengine.data.GeneratedName

/**
 * 작명 결과 화면 View 인터페이스
 *
 * UI 개발 가이드:
 * 1. 카드형 이름 목록
 * 2. 새싹 즐겨찾기 버튼
 * 3. 정렬 및 페이지네이션
 */
interface NamingResultView {

    /**
     * 이름 카드 목록 표시
     *
     * 카드 구성:
     * - 이름 (한글/한자)
     * - 이름봄 점수 (새싹 아이콘 레벨)
     * - 핵심 특징 2-3개
     * - 새싹 즐겨찾기 버튼
     */
    fun showNameCards(names: List<GeneratedName>)

    /**
     * 즐겨찾기 버튼 상태 표시
     * - true: 채워진 새싹 (⭐/🌱)
     * - false: 빈 새싹 (☆/🌰)
     */
    fun showFavoriteButton(name: GeneratedName, isFavorite: Boolean)

    /**
     * 정렬 옵션 표시
     * - 점수순 (기본)
     * - 가나다순
     */
    fun showSortOptions()

    /**
     * 페이지네이션 표시
     * - 현재 페이지 / 전체 페이지
     * - 이전/다음 버튼
     */
    fun showPagination(currentPage: Int, totalPages: Int)

    /**
     * 로딩 상태 표시
     */
    fun showLoading(isLoading: Boolean)

    /**
     * 에러 메시지 표시
     */
    fun showError(message: String)

    /**
     * 빈 결과 표시
     * "조건에 맞는 이름이 없습니다" 메시지와 함께
     * 조건 완화 제안
     */
    fun showEmptyResult()

    /**
     * 즐겨찾기 상태 업데이트
     * 애니메이션 효과 권장
     */
    fun updateFavoriteStatus(name: GeneratedName, isFavorite: Boolean)
}