// model/data/json/ElementCharacteristics.kt
package com.ssc.namespring.model.data.json

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
    val elementCareerFields: Map<String, String>,

    @SerializedName("element_development_areas")
    val elementDevelopmentAreas: Map<String, String>,

    @SerializedName("element_lacking_recommendations")
    val elementLackingRecommendations: Map<String, String>
)
