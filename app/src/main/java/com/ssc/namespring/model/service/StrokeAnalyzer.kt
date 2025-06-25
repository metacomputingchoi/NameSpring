// model/service/StrokeAnalyzer.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.Constants
import com.ssc.namespring.model.repository.DataRepository
import com.ssc.namespring.model.repository.HanjaRepository
import com.ssc.namespring.model.util.normalizeNFC

class StrokeAnalyzer(
    private val dataRepository: DataRepository,
    private val hanjaRepository: HanjaRepository
) {

    fun getHanjaStrokeCount(char: String): Int? {
        val normalizedChar = char.normalizeNFC()

        val searchMaps = listOf(
            dataRepository.nameHanjaToTripleKeys to dataRepository.nameCharTripleDict,
            dataRepository.surnameHanjaToTripleKeys to dataRepository.surnameCharTripleDict
        )

        searchMaps.forEach { (tripleKeys, charDict) ->
            tripleKeys[normalizedChar]?.forEach { tripleKey ->
                (charDict[tripleKey]?.get(Constants.JsonKeys.INTEGRATED_INFO) as? Map<*, *>)?.let { info ->
                    return (info[Constants.JsonKeys.ORIGINAL_STROKE] as? Number)?.toInt()
                }
            }
        }

        return null
    }
}