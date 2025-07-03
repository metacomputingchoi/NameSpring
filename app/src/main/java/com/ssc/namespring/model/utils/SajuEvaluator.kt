// model/utils/SajuEvaluator.kt
package com.ssc.namespring.model.utils

import android.util.Log
import com.ssc.namingengine.NamingEngine
import com.ssc.namingengine.data.analysis.component.SajuAnalysisInfo
import com.ssc.namespring.model.OhaengInfo
import com.ssc.namespring.model.Profile
import com.ssc.namespring.model.data.SajuInfo
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

/**
 * 사주 평가만을 담당하는 유틸리티 클래스
 * 이름 정보 없이 생년월일시만으로 사주 정보를 평가
 */
object SajuEvaluator {

    private const val TAG = "SajuEvaluator"

    /**
     * 생년월일시 정보만으로 사주를 평가
     * NamingEngine의 내부 SajuCalculator를 활용
     */
    fun evaluateSajuOnly(
        birthDate: Calendar,
        isYajaTime: Boolean,
        namingEngine: NamingEngine
    ): Pair<SajuInfo?, OhaengInfo?> {

        Log.d(TAG, "사주 평가 시작 - 날짜: ${birthDate.time}, 야자시: $isYajaTime")

        return try {
            // Calendar를 LocalDateTime으로 변환
            val birthDateTime = birthDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            Log.d(TAG, "LocalDateTime 변환: $birthDateTime")

            // 더미 이름으로 generateNames 호출
            val dummyNameInput = "[김/金][우/宇]"  // 실제 존재하는 한자 사용

            Log.d(TAG, "더미 이름으로 평가 시작: $dummyNameInput")

            val result = namingEngine.generateNames(
                userInput = dummyNameInput,
                birthDateTime = birthDateTime,
                useYajasi = isYajaTime,
                verbose = true,  // 디버깅을 위해 true로 변경
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

    /**
     * 프로필의 사주만 평가하여 업데이트
     */
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