// model/domain/entity/Pillar.kt
package com.ssc.namespring.model.domain.entity

import com.ssc.namespring.model.domain.service.utils.PillarEumyangCalculator
import com.ssc.namespring.model.domain.service.factory.PillarFactory
import java.io.Serializable

data class Pillar(
    val heavenlyStem: String,
    val earthlyBranch: String,
    val stemOhaeng: String,
    val branchOhaeng: String
) : Serializable {

    fun toPillarString(): String = heavenlyStem + earthlyBranch

    fun getPrimaryOhaeng(): String = stemOhaeng

    fun getAllOhaeng(): List<String> = listOfNotNull(
        stemOhaeng.takeIf { it.isNotEmpty() },
        branchOhaeng.takeIf { it.isNotEmpty() && it != stemOhaeng }
    )

    fun getEumyang(): Pair<Int, Int> = Companion.getEumyang(toPillarString())

    fun getDescription(): String {
        val (stemEumyang, branchEumyang) = getEumyang()
        val stemEumyangStr = if (stemEumyang == 1) "양" else "음"
        val branchEumyangStr = if (branchEumyang == 1) "양" else "음"

        return "$heavenlyStem($stemOhaeng-$stemEumyangStr)$earthlyBranch($branchOhaeng-$branchEumyangStr)"
    }

    companion object {
        fun fromPillarString(pillar: String): Pillar {
            return PillarFactory.createFromString(pillar)
        }

        fun getEumyang(pillar: String): Pair<Int, Int> {
            return PillarEumyangCalculator.calculate(pillar)
        }
    }
}