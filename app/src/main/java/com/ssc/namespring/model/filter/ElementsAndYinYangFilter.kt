// model/filter/ElementsAndYinYangFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import kotlin.math.abs

class ElementsAndYinYangFilter(
    private val getHangulElement: (Char) -> String?,
    private val getHangulPn: (Char) -> Int?,
    private val isHarmoniousElementCombination: (String) -> Boolean
) : NameFilterStrategy {

    override fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName> {
        val surElements = context.surHangul.mapNotNull { getHangulElement(it) }
        val surPns = context.surHangul.mapNotNull { getHangulPn(it) }

        return names.filter { name ->
            val nameElements = name.combinedPronounciation.mapNotNull { getHangulElement(it) }
            val namePns = name.combinedPronounciation.mapNotNull { getHangulPn(it) }

            val combinedElement = (surElements + nameElements).joinToString("")
            val combinedPm = (surPns + namePns).joinToString("") { it.toString() }
            val totalChars = context.surLength + context.nameLength

            if (combinedElement.length != totalChars || combinedPm.length != totalChars) {
                return@filter false
            }

            if (!checkYinYangBalance(combinedPm, context.surLength, context.nameLength)) {
                return@filter false
            }

            isHarmoniousElementCombination(combinedElement)
        }
    }

    private fun checkYinYangBalance(pm: String, surLength: Int, nameLength: Int): Boolean {
        val pmSet = pm.toSet()
        if (pmSet.size < Constants.YinYangBalance.MIN_VARIETY) return false

        val pmList = pm.map { it.toString().toInt() }

        return when (surLength to nameLength) {
            Constants.NameLengthCombinations.SINGLE_SINGLE -> pm[0] != pm[pm.length - 1]
            Constants.NameLengthCombinations.SINGLE_DOUBLE -> pm[0] != pm[pm.length - 1]
            Constants.NameLengthCombinations.SINGLE_TRIPLE -> checkConsecutiveCount(pmList, Constants.YinYangBalance.MAX_CONSECUTIVE_SINGLE)
            Constants.NameLengthCombinations.SINGLE_QUAD -> pmList.count { it == 0 } == pmList.count { it == 1 } && checkConsecutiveCount(pmList, Constants.YinYangBalance.MAX_CONSECUTIVE_DOUBLE)
            Constants.NameLengthCombinations.DOUBLE_SINGLE -> pm[0] != pm[pm.length - 1]
            Constants.NameLengthCombinations.DOUBLE_DOUBLE -> pmList.count { it == 0 } == pmList.count { it == 1 } && checkConsecutiveCount(pmList, Constants.YinYangBalance.MAX_CONSECUTIVE_SINGLE)
            Constants.NameLengthCombinations.DOUBLE_TRIPLE -> abs(pmList.count { it == 0 } - pmList.count { it == 1 }) == 1 && checkConsecutiveCount(pmList, Constants.YinYangBalance.MAX_CONSECUTIVE_DOUBLE)
            Constants.NameLengthCombinations.DOUBLE_QUAD -> pmList.count { it == 0 } == pmList.count { it == 1 } && checkConsecutiveCount(pmList, Constants.YinYangBalance.MAX_CONSECUTIVE_TRIPLE)
            else -> true
        }
    }

    private fun checkConsecutiveCount(pmList: List<Int>, maxAllowed: Int): Boolean {
        var consecutiveCount = (1 until pmList.size).count { pmList[it] == pmList[it - 1] }
        if (pmList.first() == pmList.last()) consecutiveCount++
        return consecutiveCount <= maxAllowed
    }
}
