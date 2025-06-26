// model/repository/HanjaRepository.kt
package com.ssc.namespring.model.repository

import com.ssc.namespring.model.common.parsing.ParsingConstants.JsonKeys
import com.ssc.namespring.model.data.HanjaInfo
import com.ssc.namespring.model.exception.NamingException
import com.ssc.namespring.model.util.normalizeNFC

class HanjaRepository(private val dataRepository: DataRepository) {
    val hanjaInfoList = mutableListOf<HanjaInfo>()
    val hanjaByHoeksu = mutableMapOf<Int, MutableList<HanjaInfo>>()

    init {
        initializeHanjaInfo()
    }

    private fun initializeHanjaInfo() {
        try {
            dataRepository.nameCharTripleDict.forEach { (key, data) ->
                (data[JsonKeys.INTEGRATED_INFO] as? Map<*, *>)?.let { info ->

                    val baleumEumyangValue = when (val value = info[JsonKeys.PRONUNCIATION_YINYANG]) {
                        is Number -> if (value.toInt() == 0) "陰" else "陽"
                        is String -> value
                        else -> ""
                    }

                    val hoeksuEumyangValue = when (val value = info[JsonKeys.STROKE_YINYANG]) {
                        is Number -> if (value.toInt() == 0) "陰" else "陽"
                        is String -> value
                        else -> ""
                    }

                    val hanjaInfo = HanjaInfo(
                        hanja = (info[JsonKeys.HANJA] as? String ?: "").normalizeNFC(),
                        inmyongMeaning = (info[JsonKeys.INMYONG_MEANING] as? String ?: "").normalizeNFC(),
                        inmyongSound = (info[JsonKeys.INMYONG_SOUND] as? String ?: "").normalizeNFC(),
                        baleumEumyang = baleumEumyangValue,
                        hoeksuEumyang = hoeksuEumyangValue,
                        baleumOhaeng = (info[JsonKeys.PRONUNCIATION_ELEMENT] as? String ?: "").normalizeNFC(),
                        jawonOhaeng = (info[JsonKeys.SOURCE_ELEMENT] as? String ?: "").normalizeNFC(),
                        wonHoeksu = (info[JsonKeys.ORIGINAL_STROKE] as? Number)?.toInt() ?: 0,
                        okpyeonHoeksu = (info[JsonKeys.DICTIONARY_STROKE] as? Number)?.toInt() ?: 0
                    )
                    hanjaInfoList.add(hanjaInfo)
                    hanjaByHoeksu.getOrPut(hanjaInfo.wonHoeksu) { mutableListOf() }.add(hanjaInfo)
                }
            }
        } catch (e: Exception) {
            throw NamingException.HanjaException(
                "한자 정보 초기화 실패",
                cause = e
            )
        }
    }

    fun findByHanja(hanja: String): HanjaInfo? {
        val normalizedHanja = hanja.normalizeNFC()
        return hanjaInfoList.find { it.hanja.normalizeNFC() == normalizedHanja }
    }
}