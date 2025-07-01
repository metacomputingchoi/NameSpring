// model/ProfileModel.kt
package com.ssc.namespring.model

import com.ssc.namespring.model.data.*
import com.ssc.namespring.model.repository.ProfileRepository
import com.ssc.namingengine.NamingEngine
import java.time.LocalDateTime
import java.util.UUID

/**
 * 프로필 관리 비즈니스 로직
 * NamingEngine을 통해 이미 계산된 사주 정보와 이름봄 점수를 활용
 */
class ProfileModel(
    private val repository: ProfileRepository,
    private val namingEngine: NamingEngine
) {

    suspend fun createProfile(
        profileName: String,
        surname: String,
        surnameHanja: String,
        givenName: String,
        givenNameHanja: String,
        birthDateTime: LocalDateTime,
        useYajasi: Boolean
    ): Result<Profile> {
        return try {
            // 이름 입력 형식 생성
            val nameInput = buildNameInput(surname, surnameHanja, givenName, givenNameHanja)

            // NamingEngine을 통해 평가 (withoutFilter = true로 평가만 수행)
            val evaluationResults = namingEngine.generateNames(
                userInput = nameInput,
                birthDateTime = birthDateTime,
                useYajasi = useYajasi,
                verbose = false,
                withoutFilter = true
            )

            val evaluatedName = evaluationResults.firstOrNull()
                ?: return Result.failure(Exception("이름 평가 실패"))

            // 평가 결과에서 사주 정보 추출
            val sajuInfo = evaluatedName.analysisInfo?.let { 
                SajuInfo.fromAnalysisInfo(it)
            }

            // 이름봄 점수 계산 (이미 계산된 점수 활용)
            val namebomScore = calculateNamebomScore(evaluatedName)

            val profile = Profile(
                id = UUID.randomUUID().toString(),
                profileName = profileName,
                surname = surname,
                surnameHanja = surnameHanja,
                givenName = givenName,
                givenNameHanja = givenNameHanja,
                birthDateTime = birthDateTime,
                useYajasi = useYajasi,
                sajuInfo = sajuInfo,
                namebomScore = namebomScore
            )

            repository.insert(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        id: String,
        profileName: String? = null,
        surname: String? = null,
        surnameHanja: String? = null,
        givenName: String? = null,
        givenNameHanja: String? = null,
        birthDateTime: LocalDateTime? = null,
        useYajasi: Boolean? = null
    ): Result<Profile> {
        return try {
            val existingProfile = repository.getById(id) 
                ?: return Result.failure(Exception("프로필을 찾을 수 없습니다"))

            val updatedProfile = existingProfile.copy(
                profileName = profileName ?: existingProfile.profileName,
                surname = surname ?: existingProfile.surname,
                surnameHanja = surnameHanja ?: existingProfile.surnameHanja,
                givenName = givenName ?: existingProfile.givenName,
                givenNameHanja = givenNameHanja ?: existingProfile.givenNameHanja,
                birthDateTime = birthDateTime ?: existingProfile.birthDateTime,
                useYajasi = useYajasi ?: existingProfile.useYajasi,
                updatedAt = LocalDateTime.now()
            )

            // 이름이나 생년월일이 변경된 경우 재평가
            val needsReEvaluation = 
                surname != null || surnameHanja != null || 
                givenName != null || givenNameHanja != null || 
                birthDateTime != null || useYajasi != null

            val finalProfile = if (needsReEvaluation) {
                val nameInput = buildNameInput(
                    updatedProfile.surname, updatedProfile.surnameHanja,
                    updatedProfile.givenName, updatedProfile.givenNameHanja
                )

                val evaluationResults = namingEngine.generateNames(
                    userInput = nameInput,
                    birthDateTime = updatedProfile.birthDateTime,
                    useYajasi = updatedProfile.useYajasi,
                    verbose = false,
                    withoutFilter = true
                )

                val evaluatedName = evaluationResults.firstOrNull()
                if (evaluatedName != null) {
                    val sajuInfo = evaluatedName.analysisInfo?.let { 
                        SajuInfo.fromAnalysisInfo(it)
                    }
                    val namebomScore = calculateNamebomScore(evaluatedName)

                    updatedProfile.copy(
                        sajuInfo = sajuInfo,
                        namebomScore = namebomScore
                    )
                } else {
                    updatedProfile
                }
            } else {
                updatedProfile
            }

            repository.update(finalProfile)
            Result.success(finalProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProfile(id: String): Result<Boolean> {
        return try {
            repository.delete(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(id: String): Result<Profile?> {
        return try {
            Result.success(repository.getById(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllProfiles(): Result<List<Profile>> {
        return try {
            Result.success(repository.getAll())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * NamingEngine 입력 형식으로 이름 문자열 생성
     */
    private fun buildNameInput(
        surname: String, 
        surnameHanja: String, 
        givenName: String, 
        givenNameHanja: String
    ): String {
        val surnameInput = surname.mapIndexed { index, char ->
            val hanja = surnameHanja.getOrNull(index) ?: "_"
            "[$char/$hanja]"
        }.joinToString("")

        val givenNameInput = givenName.mapIndexed { index, char ->
            val hanja = givenNameHanja.getOrNull(index) ?: "_"
            "[$char/$hanja]"
        }.joinToString("")

        return surnameInput + givenNameInput
    }

    /**
     * GeneratedName에서 이름봄 점수 계산
     * analysisInfo의 totalScore와 scoreBreakdown을 활용
     */
    private fun calculateNamebomScore(generatedName: com.ssc.namingengine.data.GeneratedName): Int {
        return generatedName.analysisInfo?.let { analysisInfo ->
            // scoreBreakdown의 최대 가능 점수를 기준으로 정규화
            val scoreBreakdown = analysisInfo.scoreBreakdown
            val categories = scoreBreakdown.keys

            // 각 카테고리별 가중치 설정
            val weights = mapOf(
                "사격점수" to 0.3,
                "음양균형" to 0.2,
                "오행조화" to 0.2,
                "획수길흉" to 0.2,
                "발음자연스러움" to 0.1
            )

            var weightedScore = 0.0
            var totalWeight = 0.0

            categories.forEach { category ->
                val score = scoreBreakdown[category] ?: 0
                val weight = weights[category] ?: 0.1
                val maxScore = when (category) {
                    "사격점수" -> 100
                    else -> 20
                }

                // 각 카테고리를 100점 만점으로 정규화
                val normalizedScore = (score.toDouble() / maxScore * 100)
                weightedScore += normalizedScore * weight
                totalWeight += weight
            }

            // 최종 점수 계산
            if (totalWeight > 0) {
                (weightedScore / totalWeight).toInt().coerceIn(0, 100)
            } else {
                // fallback: totalScore 기반 계산
                (analysisInfo.totalScore * 100 / 160).coerceIn(0, 100)
            }
        } ?: 0
    }

    /**
     * 프로필의 사주 정보 업데이트 (별도 요청 시)
     */
    suspend fun refreshSajuInfo(profileId: String): Result<Profile> {
        return try {
            val profile = repository.getById(profileId)
                ?: return Result.failure(Exception("프로필을 찾을 수 없습니다"))

            val nameInput = profile.getNameInputFormat()

            val evaluationResults = namingEngine.generateNames(
                userInput = nameInput,
                birthDateTime = profile.birthDateTime,
                useYajasi = profile.useYajasi,
                verbose = false,
                withoutFilter = true
            )

            val evaluatedName = evaluationResults.firstOrNull()
                ?: return Result.failure(Exception("사주 정보 갱신 실패"))

            val sajuInfo = evaluatedName.analysisInfo?.let { 
                SajuInfo.fromAnalysisInfo(it)
            }

            val updatedProfile = profile.copy(
                sajuInfo = sajuInfo,
                updatedAt = LocalDateTime.now()
            )

            repository.update(updatedProfile)
            Result.success(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}