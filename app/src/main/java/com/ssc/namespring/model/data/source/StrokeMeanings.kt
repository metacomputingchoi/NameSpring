// model/data/source/StrokeMeanings.kt
package com.ssc.namespring.model.data.source

import com.google.gson.annotations.SerializedName

data class StrokeMeanings(
    @SerializedName("stroke_meanings")
    val strokeMeanings: Map<String, StrokeMeaningDetail>
)

data class StrokeMeaningDetail(
    val number: Int,
    val title: String,
    val summary: String,

    @SerializedName("detailed_explanation")
    val detailedExplanation: String,

    @SerializedName("positive_aspects")
    val positiveAspects: String,

    @SerializedName("caution_points")
    val cautionPoints: String,

    @SerializedName("personality_traits")
    val personalityTraits: List<String>,

    @SerializedName("suitable_career")
    val suitableCareer: List<String>,

    @SerializedName("life_period_influence")
    val lifePeriodInfluence: String,

    @SerializedName("special_characteristics")
    val specialCharacteristics: String?,

    @SerializedName("challenge_period")
    val challengePeriod: String?,

    @SerializedName("opportunity_area")
    val opportunityArea: String?,

    @SerializedName("lucky_level")
    val luckyLevel: String
)