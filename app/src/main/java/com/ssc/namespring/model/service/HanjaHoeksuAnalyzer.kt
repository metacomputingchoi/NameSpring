// model/service/HanjaHoeksuAnalyzer.kt
package com.ssc.namespring.model.service

import com.ssc.namespring.model.common.parsing.ParsingConstants
import com.ssc.namespring.model.repository.DataRepository
import com.ssc.namespring.model.repository.HanjaRepository
import com.ssc.namespring.model.util.normalizeNFC

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
