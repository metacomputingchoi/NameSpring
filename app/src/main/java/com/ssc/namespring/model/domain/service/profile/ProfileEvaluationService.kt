// model/domain/service/profile/ProfileEvaluationService.kt
package com.ssc.namespring.model.domain.service.profile

import android.util.Log
import com.google.gson.Gson
import com.ssc.namespring.model.common.utils.fromAnalysisInfo
import com.ssc.namingengine.NamingEngine
import com.ssc.namespring.model.domain.service.interfaces.EvaluationService
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.entity.CharInfo
import com.ssc.namespring.model.domain.entity.OhaengInfo
import com.ssc.namespring.model.domain.entity.SajuInfo
import com.ssc.namespring.model.domain.service.evaluation.SajuEvaluator
import com.ssc.namespring.model.domain.service.evaluation.ProfileScoreCalculator
import com.ssc.namespring.model.domain.service.utils.ProfileUpdater
import com.ssc.namingengine.data.GeneratedName
import java.time.ZoneId

class ProfileEvaluationService(
    private val namingEngine: NamingEngine
) : EvaluationService {

    companion object {
        private const val TAG = "ProfileEvaluationService"
        private val gson = Gson()
    }

    override fun evaluate(profile: Profile): Profile {
        val hasCompleteName = profile.givenName?.let { givenName ->
            givenName.charInfos.isNotEmpty() &&
                    givenName.charInfos.all { it.korean.isNotEmpty() && it.hanja.isNotEmpty() }
        } == true

        return if (hasCompleteName && profile.surname != null) {
            evaluateFullProfile(profile)
        } else {
            SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
        }
    }

    fun updateProfilesIfNeeded(profiles: List<Profile>): List<Profile> {
        val profilesToUpdate = profiles.filter { profile ->
            // 오행정보가 없거나 evaluatedNameJson이 없는 경우 재평가
            profile.ohaengInfo == null ||
                    (profile.isEvaluated() && profile.evaluatedNameJson == null)
        }
        Log.d(TAG, "재평가 필요한 프로필: ${profilesToUpdate.size}개")

        return profiles.map { profile ->
            if (profile.ohaengInfo == null ||
                (profile.isEvaluated() && profile.evaluatedNameJson == null)) {
                Log.d(TAG, "프로필 재평가 시작: ${profile.profileName}")
                val evaluated = evaluate(profile)  // 전체 평가 수행
                Log.d(TAG, "프로필 재평가 완료: ${profile.profileName}")
                evaluated
            } else {
                profile
            }
        }
    }

    private fun evaluateFullProfile(profile: Profile): Profile {
        val givenName = profile.givenName ?: return profile
        val surname = profile.surname ?: return profile

        val nameInput = buildNameInput(surname, givenName)
        Log.d(TAG, "=== evaluateFullProfile 시작 ===")
        Log.d(TAG, "이름 입력: $nameInput")

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

            val generatedName = evaluatedNames.firstOrNull()
            if (generatedName != null) {
                Log.d(TAG, "GeneratedName 받음:")
                Log.d(TAG, "  - combinedHanja: ${generatedName.combinedHanja}")
                Log.d(TAG, "  - combinedPronounciation: ${generatedName.combinedPronounciation}")

                // JSON 변환
                val jsonTest = gson.toJson(generatedName)
                Log.d(TAG, "JSON 변환 성공: ${jsonTest.length} bytes")

                // 기존 프로필을 복사하면서 업데이트
                val updatedProfile = profile.copy(
                    givenName = updateGivenNameInfoFromGeneratedName(givenName, generatedName),
                    updatedAt = System.currentTimeMillis(),
                    evaluatedNameJson = jsonTest,  // 여기서 직접 설정
                    nameBomScore = ProfileScoreCalculator.calculateNamebomScore(generatedName)
                )

                // 사주/오행 정보 업데이트
                generatedName.analysisInfo?.let { analysisInfo ->
                    updatedProfile.sajuInfo = SajuInfo.fromAnalysisInfo(analysisInfo)
                    updatedProfile.ohaengInfo = OhaengInfo(
                        wood = analysisInfo.sajuInfo.sajuOhaengCount["木"] ?: 0,
                        fire = analysisInfo.sajuInfo.sajuOhaengCount["火"] ?: 0,
                        earth = analysisInfo.sajuInfo.sajuOhaengCount["土"] ?: 0,
                        metal = analysisInfo.sajuInfo.sajuOhaengCount["金"] ?: 0,
                        water = analysisInfo.sajuInfo.sajuOhaengCount["水"] ?: 0
                    )
                }

                Log.d(TAG, "=== 평가 결과 ===")
                Log.d(TAG, "  - nameBomScore: ${updatedProfile.nameBomScore}")
                Log.d(TAG, "  - evaluatedNameJson: ${updatedProfile.evaluatedNameJson?.length} bytes")
                Log.d(TAG, "  - evaluatedNameJson is null: ${updatedProfile.evaluatedNameJson == null}")
                Log.d(TAG, "=== evaluateFullProfile 끝 ===")

                return updatedProfile
            } else {
                Log.w(TAG, "GeneratedName이 비어있음")
                return SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
            }
        } catch (e: Exception) {
            Log.e(TAG, "전체 평가 실패", e)
            e.printStackTrace()
            return SajuEvaluator.evaluateProfileSaju(profile, namingEngine)
        }
    }

    private fun updateGivenNameInfoFromGeneratedName(
        givenNameInfo: GivenNameInfo,
        generatedName: GeneratedName
    ): GivenNameInfo {
        // hanjaDetails에서 CharInfo 정보 추출 (성씨 제외하고 이름 부분만)
        val updatedCharInfos = givenNameInfo.charInfos.mapIndexed { index, existingCharInfo ->
            // hanjaDetails에서 해당 위치의 정보 가져오기 (성씨 다음부터)
            val hanjaDetail = generatedName.hanjaDetails.getOrNull(index + 1)

            CharInfo(
                korean = existingCharInfo.korean,
                hanja = existingCharInfo.hanja,
                meaning = hanjaDetail?.inmyongMeaning, // 인명용 뜻
                strokes = hanjaDetail?.okpyeonHoeksu ?: generatedName.nameHanjaHoeksu.getOrNull(index) ?: 0, // 옥편획수
                ohaeng = hanjaDetail?.jawonOhaeng, // 자원오행
                eumyang = hanjaDetail?.baleumEumyang?.toIntOrNull() ?: 0 // 발음음양 (String -> Int 변환)
            )
        }

        return givenNameInfo.copy(charInfos = updatedCharInfos)
    }

    private fun buildNameInput(surname: SurnameInfo, givenName: GivenNameInfo): String {
        val surnameInput = "[${surname.korean}/${surname.hanja}]"
        val givenNameInput = givenName.charInfos.joinToString("") { charInfo ->
            "[${charInfo.korean}/${charInfo.hanja}]"
        }
        return surnameInput + givenNameInput
    }
}