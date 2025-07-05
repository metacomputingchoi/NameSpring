// model/repository/NameData.kt
package com.ssc.namespring.model.repository

import android.content.Context

class NameData {
    companion object {
        private val repository: NameDataRepository = NameDataRepositoryImpl()

        @JvmStatic
        fun init(context: Context) = repository.init(context)

        @JvmStatic
        fun searchHanja(query: String): List<HanjaSearchResult> = repository.searchHanja(query)

        @JvmStatic
        fun getCharInfo(tripleKey: String): CharTripleInfo? = repository.getCharInfo(tripleKey)

        @JvmStatic
        fun getCharInfo(korean: String, hanja: String): CharTripleInfo? =
            repository.getCharInfo(korean, hanja)

        @JvmStatic
        fun validateData(): DataLoader.ValidationResult {
            val result = repository.validateData()
            return DataLoader.ValidationResult(
                isValid = result.isValid,
                warnings = result.warnings,
                criticalErrors = result.criticalErrors
            )
        }

        @JvmStatic
        fun isReady(): Boolean = repository.isReady()

        @JvmStatic
        fun getStats(): MappingStats? = repository.getStats()
    }

    data class OptimizedMapping(
        val version: String,
        val chosungToHanjaInfo: Map<String, List<HanjaInfo>>,
        val koreanToHanjaInfo: Map<String, List<HanjaInfo>>,
        val hanjaToHanjaInfo: Map<String, List<HanjaInfo>>,
        val meaningSearchIndex: Map<String, List<HanjaInfo>>,
        val stats: MappingStats
    )

    data class MappingStats(
        val totalTriples: Int,
        val totalProcessed: Int,
        val totalSkipped: Int,
        val totalChosung: Int,
        val totalKorean: Int,
        val totalHanja: Int,
        val totalMeaningWords: Int
    )

    data class HanjaInfo(
        val korean: String,
        val hanja: String,
        val meaning: String?,
        val ohaeng: String,
        val strokes: Int,
        val tripleKey: String,
        val nameMeaning: String?,
        val soundEumyang: Int,
        val strokeEumyang: Int,
        val soundOhaeng: String,
        val sourceOhaeng: String,
        val englishName: String,
        val cautionRed: String?,
        val cautionBlue: String?
    )

    data class CharTripleInfo(
        val koreanInfo: CharInfo,
        val hanjaInfo: CharInfo,
        val integratedInfo: IntegratedInfo
    )

    data class CharInfo(
        val character: String,
        val meaning: String?,
        val sound: String,
        val eumyang: Int,
        val ohaeng: String,
        val strokes: Int,
        val originalStrokes: Int
    )

    data class IntegratedInfo(
        val hanja: String,
        val nameMeaning: String?,
        val nameSound: String,
        val soundEumyang: Int,
        val strokeEumyang: Int,
        val soundOhaeng: String,
        val sourceOhaeng: String,
        val originalStrokes: Int,
        val dictionaryStrokes: Int,
        val englishName: String,
        val cautionRed: String?,
        val cautionBlue: String?
    )
}