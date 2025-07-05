// model/domain/usecase/NameDataManager.kt
package com.ssc.namespring.model.domain.usecase

import com.ssc.namespring.model.presentation.components.NameCharData
import com.ssc.namespring.model.domain.entity.CharInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.NameData
import com.ssc.namespring.model.data.mapper.CharTripleInfo

class NameDataManager {
    private val nameCharDataList = mutableListOf<NameCharData>()
    private val selectedHanjaInfo = mutableMapOf<Int, CharTripleInfo>()
    private var displayCount = 1

    fun initialize() {
        nameCharDataList.clear()
        nameCharDataList.add(NameCharData())
        displayCount = 1
    }

    fun loadFromProfile(profile: Profile) {
        nameCharDataList.clear()
        selectedHanjaInfo.clear()

        profile.givenName?.let { givenName ->
            givenName.charInfos.forEach { charInfo ->
                nameCharDataList.add(
                    NameCharData(
                        korean = charInfo.korean,
                        hanja = charInfo.hanja
                    )
                )
            }

            givenName.charInfos.forEachIndexed { index, charInfo ->
                if (charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()) {
                    NameData.getCharInfo(charInfo.korean, charInfo.hanja)?.let { info ->
                        selectedHanjaInfo[index] = info
                    }
                }
            }
        }

        if (nameCharDataList.isEmpty()) {
            nameCharDataList.add(NameCharData())
        }
        displayCount = nameCharDataList.size
    }

    fun canAddChar(): Boolean = displayCount < 4

    fun canRemoveChar(): Boolean = displayCount > 1

    fun addChar() {
        if (nameCharDataList.size <= displayCount) {
            nameCharDataList.add(NameCharData())
        }
        displayCount++
    }

    fun removeChar() {
        if (displayCount > 1) {
            displayCount--
        }
    }

    fun setCharData(position: Int, korean: String, hanja: String) {
        if (position < nameCharDataList.size) {
            nameCharDataList[position].korean = korean
            nameCharDataList[position].hanja = hanja
        }
    }

    fun setHanjaInfo(position: Int, info: CharTripleInfo) {
        selectedHanjaInfo[position] = info
    }

    fun removeHanjaInfo(position: Int) {
        selectedHanjaInfo.remove(position)
    }

    fun getCharCount(): Int = displayCount

    fun getCharDataList(): List<NameCharData> = nameCharDataList.take(displayCount)

    fun getCharData(position: Int): NameCharData? =
        if (position < nameCharDataList.size) nameCharDataList[position] else null

    fun getHanjaInfo(position: Int): CharTripleInfo? = selectedHanjaInfo[position]

    fun createGivenNameInfo(): GivenNameInfo? {
        val charInfos = mutableListOf<CharInfo>()

        for (i in 0 until displayCount) {
            val data = nameCharDataList[i]
            if (data.korean.isNotEmpty() || data.hanja.isNotEmpty()) {
                val info = selectedHanjaInfo[i]
                charInfos.add(CharInfo(
                    korean = data.korean,
                    hanja = data.hanja,
                    meaning = info?.integratedInfo?.nameMeaning,
                    strokes = info?.hanjaInfo?.strokes ?: 0,
                    ohaeng = info?.hanjaInfo?.ohaeng,
                    eumyang = info?.hanjaInfo?.eumyang ?: 0
                ))
            }
        }

        return if (charInfos.isNotEmpty()) {
            GivenNameInfo(
                korean = charInfos.joinToString("") { it.korean },
                hanja = charInfos.joinToString("") { it.hanja },
                charInfos = charInfos
            )
        } else null
    }

    fun reset() {
        nameCharDataList.clear()
        selectedHanjaInfo.clear()
        nameCharDataList.add(NameCharData())
        displayCount = 1
    }
}