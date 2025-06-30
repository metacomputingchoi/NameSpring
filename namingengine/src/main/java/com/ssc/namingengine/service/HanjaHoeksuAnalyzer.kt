// model/service/HanjaHoeksuAnalyzer.kt
package com.ssc.namingengine.service

import com.ssc.namingengine.common.parsing.ParsingConstants
import com.ssc.namingengine.repository.DataRepository
import com.ssc.namingengine.repository.HanjaRepository
import com.ssc.namingengine.util.normalizeNFC

class HanjaHoeksuAnalyzer(
    private val dataRepository: DataRepository,
    private val hanjaRepository: HanjaRepository
) {

    fun getHanjaHoeksu(char: String): Int? {
        val normalizedChar = char.normalizeNFC()

        val searchMaps = listOf(
            dataRepository.nameHanjaToTripleKeys to dataRepository.nameCharTripleDict,
            dataRepository.surnameHanjaToTripleKeys to dataRepository.surnameCharTripleDict
        )

        searchMaps.forEach { (tripleKeys, charDict) ->
            tripleKeys[normalizedChar]?.forEach { tripleKey ->
                (charDict[tripleKey]?.get(ParsingConstants.JsonKeys.INTEGRATED_INFO) as? Map<*, *>)?.let { info ->
                    return (info[ParsingConstants.JsonKeys.ORIGINAL_STROKE] as? Number)?.toInt()
                }
            }
        }

        return null
    }
}
