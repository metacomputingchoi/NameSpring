// model/domain/element/entity/ElementBalance.kt
package com.ssc.namespring.model.domain.element.entity

data class ElementBalance(
    val wood: Int, 
    val fire: Int, 
    val earth: Int,
    val metal: Int, 
    val water: Int
) {
    fun toMap() = mapOf(
        "木" to wood, 
        "火" to fire, 
        "土" to earth,
        "金" to metal, 
        "水" to water
    )
}