// model/common/utils/PillarUtils.kt
package com.ssc.namespring.model.common.utils

import com.ssc.namespring.model.domain.entity.Pillar

object PillarUtils {
    fun fromPillarString(pillar: String): Pillar {
        require(pillar.length == 2) { "간지는 2글자여야 합니다: $pillar" }

        val heavenlyStem = pillar[0].toString()
        val earthlyBranch = pillar[1].toString()

        val stemOhaeng = PillarConstants.HEAVENLY_STEM_OHAENG[heavenlyStem]
            ?: throw IllegalArgumentException("알 수 없는 천간: $heavenlyStem")
        val branchOhaeng = PillarConstants.EARTHLY_BRANCH_OHAENG[earthlyBranch]
            ?: throw IllegalArgumentException("알 수 없는 지지: $earthlyBranch")

        return Pillar(heavenlyStem, earthlyBranch, stemOhaeng, branchOhaeng)
    }

    fun getEumyang(pillar: String): Pair<Int, Int> {
        require(pillar.length == 2) { "간지는 2글자여야 합니다: $pillar" }

        val heavenlyStem = pillar[0].toString()
        val earthlyBranch = pillar[1].toString()

        val stemEumyang = PillarConstants.HEAVENLY_STEM_EUMYANG[heavenlyStem] ?: 0
        val branchEumyang = PillarConstants.EARTHLY_BRANCH_EUMYANG[earthlyBranch] ?: 0

        return stemEumyang to branchEumyang
    }
}