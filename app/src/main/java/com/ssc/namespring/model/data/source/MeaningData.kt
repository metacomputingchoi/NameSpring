// model/data/source/MeaningData.kt
package com.ssc.namespring.model.data.source

import com.google.gson.annotations.SerializedName

// 한자 기원 정보
data class HanjaOriginInfo(
    @SerializedName("meaning")
    val meaning: String,

    @SerializedName("origin")
    val origin: String,

    @SerializedName("usage")
    val usage: String
)

// 한자 구성 요소 정보
data class HanjaComponentInfo(
    @SerializedName("parts")
    val parts: List<String>,

    @SerializedName("strokes")
    val strokes: Int,

    @SerializedName("radical")
    val radical: String
)

// 한자 의미 전체 데이터
data class HanjaMeanings(
    @SerializedName("hanja_origins")
    val hanjaOrigins: Map<String, HanjaOriginInfo>,

    @SerializedName("hanja_components")
    val hanjaComponents: Map<String, HanjaComponentInfo>,

    @SerializedName("hanja_related_characters")
    val hanjaRelatedCharacters: Map<String, List<String>>,

    @SerializedName("combined_meanings")
    val combinedMeanings: Map<String, String>,

    @SerializedName("symbolic_elements")
    val symbolicElements: Map<String, String>,

    @SerializedName("cultural_significance")
    val culturalSignificance: String,

    @SerializedName("positive_meanings")
    val positiveMeanings: List<String>,

    @SerializedName("meaning_harmony_patterns")
    val meaningHarmonyPatterns: Map<String, String>,

    @SerializedName("modern_naming_considerations")
    val modernNamingConsiderations: Map<String, String>? = null
)

// 문자 의미 문자열 데이터
data class CharacterMeaningStrings(
    @SerializedName("default_meaning")
    val defaultMeaning: String,

    @SerializedName("origin_not_found")
    val originNotFound: String,

    @SerializedName("combined_meaning_format")
    val combinedMeaningFormat: String,

    @SerializedName("default_symbolic")
    val defaultSymbolic: String,

    @SerializedName("basic_score")
    val basicScore: Int,

    @SerializedName("positive_meaning_bonus")
    val positiveMeaningBonus: Int,

    @SerializedName("harmony_bonus")
    val harmonyBonus: Int,

    @SerializedName("score_min")
    val scoreMin: Int,

    @SerializedName("score_max")
    val scoreMax: Int,

    @SerializedName("harmony_pattern_size")
    val harmonyPatternSize: Int,

    @SerializedName("pattern_delimiter")
    val patternDelimiter: String
)

// 한자 정보 의미 (반환용)
data class HanjaInfoMeaning(
    val meaning: String?,
    val origin: String?,
    val usage: String?,
    val components: HanjaComponentInfo?,
    val relatedCharacters: List<String>?
)

// 간단한 획수 의미 (StrokeAnalyzer에서 사용)
data class SimpleStrokeMeaning(
    @SerializedName("strokes")
    val strokes: Int,

    @SerializedName("luck")
    val luck: String,

    @SerializedName("element")
    val element: String,

    @SerializedName("character")
    val character: String,

    @SerializedName("meaning")
    val meaning: String
)