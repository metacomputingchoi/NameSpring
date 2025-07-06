// model/domain/usecase/MainManager.kt
package com.ssc.namespring.model.domain.usecase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.profile.ProfileEvaluationService
import com.ssc.namingengine.NamingEngine

class MainMagager {
    private val _uiState = MutableLiveData<MainUiState>()
    val uiState: LiveData<MainUiState> = _uiState

    private val profileManager: ProfileManager = ProfileManagerProvider.getInstance()

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        val currentProfile = profileManager.getCurrentProfile()
        if (currentProfile != null) {
            // 재평가 없이 현재 프로필 정보만 표시
            updateUIWithProfile(currentProfile)
        } else {
            Log.w("MainManager", "현재 프로필이 null입니다")
        }
    }

    private fun updateUIWithProfile(profile: Profile) {
        Log.d("MainManager", "현재 프로필 정보:")
        Log.d("MainManager", "  - profileName: ${profile.profileName}")
        Log.d("MainManager", "  - nameBomScore: ${profile.nameBomScore}")
        Log.d("MainManager", "  - evaluatedName 존재: ${profile.evaluatedName != null}")
        Log.d("MainManager", "  - evaluatedNameJson 길이: ${profile.evaluatedNameJson?.length}")

        // evaluatedName이 있으면 내용도 로그
        profile.evaluatedName?.let { generatedName ->
            Log.d("MainManager", "  - GeneratedName 내용:")
            Log.d("MainManager", "    - combinedHanja: ${generatedName.combinedHanja}")
            Log.d("MainManager", "    - combinedPronounciation: ${generatedName.combinedPronounciation}")
            Log.d("MainManager", "    - hanjaDetails 수: ${generatedName.hanjaDetails.size}")
            Log.d("MainManager", "    - analysisInfo 존재: ${generatedName.analysisInfo != null}")
        }

        _uiState.value = MainUiState(
            profileName = profile.profileName,
            scoreText = if (profile.isEvaluated()) profile.nameBomScore.toString() else "-",
            fullName = profile.getFullName(),
            birthInfo = profile.getBirthDateString(),
            ohaengInfo = getOhaengInfoText(profile),
            ohaengCounts = listOf(
                profile.ohaengInfo?.wood ?: 0,
                profile.ohaengInfo?.fire ?: 0,
                profile.ohaengInfo?.earth ?: 0,
                profile.ohaengInfo?.metal ?: 0,
                profile.ohaengInfo?.water ?: 0
            ),
            theme = if (profile.isEvaluated()) {
                profile.getScoreThemeColor()
            } else {
                Profile.ScoreTheme.NOT_EVALUATED
            }
        )
    }

    private fun getOhaengInfoText(profile: Profile): String {
        val ohaeng = profile.ohaengInfo ?: return "오행 정보를 계산 중입니다..."

        if (ohaeng.wood == 0 && ohaeng.fire == 0 && ohaeng.earth == 0 &&
            ohaeng.metal == 0 && ohaeng.water == 0) {
            return "오행 정보를 계산 중입니다..."
        }

        val lacking = ohaeng.getLackingOhaeng()
        val excess = ohaeng.getExcessOhaeng()

        return when {
            lacking.isNotEmpty() && excess.isNotEmpty() ->
                "부족한 오행: ${lacking.joinToString(", ")} · 많은 오행: ${excess.joinToString(", ")}"
            lacking.isNotEmpty() ->
                "부족한 오행: ${lacking.joinToString(", ")}"
            excess.isNotEmpty() ->
                "많은 오행: ${excess.joinToString(", ")}"
            else -> "오행이 균형 잡혀 있습니다"
        }
    }

    fun getCurrentProfile(): Profile? = profileManager.getCurrentProfile()

    fun hasCurrentProfile(): Boolean = profileManager.getCurrentProfile() != null
}

data class MainUiState(
    val profileName: String = "",
    val scoreText: String = "-",
    val fullName: String = "",
    val birthInfo: String = "",
    val ohaengInfo: String = "",
    val ohaengCounts: List<Int> = listOf(0, 0, 0, 0, 0),
    val theme: Profile.ScoreTheme = Profile.ScoreTheme.NOT_EVALUATED
)