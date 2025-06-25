// model/filter/BaleumOhaengEumyangFilter.kt
package com.ssc.namespring.model.filter

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.data.FilterContext
import com.ssc.namespring.model.data.GeneratedName
import kotlin.math.abs

class BaleumOhaengEumyangFilter(
    private val getBaleumOhaeng: (Char) -> String?,
    private val getBaleumEumyang: (Char) -> Int?,
    private val checkBaleumOhaengHarmony: (String) -> Boolean
) : NameFilterStrategy {

    override fun filter(names: List<GeneratedName>, context: FilterContext): List<GeneratedName> {
        val surBaleumOhaeng = context.surHangul.mapNotNull { getBaleumOhaeng(it) }
        val surBaleumEumyang = context.surHangul.mapNotNull { getBaleumEumyang(it) }

        return names.filter { name ->
            isValid(name, context, surBaleumOhaeng, surBaleumEumyang)
        }
    }

    override fun filterBatch(names: Sequence<GeneratedName>, context: FilterContext): Sequence<GeneratedName> {
        val surBaleumOhaeng = context.surHangul.mapNotNull { getBaleumOhaeng(it) }
        val surBaleumEumyang = context.surHangul.mapNotNull { getBaleumEumyang(it) }

        return names.filter { name ->
            isValid(name, context, surBaleumOhaeng, surBaleumEumyang)
        }
    }

    private fun isValid(
        name: GeneratedName, 
        context: FilterContext,
        surBaleumOhaeng: List<String>,
        surBaleumEumyang: List<Int>
    ): Boolean {
        val nameBaleumOhaeng = name.combinedPronounciation.mapNotNull { getBaleumOhaeng(it) }
        val nameBaleumEumyang = name.combinedPronounciation.mapNotNull { getBaleumEumyang(it) }

        val combinedBaleumOhaeng = (surBaleumOhaeng + nameBaleumOhaeng).joinToString("")
        val combinedEumyang = (surBaleumEumyang + nameBaleumEumyang).joinToString("") { it.toString() }
        val totalChars = context.surLength + context.nameLength

        if (combinedBaleumOhaeng.length != totalChars || combinedEumyang.length != totalChars) {
            return false
        }

        if (!checkYinYangBalance(combinedEumyang, context.surLength, context.nameLength)) {
            return false
        }

        return checkBaleumOhaengHarmony(combinedBaleumOhaeng)
    }

    private fun checkYinYangBalance(eumyang: String, surLength: Int, nameLength: Int): Boolean {
        val eumyangSet = eumyang.toSet()
        if (eumyangSet.size < Constants.YinYangBalance.MIN_VARIETY) return false

        val eumyangList = eumyang.map { it.toString().toInt() }

        return when (surLength to nameLength) {
            Constants.NameLengthCombinations.SINGLE_SINGLE -> eumyang[0] != eumyang[eumyang.length - 1]
            Constants.NameLengthCombinations.SINGLE_DOUBLE -> eumyang[0] != eumyang[eumyang.length - 1]
            Constants.NameLengthCombinations.SINGLE_TRIPLE -> checkConsecutiveCount(eumyangList, Constants.YinYangBalance.MAX_CONSECUTIVE_SINGLE)
            Constants.NameLengthCombinations.SINGLE_QUAD -> eumyangList.count { it == 0 } == eumyangList.count { it == 1 } && checkConsecutiveCount(eumyangList, Constants.YinYangBalance.MAX_CONSECUTIVE_DOUBLE)
            Constants.NameLengthCombinations.DOUBLE_SINGLE -> eumyang[0] != eumyang[eumyang.length - 1]
            Constants.NameLengthCombinations.DOUBLE_DOUBLE -> eumyangList.count { it == 0 } == eumyangList.count { it == 1 } && checkConsecutiveCount(eumyangList, Constants.YinYangBalance.MAX_CONSECUTIVE_SINGLE)
            Constants.NameLengthCombinations.DOUBLE_TRIPLE -> abs(eumyangList.count { it == 0 } - eumyangList.count { it == 1 }) == 1 && checkConsecutiveCount(eumyangList, Constants.YinYangBalance.MAX_CONSECUTIVE_DOUBLE)
            Constants.NameLengthCombinations.DOUBLE_QUAD -> eumyangList.count { it == 0 } == eumyangList.count { it == 1 } && checkConsecutiveCount(eumyangList, Constants.YinYangBalance.MAX_CONSECUTIVE_TRIPLE)
            else -> true
        }
    }

    private fun checkConsecutiveCount(eumyangList: List<Int>, maxAllowed: Int): Boolean {
        var consecutiveCount = (1 until eumyangList.size).count { eumyangList[it] == eumyangList[it - 1] }
        if (eumyangList.first() == eumyangList.last()) consecutiveCount++
        return consecutiveCount <= maxAllowed
    }
}
