// model/common/util/ElementUtils.kt
package com.ssc.namespring.model.common.util

import com.ssc.namespring.model.common.constants.Constants
import java.text.Normalizer

object ElementUtils {

    fun normalize(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFC)
    }

    fun isHarmoniousElementCombination(elementCombination: String): Pair<Boolean, List<Map<String, Any>>> {
        val elements = Constants.ELEMENTS
        var scoreCoexist = 0
        val elementHarmonyDetails = mutableListOf<Map<String, Any>>()

        for (i in 1 until elementCombination.length) {
            val prevElement = elements.indexOf(elementCombination[i - 1].toString())
            val currElement = elements.indexOf(elementCombination[i].toString())
            val diff = (currElement - prevElement + Constants.ELEMENT_COUNT) % Constants.ELEMENT_COUNT

            if (diff == Constants.ELEMENT_HARMONY_DIFF_1 || diff == Constants.ELEMENT_HARMONY_DIFF_2) {
                scoreCoexist++
                elementHarmonyDetails.add(mapOf(
                    "position" to "${i-1}-$i",
                    "elements" to "${elementCombination[i-1]}-${elementCombination[i]}",
                    "relation" to "harmonious",
                    "diff" to diff
                ))
            } else if (diff == Constants.ELEMENT_CONFLICT_DIFF_1 || diff == Constants.ELEMENT_CONFLICT_DIFF_2) {
                elementHarmonyDetails.add(mapOf(
                    "position" to "${i-1}-$i",
                    "elements" to "${elementCombination[i-1]}-${elementCombination[i]}",
                    "relation" to "conflicting",
                    "diff" to diff
                ))
                return false to elementHarmonyDetails
            }
        }

        return (scoreCoexist > 0) to elementHarmonyDetails
    }
}