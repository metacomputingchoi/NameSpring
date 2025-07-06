// model/data/source/ElementCharacteristics.kt
package com.ssc.namespring.model.data.source

import com.google.gson.annotations.SerializedName

data class ElementCharacteristics(
    @SerializedName("element_characteristics")
    val elementCharacteristics: Map<String, String>,

    @SerializedName("element_colors")
    val elementColors: Map<String, List<String>>,

    @SerializedName("element_generative_relations")
    val elementGenerativeRelations: Map<String, String>,

    @SerializedName("element_controlling_relations")
    val elementControllingRelations: Map<String, String>,

    @SerializedName("element_career_fields")
    val elementCareerFields: Map<String, List<String>>,

    @SerializedName("element_development_areas")
    val elementDevelopmentAreas: Map<String, String>,

    @SerializedName("element_lacking_recommendations")
    val elementLackingRecommendations: Map<String, List<String>>,

    @SerializedName("element_directions")
    val elementDirections: Map<String, String>? = null,

    @SerializedName("element_seasons")
    val elementSeasons: Map<String, String>? = null,

    @SerializedName("element_tastes")
    val elementTastes: Map<String, String>? = null,

    @SerializedName("element_numbers")
    val elementNumbers: Map<String, List<String>>? = null
)