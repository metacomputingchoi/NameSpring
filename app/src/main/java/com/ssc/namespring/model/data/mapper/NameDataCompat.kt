// model/data/mapper/NameDataCompat.kt
package com.ssc.namespring.model.data.mapper

// 기존 코드와의 호환성을 위한 타입 정의
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
    val koreanInfo: CharInfoMapping,
    val hanjaInfo: CharInfoMapping,
    val integratedInfo: IntegratedInfo
)

data class CharInfoMapping(
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