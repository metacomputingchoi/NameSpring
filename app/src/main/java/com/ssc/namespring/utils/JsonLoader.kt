// utils/JsonLoader.kt
package com.ssc.namespring.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssc.namespring.model.data.json.*
import com.ssc.namespring.utils.logger.AndroidLogger
import java.io.InputStreamReader

/**
 * JSON 파일 로딩을 담당하는 싱글톤 클래스
 * 앱 시작 시 한 번만 로드하고 메모리에 캐싱
 */
object JsonLoader {
    private val logger = AndroidLogger("JsonLoader")
    private val gson = Gson()

    // 로드된 데이터 캐싱
    private var isInitialized = false
    lateinit var scoreEvaluations: ScoreEvaluations
    lateinit var strokeMeanings: StrokeMeanings
    lateinit var elementCharacteristics: ElementCharacteristics
    lateinit var sajuAnalyzerStrings: SajuAnalyzerStrings
    lateinit var elementAnalyzerStrings: ElementAnalyzerStrings
    lateinit var yinyangAnalyzerStrings: YinyangAnalyzerStrings
    lateinit var hanjaMeanings: HanjaMeanings
    lateinit var characterMeaningStrings: CharacterMeaningStrings
    lateinit var reportTemplates: ReportTemplates
    lateinit var basicReportSections: BasicReportSections
    lateinit var formatSettings: FormatSettings

    // Optional JSON files - 없어도 앱이 동작하도록
    var personalityEvaluatorStrings: PersonalityEvaluatorStrings? = null
    var careerEvaluatorStrings: CareerEvaluatorStrings? = null
    var fortuneEvaluatorStrings: FortuneEvaluatorStrings? = null
    var lifePeriods: LifePeriods? = null
    var personalityTraits: PersonalityTraits? = null
    var businessLuckStrokes: BusinessLuckStrokes? = null
    var careerFields: CareerFields? = null

    /**
     * 모든 JSON 파일을 로드하고 초기화
     * MainActivity의 onCreate에서 호출
     */
    fun initialize(context: Context) {
        if (isInitialized) {
            logger.d("JsonLoader already initialized")
            return
        }

        try {
            logger.d("Loading JSON files...")

            // 필수 파일들 - 실제 경로에 맞게 수정
            scoreEvaluations = loadJsonRequired(context, "evaluations/score_evaluations.json")
            strokeMeanings = loadJsonRequired(context, "evaluations/stroke_meanings.json")

            // analysis 폴더 (analyzer가 아님)
            sajuAnalyzerStrings = loadJsonRequired(context, "analysis/saju_analyzer_strings.json")
            elementAnalyzerStrings = loadJsonRequired(context, "analysis/element_analyzer_strings.json")
            yinyangAnalyzerStrings = loadJsonRequired(context, "analysis/yinyang_analyzer_strings.json")
            characterMeaningStrings = loadJsonRequired(context, "analysis/character_meaning_strings.json")

            // meanings 폴더
            elementCharacteristics = loadJsonRequired(context, "meanings/element_characteristics.json")
            hanjaMeanings = loadJsonRequired(context, "meanings/hanja_meanings.json")

            // report 폴더
            reportTemplates = loadJsonRequired(context, "report/report_templates.json")
            basicReportSections = loadJsonRequired(context, "report/basic_report_sections.json")
            formatSettings = loadJsonRequired(context, "report/format_settings.json")

            // 선택적 파일들 - 없어도 앱이 동작하도록
            personalityEvaluatorStrings = loadJsonOptional(context, "evaluations/personality_evaluator_strings.json")
            careerEvaluatorStrings = loadJsonOptional(context, "evaluations/career_evaluator_strings.json")
            fortuneEvaluatorStrings = loadJsonOptional(context, "evaluations/fortune_evaluator_strings.json")
            businessLuckStrokes = loadJsonOptional(context, "evaluations/business_luck_strokes.json")

            lifePeriods = loadJsonOptional(context, "report/life_periods.json")
            personalityTraits = loadJsonOptional(context, "report/personality_traits.json")
            careerFields = loadJsonOptional(context, "report/career_fields.json")

            isInitialized = true
            logger.d("All JSON files loaded successfully")

        } catch (e: Exception) {
            logger.e("Failed to load JSON files", e)
            throw RuntimeException("JSON 파일 로딩 실패: ${e.message}", e)
        }
    }

    /**
     * 필수 JSON 로딩 함수 - 실패 시 예외 발생
     */
    private inline fun <reified T> loadJsonRequired(context: Context, fileName: String): T {
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    gson.fromJson(reader, T::class.java)
                }
            }
        } catch (e: Exception) {
            logger.e("Failed to load required file: $fileName", e)
            throw e
        }
    }

    /**
     * 선택적 JSON 로딩 함수 - 실패 시 null 반환
     */
    private inline fun <reified T> loadJsonOptional(context: Context, fileName: String): T? {
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    gson.fromJson(reader, T::class.java)
                }
            }
        } catch (e: Exception) {
            logger.d("Optional file not found: $fileName")
            null
        }
    }

    /**
     * 특정 획수의 의미 조회 (81수를 넘어가면 % 81로 정규화)
     */
    fun getStrokeMeaning(stroke: Int): StrokeMeaningDetail {
        val normalizedStroke = if (stroke > 81) stroke % 81 else stroke
        val strokeStr = normalizedStroke.toString()

        return strokeMeanings.strokeMeanings[strokeStr]
            ?: strokeMeanings.strokeMeanings["1"]!! // 기본값 (1수)
    }

    /**
     * 오행의 특성 조회
     */
    fun getElementCharacteristic(element: String): String {
        return elementCharacteristics.elementCharacteristics[element]
            ?: "알 수 없는 오행"
    }

    /**
     * 점수에 따른 등급 조회
     */
    fun getGrade(score: Int): String {
        return when {
            score >= scoreEvaluations.scoreThresholds.gradeA -> "A"
            score >= scoreEvaluations.scoreThresholds.gradeB -> "B"
            score >= scoreEvaluations.scoreThresholds.gradeC -> "C"
            else -> "D"
        }
    }

    /**
     * 한자의 의미 정보 조회
     */
    fun getHanjaMeaning(hanja: String): HanjaInfo? {
        return HanjaInfo(
            origin = hanjaMeanings.hanjaOrigins[hanja],
            components = hanjaMeanings.hanjaComponents[hanja],
            relatedCharacters = hanjaMeanings.hanjaRelatedCharacters[hanja]
        )
    }

    /**
     * 사업운 획수 확인 (선택적 기능)
     */
    fun isBusinessLuckStroke(stroke: Int): Boolean {
        val normalizedStroke = if (stroke > 81) stroke % 81 else stroke
        return businessLuckStrokes?.businessLuckStrokes?.contains(normalizedStroke) ?: false
    }

    /**
     * 리더십 획수 확인 (선택적 기능)
     */
    fun isLeadershipStroke(stroke: Int): Boolean {
        val normalizedStroke = if (stroke > 81) stroke % 81 else stroke
        return businessLuckStrokes?.leadershipStrokes?.contains(normalizedStroke) ?: false
    }

    /**
     * 긍정적인 의미를 가진 한자인지 확인
     */
    fun hasPositiveMeaning(meaning: String): Boolean {
        return hanjaMeanings.positiveMeanings.any { positive ->
            meaning.contains(positive)
        }
    }

    /**
     * 두 한자의 의미가 조화로운지 확인
     */
    fun isMeaningHarmony(meaning1: String, meaning2: String): Boolean {
        val pattern = "${meaning1}_${meaning2}"
        return hanjaMeanings.meaningHarmonyPatterns[pattern] ?: false
    }

    /**
     * 보고서 섹션 제목 가져오기
     */
    fun getReportSectionTitle(section: String): String {
        return reportTemplates.sectionTitles[section] ?: section
    }

    /**
     * 보고서 서브섹션 라벨 가져오기
     */
    fun getReportSubsectionLabel(subsection: String): String {
        return reportTemplates.subsectionLabels[subsection] ?: subsection
    }
}