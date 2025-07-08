// model/domain/service/evaluation/config/ScoreWeightConfig.kt
package com.ssc.namespring.model.domain.service.evaluation.config

data class ScoreWeightConfig(
    val weights: Map<String, Float> = mapOf(
        "사격수리" to 0.35f,
        "음양균형" to 0.15f,
        "오행조화" to 0.20f,
        "한자의미" to 0.15f,
        "발음자연스러움" to 0.10f,
        "사주보완" to 0.05f
    )
) {
    fun getWeight(category: String): Float = weights[category] ?: 0f

    fun validateWeights(): Boolean {
        val total = weights.values.sum()
        return kotlin.math.abs(total - 1.0f) < 0.001f
    }
}
