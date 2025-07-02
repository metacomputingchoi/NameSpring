// model/data/json/MeaningData.kt
package com.ssc.namespring.model.data.json

import com.google.gson.annotations.SerializedName

data class HanjaMeanings(
    @SerializedName("hanja_origins")
    val hanjaOrigins: Map<String, HanjaOriginDetail>,

    @SerializedName("hanja_components")
    val hanjaComponents: Map<String, HanjaComponent>,

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
    val modernNamingConsiderations: ModernNamingConsiderations
)

data class HanjaOriginDetail(
    val meaning: String,
    val origin: String,
    val usage: String
)

data class HanjaComponent(
    val parts: List<String>,
    val strokes: Int,
    val radical: String
)

data class ModernNamingConsiderations(
    val globalization: String,
    @SerializedName("gender_neutral")
    val genderNeutral: String,
    @SerializedName("meaning_focus")
    val meaningFocus: String,
    val pronunciation: String,
    val uniqueness: String
)

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