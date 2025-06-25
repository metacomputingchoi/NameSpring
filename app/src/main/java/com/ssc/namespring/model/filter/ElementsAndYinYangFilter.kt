// model/filter/ElementsAndYinYangFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.Constants
import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import com.ssc.namespring.model.util.toHangulDecomposition
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
        if (pmSet.size <= 1) return false

        val pmList = pm.map { it.toString().toInt() }

        return when (surLength to nameLength) {
            1 to 1 -> pm[0] != pm[pm.length - 1]
            1 to 2 -> pm[0] != pm[pm.length - 1]
            1 to 3 -> checkConsecutiveCount(pmList, 1)
            1 to 4 -> pmList.count { it == 0 } == pmList.count { it == 1 } && checkConsecutiveCount(pmList, 2)
            2 to 1 -> pm[0] != pm[pm.length - 1]
            2 to 2 -> pmList.count { it == 0 } == pmList.count { it == 1 } && checkConsecutiveCount(pmList, 1)
            2 to 3 -> abs(pmList.count { it == 0 } - pmList.count { it == 1 }) == 1 && checkConsecutiveCount(pmList, 2)
            2 to 4 -> pmList.count { it == 0 } == pmList.count { it == 1 } && checkConsecutiveCount(pmList, 3)
            else -> true
        }
    }

    private fun checkConsecutiveCount(pmList: List<Int>, maxAllowed: Int): Boolean {
        var consecutiveCount = (1 until pmList.size).count { pmList[it] == pmList[it - 1] }
        if (pmList.first() == pmList.last()) consecutiveCount++
        return consecutiveCount <= maxAllowed
    }
}
