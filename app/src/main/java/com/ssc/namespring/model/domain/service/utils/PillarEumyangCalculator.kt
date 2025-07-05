// model/domain/service/utils/PillarEumyangCalculator.kt
package com.ssc.namespring.model.domain.service.utils

import com.ssc.namespring.model.common.utils.PillarConstants

object PillarEumyangCalculator {
    fun calculate(pillar: String): Pair<Int, Int> {
        require(pillar.length == 2) { "간지는 2글자여야 합니다: $pillar" }

        val heavenlyStem = pillar[0].toString()
        val earthlyBranch = pillar[1].toString()

        val stemEumyang = PillarConstants.HEAVENLY_STEM_EUMYANG[heavenlyStem] ?: 0
        val branchEumyang = PillarConstants.EARTHLY_BRANCH_EUMYANG[earthlyBranch] ?: 0

        return stemEumyang to branchEumyang
    }
}
