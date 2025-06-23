// model/domain/name/entity/NameCombination.kt
package com.ssc.namespring.model.domain.name.entity

data class NameCombination(
    val surHanjaStroke: Int,
    val stroke1: Int,
    val stroke2: Int,
    val fourTypes: List<Int>,
    val fourTypesLuck: List<Int>,
    val initialScore: Int,
    val namePn: List<Int>,
    val namePnSum: Int,
    val nameElements: List<Int>,
    val nameElementChecks: List<ElementCheck>,
    val scoreCoexistName: Int,
    val typeElements: List<Int>,
    val typeElementChecks: List<ElementCheck>,
    val scoreCoexistType: Int,
    val finalScore: Int,
    val scoreZeroReason: String? = null
)

data class ElementCheck(
    val position: String,
    val elements: String,
    val diff: Int,
    val result: String
)