// model/domain/name/entity/FilteringStep.kt
package com.ssc.namespring.model.domain.name.entity

data class FilteringStep(
    val step: String,
    val passed: Boolean,
    val reason: String? = null,
    val details: Map<String, Any>? = null
)