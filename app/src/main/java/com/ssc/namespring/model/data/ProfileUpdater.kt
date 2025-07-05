// model/data/ProfileUpdater.kt
package com.ssc.namespring.model.data

import android.util.Log
import com.ssc.namingengine.data.GeneratedName
import com.ssc.namingengine.data.analysis.NameAnalysisInfo

object ProfileUpdater {
    fun updateFromGeneratedName(profile: Profile, generatedName: GeneratedName) {
        profile.evaluatedName = generatedName
        profile.nameBomScore = ProfileScoreCalculator.calculateNamebomScore(generatedName)

        generatedName.analysisInfo?.let { analysisInfo ->
            profile.sajuInfo = SajuInfo.fromAnalysisInfo(analysisInfo)
            profile.ohaengInfo = extractOhaengInfo(analysisInfo)
        }

        profile.updatedAt = System.currentTimeMillis()
        Log.d("Profile", "updateFromGeneratedName - 점수: ${profile.nameBomScore}, 오행: ${profile.ohaengInfo}")
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