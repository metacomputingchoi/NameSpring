// model/domain/service/workmanager/workers/ReportGenerationWorker.kt
package com.ssc.namespring.model.domain.service.workmanager.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.service.workmanager.BaseWorker
import com.ssc.namespring.model.domain.usecase.ProfileManagerProvider
import com.ssc.namespring.utils.analysis.ReportHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class ReportGenerationWorker(
    context: Context,
    params: WorkerParameters
) : BaseWorker(context, params) {

    override suspend fun performWork(): WorkResult {
        try {
            val inputData = getInputDataMap()

            // Extract report parameters
            val reportType = inputData["reportType"] as? String ?: "comprehensive"
            val format = inputData["format"] as? String ?: "JSON"
            val includeDetails = inputData["includeDetails"] as? Boolean ?: true

            updateProgress(10)

            // Get profile for report generation
            val profileManager = ProfileManagerProvider.getInstance()  // 수정됨
            val profile = profileManager.getProfile(profileId) ?: return WorkResult(
                success = false,
                error = "Profile not found for report generation"
            )

            updateProgress(20)

            // Generate report sections
            val reportSections = mutableMapOf<String, Any>()

            // Basic Information Section
            reportSections["basicInfo"] = generateBasicInfoSection(profile)
            updateProgress(30)

            // Saju Analysis Section
            if (profile.sajuInfo != null) {
                reportSections["sajuAnalysis"] = generateSajuSection(profile)
                updateProgress(40)
            }

            // Name Evaluation Section
            if (profile.nameBomScore > 0) {
                reportSections["nameEvaluation"] = generateEvaluationSection(profile)
                updateProgress(50)
            }

            // Ohaeng Analysis Section
            if (profile.ohaengInfo != null) {
                reportSections["ohaengAnalysis"] = generateOhaengSection(profile)
                updateProgress(60)
            }

            // Detailed Analysis (if requested)
            if (includeDetails) {
                reportSections["detailedAnalysis"] = generateDetailedAnalysis(profile)
                updateProgress(80)
            }

            // Simulate report compilation
            delay(1000)

            // Generate final report
            val report = mapOf(
                "reportId" to "RPT-${profile.id}-${System.currentTimeMillis()}",
                "profileId" to profile.id,
                "profileName" to profile.profileName,
                "reportType" to reportType,
                "format" to format,
                "generatedAt" to System.currentTimeMillis(),
                "generatedDate" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                "sections" to reportSections,
                "metadata" to mapOf(
                    "version" to "1.0",
                    "generator" to "NameSpring Report Engine",
                    "includesDetails" to includeDetails
                )
            )

            updateProgress(100)

            return WorkResult(
                success = true,
                data = mapOf(
                    "report" to report,
                    "reportId" to (report["reportId"] as? String ?: ""),
                    "format" to format,
                    "sectionCount" to reportSections.size,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            return WorkResult(
                success = false,
                error = "Report generation failed: ${e.message}"
            )
        }
    }

    private fun generateBasicInfoSection(profile: com.ssc.namespring.model.domain.entity.Profile): Map<String, Any> {
        return mapOf(
            "profileName" to profile.profileName,
            "fullName" to "${profile.surname?.korean ?: ""}${profile.givenName?.korean ?: ""}",
            "fullNameHanja" to "${profile.surname?.hanja ?: ""}${profile.givenName?.hanja ?: ""}",
            "birthDate" to SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(profile.birthDate.time),
            "birthTime" to SimpleDateFormat("HH시 mm분", Locale.KOREAN).format(profile.birthDate.time),
            "isYajaTime" to profile.isYajaTime,
            "createdDate" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(profile.createdAt)),
            "lastUpdated" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(profile.updatedAt))
        )
    }

    private fun generateSajuSection(profile: com.ssc.namespring.model.domain.entity.Profile): Map<String, Any> {
        val saju = profile.sajuInfo ?: return emptyMap()

        return mapOf(
            "fourPillars" to saju.fourPillars,
            "yearPillar" to mapOf(
                "heavenlyStem" to saju.yearPillar.heavenlyStem,
                "earthlyBranch" to saju.yearPillar.earthlyBranch,
                "ohaeng" to "${saju.yearPillar.stemOhaeng}${saju.yearPillar.branchOhaeng}"
            ),
            "monthPillar" to mapOf(
                "heavenlyStem" to saju.monthPillar.heavenlyStem,
                "earthlyBranch" to saju.monthPillar.earthlyBranch,
                "ohaeng" to "${saju.monthPillar.stemOhaeng}${saju.monthPillar.branchOhaeng}"
            ),
            "dayPillar" to mapOf(
                "heavenlyStem" to saju.dayPillar.heavenlyStem,
                "earthlyBranch" to saju.dayPillar.earthlyBranch,
                "ohaeng" to "${saju.dayPillar.stemOhaeng}${saju.dayPillar.branchOhaeng}"
            ),
            "hourPillar" to mapOf(
                "heavenlyStem" to saju.hourPillar.heavenlyStem,
                "earthlyBranch" to saju.hourPillar.earthlyBranch,
                "ohaeng" to "${saju.hourPillar.stemOhaeng}${saju.hourPillar.branchOhaeng}"
            ),
            "elementCounts" to saju.sajuOhaengCount,
            "missingElements" to saju.missingElements,
            "dominantElements" to saju.dominantElements,
            "elementBalance" to saju.elementBalance
        )
    }

    private fun generateEvaluationSection(profile: com.ssc.namespring.model.domain.entity.Profile): Map<String, Any> {
        return mapOf(
            "totalScore" to profile.nameBomScore,
            "scoreTheme" to profile.getScoreThemeColor().name,
            "scoreThemeDescription" to when (profile.getScoreThemeColor()) {
                Profile.ScoreTheme.SUNNY_SPRING -> "화창한 봄"
                Profile.ScoreTheme.WARM_SPRING -> "따뜻한 봄"
                Profile.ScoreTheme.CLOUDY_SPRING -> "구름 낀 봄"
                Profile.ScoreTheme.RAINY_SPRING -> "비 오는 봄"
                Profile.ScoreTheme.COLD_SPRING -> "추운 봄"
                Profile.ScoreTheme.NOT_EVALUATED -> "미평가"
            },
            "scoreCategory" to when (profile.nameBomScore) {
                in 90..100 -> "매우 우수"
                in 80..89 -> "우수"
                in 70..79 -> "양호"
                in 60..69 -> "보통"
                else -> "미흡"
            },
            "evaluationDate" to if (profile.nameBomScore > 0) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(profile.updatedAt))
            } else "미평가",
            "givenName" to mapOf(
                "fullName" to profile.getFullName(),  // Profile의 메서드 활용
                "fullNameWithHanja" to profile.getFullNameWithHanja(),  // Profile의 메서드 활용
                "characterCount" to profile.nameCharCount
            )
        )
    }

    private fun generateOhaengSection(profile: com.ssc.namespring.model.domain.entity.Profile): Map<String, Any> {
        val ohaeng = profile.ohaengInfo ?: return emptyMap()

        val total = ohaeng.wood + ohaeng.fire + ohaeng.earth + ohaeng.metal + ohaeng.water

        return mapOf(
            "counts" to mapOf(
                "wood" to ohaeng.wood,
                "fire" to ohaeng.fire,
                "earth" to ohaeng.earth,
                "metal" to ohaeng.metal,
                "water" to ohaeng.water
            ),
            "percentages" to if (total > 0) {
                mapOf(
                    "wood" to String.format("%.1f%%", (ohaeng.wood * 100.0 / total)),
                    "fire" to String.format("%.1f%%", (ohaeng.fire * 100.0 / total)),
                    "earth" to String.format("%.1f%%", (ohaeng.earth * 100.0 / total)),
                    "metal" to String.format("%.1f%%", (ohaeng.metal * 100.0 / total)),
                    "water" to String.format("%.1f%%", (ohaeng.water * 100.0 / total))
                )
            } else emptyMap(),
            "total" to total,
            "balance" to calculateOhaengBalance(ohaeng)
        )
    }

    private fun generateDetailedAnalysis(profile: com.ssc.namespring.model.domain.entity.Profile): Map<String, Any> {
        val analysis = mutableMapOf<String, Any>()

        // Character meanings
        profile.givenName?.charInfos?.let { charInfos ->
            analysis["characterMeanings"] = charInfos.mapIndexed { index, charInfo ->
                mapOf(
                    "position" to (index + 1),
                    "korean" to charInfo.korean,
                    "hanja" to charInfo.hanja,
                    "meaning" to (charInfo.meaning ?: "의미 정보 없음"),
                    "strokes" to charInfo.strokes,
                    "ohaeng" to (charInfo.ohaeng ?: ""),
                    "eumyang" to if (charInfo.eumyang == 1) "양(陽)" else "음(陰)"
                )
            }
        }

        // Recommendations
        analysis["recommendations"] = generateRecommendations(profile)

        // Compatibility analysis
        analysis["compatibility"] = mapOf(
            "withSurname" to calculateNameCompatibility(profile),
            "withBirthDate" to calculateBirthDateCompatibility(profile)
        )

        return analysis
    }

    private fun calculateOhaengBalance(ohaeng: com.ssc.namespring.model.domain.entity.OhaengInfo): String {
        val counts = listOf(ohaeng.wood, ohaeng.fire, ohaeng.earth, ohaeng.metal, ohaeng.water)
        val max = counts.maxOrNull() ?: 0
        val min = counts.minOrNull() ?: 0

        return when {
            max - min <= 2 -> "매우 균형잡힌 오행"
            max - min <= 4 -> "균형잡힌 오행"
            max - min <= 6 -> "약간 불균형한 오행"
            else -> "불균형한 오행"
        }
    }

    private fun generateRecommendations(profile: com.ssc.namespring.model.domain.entity.Profile): List<String> {
        val recommendations = mutableListOf<String>()

        // Based on score
        when (profile.nameBomScore) {
            in 0..59 -> recommendations.add("이름 재검토를 권장합니다")
            in 60..79 -> recommendations.add("보완이 필요한 부분을 개선하시면 좋습니다")
            in 80..100 -> recommendations.add("좋은 이름입니다. 현재 상태를 유지하세요")
        }

        // Based on ohaeng
        profile.sajuInfo?.missingElements?.forEach { element ->
            recommendations.add("부족한 $element 기운을 보완하는 것이 좋습니다")
        }

        return recommendations
    }

    private fun calculateNameCompatibility(profile: com.ssc.namespring.model.domain.entity.Profile): String {
        // Simplified compatibility calculation
        return when {
            profile.surname != null && profile.givenName != null -> "양호"
            else -> "평가 불가"
        }
    }

    private fun calculateBirthDateCompatibility(profile: com.ssc.namespring.model.domain.entity.Profile): String {
        // Simplified compatibility calculation
        return when {
            profile.sajuInfo != null -> "분석 완료"
            else -> "사주 정보 필요"
        }
    }
}