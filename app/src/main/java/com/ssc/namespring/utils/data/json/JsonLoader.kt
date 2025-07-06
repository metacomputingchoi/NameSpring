// utils/data/json/JsonLoader.kt
package com.ssc.namespring.utils.data.json

import android.content.Context
import com.ssc.namespring.model.data.source.StrokeMeaningDetail
import com.ssc.namespring.model.data.source.HanjaInfoMeaning
import com.ssc.namespring.model.data.source.SimpleStrokeMeaning
import com.ssc.namespring.utils.analysis.HanjaAnalyzer
import com.ssc.namespring.utils.analysis.ReportHelper
import com.ssc.namespring.utils.analysis.StrokeAnalyzer
import com.ssc.namespring.utils.logger.AndroidLogger

object JsonLoader {
    private val logger = AndroidLogger("JsonLoader")
    private var isInitialized = false

    private lateinit var repository: JsonDataRepository
    private lateinit var strokeAnalyzer: StrokeAnalyzer
    private lateinit var hanjaAnalyzer: HanjaAnalyzer
    private lateinit var reportHelper: ReportHelper

    // 초기화 체크 헬퍼 메서드
    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("JsonLoader가 초기화되지 않았습니다. initialize()를 먼저 호출하세요.")
        }
    }

    // 기존 인터페이스 유지를 위한 프로퍼티들
    val scoreEvaluations get() = repository.scoreEvaluations
    val strokeMeanings get() = repository.strokeMeanings
    val elementCharacteristics get() = repository.elementCharacteristics
    val sajuAnalyzerStrings get() = repository.sajuAnalyzerStrings
    val elementAnalyzerStrings get() = repository.elementAnalyzerStrings
    val yinyangAnalyzerStrings get() = repository.yinyangAnalyzerStrings
    val hanjaMeanings get() = repository.hanjaMeanings
    val characterMeaningStrings get() = repository.characterMeaningStrings
    val reportTemplates get() = repository.reportTemplates
    val basicReportSections get() = repository.basicReportSections
    val formatSettings get() = repository.formatSettings
    val personalityEvaluatorStrings get() = repository.personalityEvaluatorStrings
    val careerEvaluatorStrings get() = repository.careerEvaluatorStrings
    val fortuneEvaluatorStrings get() = repository.fortuneEvaluatorStrings
    val lifePeriods get() = repository.lifePeriods
    val personalityTraits get() = repository.personalityTraits
    val businessLuckStrokes get() = repository.businessLuckStrokes
    val careerFields get() = repository.careerFields

    fun initialize(context: Context) {
        if (isInitialized) {
            logger.d("JsonLoader already initialized")
            return
        }

        repository = JsonDataRepository()
        val loader = JsonFileLoader(context)
        val initializer = JsonDataInitializer(loader, repository)

        initializer.initialize()

        strokeAnalyzer = StrokeAnalyzer(repository)
        hanjaAnalyzer = HanjaAnalyzer(repository)
        reportHelper = ReportHelper(repository)

        isInitialized = true
        logger.d("JsonLoader initialized successfully")
    }

    fun getStrokeMeaning(stroke: Int): SimpleStrokeMeaning {
        checkInitialized()
        return strokeAnalyzer.getStrokeMeaning(stroke)
    }

    fun getElementCharacteristic(element: String): String {
        checkInitialized()
        return hanjaAnalyzer.getElementCharacteristic(element)
    }

    fun getGrade(score: Int): String {
        checkInitialized()
        return strokeAnalyzer.getGrade(score)
    }

    fun getHanjaMeaning(hanja: String): HanjaInfoMeaning? {
        checkInitialized()
        return hanjaAnalyzer.getHanjaInfo(hanja)
    }

    fun isBusinessLuckStroke(stroke: Int): Boolean {
        checkInitialized()
        return strokeAnalyzer.isBusinessLuckStroke(stroke)
    }

    fun isLeadershipStroke(stroke: Int): Boolean {
        checkInitialized()
        return strokeAnalyzer.isLeadershipStroke(stroke)
    }

    fun hasPositiveMeaning(meaning: String): Boolean {
        checkInitialized()
        return hanjaAnalyzer.hasPositiveMeaning(meaning)
    }

    fun isMeaningHarmony(meaning1: String, meaning2: String): Boolean {
        checkInitialized()
        return hanjaAnalyzer.isMeaningHarmony(meaning1, meaning2)
    }

    fun getReportSectionTitle(section: String): String {
        checkInitialized()
        return reportHelper.getReportSectionTitle(section)
    }

    fun getReportSubsectionLabel(subsection: String): String {
        checkInitialized()
        return reportHelper.getReportSubsectionLabel(subsection)
    }
}