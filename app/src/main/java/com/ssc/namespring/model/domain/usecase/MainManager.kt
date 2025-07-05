// model/domain/usecase/MainManager.kt
package com.ssc.namespring.model.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.domain.entity.Profile

class MainMagager {
    private val _uiState = MutableLiveData<MainUiState>()
    val uiState: LiveData<MainUiState> = _uiState

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        ProfileManager.getCurrentProfile()?.let { profile ->
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
                theme = if (profile.isEvaluated()) {  // isEvaluated() 사용
                    profile.getScoreThemeColor()
                } else {
                    Profile.ScoreTheme.NOT_EVALUATED  // NOT_EVALUATED로 변경
                }
            )
        }
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

    fun hasCurrentProfile(): Boolean = ProfileManager.getCurrentProfile() != null
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
