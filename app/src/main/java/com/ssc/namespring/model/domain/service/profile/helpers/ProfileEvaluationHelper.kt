// model/domain/service/profile/helpers/ProfileEvaluationHelper.kt
package com.ssc.namespring.model.domain.service.profile.helpers

import android.util.Log
import com.ssc.namespring.model.common.utils.fromAnalysisInfo
import com.ssc.namespring.model.domain.entity.*
import com.ssc.namespring.model.domain.service.evaluation.ProfileScoreCalculator
import com.ssc.namingengine.NamingEngine
import com.ssc.namingengine.data.GeneratedName
import java.time.ZoneId

internal class ProfileEvaluationHelper(
    private val namingEngine: NamingEngine
) {
    companion object {
        private const val TAG = "ProfileEvaluationHelper"
    }

    fun evaluateFullProfile(
        profile: Profile,
        nameInput: String
    ): Profile? {
        return try {
            val birthDateTime = profile.birthDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            val evaluatedNames = namingEngine.generateNames(
                userInput = nameInput,
                birthDateTime = birthDateTime,
                useYajasi = profile.isYajaTime,
                verbose = true,
                withoutFilter = true
            )

            Log.d(TAG, "GeneratedName 개수: ${evaluatedNames.size}")
            evaluatedNames.firstOrNull()?.let { generatedName ->
                processGeneratedName(profile, generatedName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "전체 평가 실패", e)
            null
        }
    }

    private fun processGeneratedName(
        profile: Profile,
        generatedName: GeneratedName
    ): Profile {
        Log.d(TAG, "GeneratedName 받음:")
        Log.d(TAG, "  - combinedHanja: ${generatedName.combinedHanja}")

        val calculatedScore = ProfileScoreCalculator.calculateNamebomScore(generatedName)
        Log.d(TAG, "계산된 점수: $calculatedScore")

        // 직접 필드 업데이트
        profile.nameBomScore = calculatedScore
        profile.updatedAt = System.currentTimeMillis()

        // 사주/오행 정보 업데이트
        generatedName.analysisInfo?.let { analysisInfo ->
            profile.sajuInfo = SajuInfo.fromAnalysisInfo(analysisInfo)
            profile.ohaengInfo = OhaengInfo(
                wood = analysisInfo.sajuInfo.sajuOhaengCount["木"] ?: 0,
                fire = analysisInfo.sajuInfo.sajuOhaengCount["火"] ?: 0,
                earth = analysisInfo.sajuInfo.sajuOhaengCount["土"] ?: 0,
                metal = analysisInfo.sajuInfo.sajuOhaengCount["金"] ?: 0,
                water = analysisInfo.sajuInfo.sajuOhaengCount["水"] ?: 0
            )
            Log.d(TAG, "오행 정보 업데이트: ${profile.ohaengInfo}")
        }

        return profile
    }
}