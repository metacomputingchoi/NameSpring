// model/data/UiState.kt
package com.ssc.namespring.model.data

import com.ssc.namingengine.data.GeneratedName

/**
 * UI 상태를 표현하는 데이터 클래스들
 * MVVM 패턴 적용 시 ViewModel에서 사용
 */

/**
 * 공통 UI 상태
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

/**
 * 메인 화면 UI 상태
 */
data class MainScreenState(
    val profile: Profile? = null,
    val theme: Theme? = null,
    val allProfiles: List<Profile> = emptyList(),
    val currentProfileIndex: Int = 0,
    val nameFeatures: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 작명 결과 UI 상태
 */
data class NamingResultState(
    val names: List<GeneratedName> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 0,
    val sortType: String = "SCORE",
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 평가 결과 UI 상태
 */
data class EvaluationResultState(
    val report: EvaluationReport? = null,
    val theme: Theme? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)