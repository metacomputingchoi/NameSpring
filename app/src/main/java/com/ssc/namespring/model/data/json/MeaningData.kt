// model/data/json/MeaningData.kt
package com.ssc.namespring.model.data.json

import com.google.gson.annotations.SerializedName

data class HanjaMeanings(
    @SerializedName("hanja_origins")
    val hanjaOrigins: Map<String, String>,

    @SerializedName("hanja_components")
    val hanjaComponents: Map<String, List<String>>,

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
    val meaningHarmonyPatterns: Map<String, Boolean>
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

data class HanjaInfo(
    val origin: String?,
    val components: List<String>?,
    val relatedCharacters: List<String>?
)
