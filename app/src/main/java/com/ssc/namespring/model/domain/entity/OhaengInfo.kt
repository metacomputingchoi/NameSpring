// model/domain/entity/OhaengInfo.kt
package com.ssc.namespring.model.domain.entity

import java.io.Serializable

data class OhaengInfo(
    val wood: Int = 0,
    val fire: Int = 0,
    val earth: Int = 0,
    val metal: Int = 0,
    val water: Int = 0
) : Serializable {
    fun getLackingOhaeng(): List<String> {
        val ohaengMap = mapOf(
            "목" to wood,
            "화" to fire,
            "토" to earth,
            "금" to metal,
            "수" to water
        )
        return ohaengMap.filter { it.value == 0 }.keys.toList()
    }

    fun getExcessOhaeng(): List<String> {
        val ohaengMap = mapOf(
            "목" to wood,
            "화" to fire,
            "토" to earth,
            "금" to metal,
            "수" to water
        )
        val total = wood + fire + earth + metal + water
        if (total == 0) return emptyList()

        val avg = total / 5.0
        return ohaengMap.filter { it.value > avg * 1.5 && it.value >= 3 }.keys.toList()
    }

    override fun toString(): String {
        return "OhaengInfo(목=$wood, 화=$fire, 토=$earth, 금=$metal, 수=$water)"
    }
}