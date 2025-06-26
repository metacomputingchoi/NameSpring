// model/repository/HanjaRepository.kt
package com.ssc.namespring.model.repository

import com.ssc.namespring.model.common.parsing.ParsingConstants.JsonKeys
import com.ssc.namespring.model.data.HanjaInfo
import com.ssc.namespring.model.util.normalizeNFC

class HanjaRepository(private val dataRepository: DataRepository) {
    val hanjaInfoList = mutableListOf<HanjaInfo>()
    val hanjaByHoeksu = mutableMapOf<Int, MutableList<HanjaInfo>>()

    init {
        initializeHanjaInfo()
    }

    private fun initializeHanjaInfo() {
        dataRepository.nameCharTripleDict.forEach { (_, data) ->
            (data[JsonKeys.INTEGRATED_INFO] as? Map<*, *>)?.let { info ->
                val hanjaInfo = HanjaInfo(
                    hanja = (info[JsonKeys.HANJA] as? String ?: "").normalizeNFC(),
                    inmyongMeaning = (info[JsonKeys.INMYONG_MEANING] as? String ?: "").normalizeNFC(),
                    inmyongSound = (info[JsonKeys.INMYONG_SOUND] as? String ?: "").normalizeNFC(),
                    baleumEumyang = info[JsonKeys.PRONUNCIATION_YINYANG] as? String ?: "",
                    hoeksuEumyang = info[JsonKeys.STROKE_YINYANG] as? String ?: "",
                    baleumOhaeng = (info[JsonKeys.PRONUNCIATION_ELEMENT] as? String ?: "").normalizeNFC(),
                    jawonOhaeng = (info[JsonKeys.SOURCE_ELEMENT] as? String ?: "").normalizeNFC(),
                    wonHoeksu = (info[JsonKeys.ORIGINAL_STROKE] as? Number)?.toInt() ?: 0,
                    okpyeonHoeksu = (info[JsonKeys.DICTIONARY_STROKE] as? Number)?.toInt() ?: 0
                )
                hanjaInfoList.add(hanjaInfo)
                hanjaByHoeksu.getOrPut(hanjaInfo.wonHoeksu) { mutableListOf() }.add(hanjaInfo)
            }
        }
    }
}
