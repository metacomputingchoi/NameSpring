// model/domain/service/evaluation/SajuEvaluator.kt
package com.ssc.namespring.model.domain.service.evaluation

import android.util.Log
import com.ssc.namespring.model.common.utils.fromAnalysisInfo
import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.model.domain.entity.OhaengInfo
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SajuInfo
import java.time.ZoneId
import java.util.Calendar

object SajuEvaluator {
    private const val TAG = "SajuEvaluator"

    fun evaluateSajuOnly(
        birthDate: Calendar,
        isYajaTime: Boolean,
        namingEngine: NamingEngine
    ): Pair<SajuInfo?, OhaengInfo?> {

        Log.d(TAG, "사주 평가 시작 - 날짜: ${birthDate.time}, 야자시: $isYajaTime")

        return try {
            val birthDateTime = birthDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            Log.d(TAG, "LocalDateTime 변환: $birthDateTime")

            val dummyNameInput = "[김/金][우/宇]"

            Log.d(TAG, "더미 이름으로 평가 시작: $dummyNameInput")

            val result = namingEngine.generateNames(
                userInput = dummyNameInput,
                birthDateTime = birthDateTime,
                useYajasi = isYajaTime,
                verbose = true,
                withoutFilter = true
            )

            Log.d(TAG, "generateNames 결과: ${result.size}개")

            result.firstOrNull()?.analysisInfo?.let { analysisInfo ->
                val sajuInfo = SajuInfo.fromAnalysisInfo(analysisInfo)

                val ohaengInfo = OhaengInfo(
                    wood = analysisInfo.sajuInfo.sajuOhaengCount["木"] ?: 0,
                    fire = analysisInfo.sajuInfo.sajuOhaengCount["火"] ?: 0,
                    earth = analysisInfo.sajuInfo.sajuOhaengCount["土"] ?: 0,
                    metal = analysisInfo.sajuInfo.sajuOhaengCount["金"] ?: 0,
                    water = analysisInfo.sajuInfo.sajuOhaengCount["水"] ?: 0
                )

                Log.d(TAG, "사주 평가 성공 - 사주: ${sajuInfo.fourPillars.joinToString(" ")}")
                Log.d(TAG, "오행 분포: $ohaengInfo")

                Pair(sajuInfo, ohaengInfo)
            } ?: run {
                Log.e(TAG, "analysisInfo가 null입니다")
                Pair(null, null)
            }

        } catch (e: Exception) {
            Log.e(TAG, "사주 평가 실패", e)
            e.printStackTrace()
            Pair(null, null)
        }
    }

    fun evaluateProfileSaju(profile: Profile, namingEngine: NamingEngine): Profile {
        val (sajuInfo, ohaengInfo) = evaluateSajuOnly(
            profile.birthDate,
            profile.isYajaTime,
            namingEngine
        )

        return profile.copy(
            sajuInfo = sajuInfo,
            ohaengInfo = ohaengInfo,
            updatedAt = System.currentTimeMillis()
        )
    }
}
