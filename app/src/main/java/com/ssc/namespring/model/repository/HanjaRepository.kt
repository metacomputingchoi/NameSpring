// model/repository/HanjaRepository.kt
package com.ssc.namespring.model.repository

import com.ssc.namespring.model.common.Constants.JsonKeys
import com.ssc.namespring.model.data.HanjaInfo
import com.ssc.namespring.model.util.normalizeNFC

class HanjaRepository(private val dataRepository: DataRepository) {
    val hanjaInfoList = mutableListOf<HanjaInfo>()
    val hanjaByStroke = mutableMapOf<Int, MutableList<HanjaInfo>>()

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
                    pronunciationYinYang = info[JsonKeys.PRONUNCIATION_YINYANG] as? String ?: "",
                    strokeYinYang = info[JsonKeys.STROKE_YINYANG] as? String ?: "",
                    pronunciationElement = (info[JsonKeys.PRONUNCIATION_ELEMENT] as? String ?: "").normalizeNFC(),
                    sourceElement = (info[JsonKeys.SOURCE_ELEMENT] as? String ?: "").normalizeNFC(),
                    originalStroke = (info[JsonKeys.ORIGINAL_STROKE] as? Number)?.toInt() ?: 0,
                    dictionaryStroke = (info[JsonKeys.DICTIONARY_STROKE] as? Number)?.toInt() ?: 0
                )
                hanjaInfoList.add(hanjaInfo)
                hanjaByStroke.getOrPut(hanjaInfo.originalStroke) { mutableListOf() }.add(hanjaInfo)
            }
        }
    }
}