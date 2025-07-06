// model/domain/service/utils/ProfileUpdater.kt
package com.ssc.namespring.model.domain.service.utils

import android.util.Log
import com.ssc.namespring.model.common.utils.fromAnalysisInfo
import com.ssc.namespring.model.domain.entity.OhaengInfo
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SajuInfo
import com.ssc.namespring.model.domain.service.evaluation.ProfileScoreCalculator
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.data.analysis.NameAnalysisInfo

object ProfileUpdater {
    private const val TAG = "ProfileUpdater"

    fun updateFromGeneratedName(profile: Profile, generatedName: GeneratedName) {
        Log.d(TAG, "updateFromGeneratedName 시작")

        // GeneratedName 저장
        profile.updateEvaluatedName(generatedName)

        // 점수 계산
        profile.nameBomScore = ProfileScoreCalculator.calculateNamebomScore(generatedName)

        // analysisInfo가 있을 때만 사주/오행 정보 업데이트
        generatedName.analysisInfo?.let { analysisInfo ->
            Log.d(TAG, "analysisInfo 있음, 사주/오행 정보 업데이트")
            profile.sajuInfo = SajuInfo.fromAnalysisInfo(analysisInfo)
            profile.ohaengInfo = extractOhaengInfo(analysisInfo)

            Log.d(TAG, "업데이트된 sajuInfo: ${profile.sajuInfo?.fourPillars}")
            Log.d(TAG, "업데이트된 ohaengInfo: ${profile.ohaengInfo}")
        } ?: Log.w(TAG, "analysisInfo가 null입니다")

        profile.updatedAt = System.currentTimeMillis()

        Log.d(TAG, "updateFromGeneratedName 완료")
    }

    private fun extractOhaengInfo(analysisInfo: NameAnalysisInfo): OhaengInfo {
        val sajuOhaengCount = analysisInfo.sajuInfo.sajuOhaengCount
        return OhaengInfo(
            wood = sajuOhaengCount["木"] ?: 0,
            fire = sajuOhaengCount["火"] ?: 0,
            earth = sajuOhaengCount["土"] ?: 0,
            metal = sajuOhaengCount["金"] ?: 0,
            water = sajuOhaengCount["水"] ?: 0
        )
    }
}