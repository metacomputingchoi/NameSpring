// model/domain/usecase/profileform/ProfileFormNameDataManager.kt
package com.ssc.namespring.model.domain.usecase.profileform

import com.ssc.namespring.model.presentation.components.NameCharData
import com.ssc.namespring.model.domain.entity.CharInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.NameData
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager

class ProfileFormNameDataManager : INameDataManager {
    private val nameCharDataList = mutableListOf<NameCharData>()
    private val selectedHanjaInfo = mutableMapOf<Int, CharTripleInfo>()
    private var displayCount = 1

    init {
        reset()
    }

    override fun initialize() {
        nameCharDataList.clear()
        nameCharDataList.add(NameCharData())
        displayCount = 1
    }

    override fun loadFromProfile(profile: Profile) {
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

    override fun canAddChar(): Boolean = displayCount < 4

    override fun canRemoveChar(): Boolean = displayCount > 1

    override fun addChar() {
        if (nameCharDataList.size <= displayCount) {
            nameCharDataList.add(NameCharData())
        }
        displayCount++
    }

    override fun removeChar() {
        if (displayCount > 1) {
            displayCount--
        }
    }

    override fun setCharData(position: Int, korean: String, hanja: String) {
        if (position < nameCharDataList.size) {
            nameCharDataList[position].korean = korean
            nameCharDataList[position].hanja = hanja
        }
    }

    override fun setHanjaInfo(position: Int, info: CharTripleInfo) {
        selectedHanjaInfo[position] = info
    }

    override fun removeHanjaInfo(position: Int) {
        selectedHanjaInfo.remove(position)
    }

    override fun getCharCount(): Int = displayCount

    override fun getCharDataList(): List<NameCharData> = nameCharDataList.take(displayCount)

    override fun getCharData(position: Int): NameCharData? =
        if (position < nameCharDataList.size) nameCharDataList[position] else null

    override fun getHanjaInfo(position: Int): CharTripleInfo? = selectedHanjaInfo[position]

    override fun createGivenNameInfo(): GivenNameInfo? {
        val charInfos = mutableListOf<CharInfo>()

        // 모든 displayCount만큼의 자리를 보존 (빈 자리도 포함)
        for (i in 0 until displayCount) {
            val data = if (i < nameCharDataList.size) nameCharDataList[i] else NameCharData()
            val info = selectedHanjaInfo[i]

            charInfos.add(CharInfo(
                korean = data.korean,  // 빈 문자열도 허용
                hanja = data.hanja,    // 빈 문자열도 허용
                meaning = info?.integratedInfo?.nameMeaning,
                strokes = info?.hanjaInfo?.strokes ?: 0,
                ohaeng = info?.hanjaInfo?.ohaeng,
                eumyang = info?.hanjaInfo?.eumyang ?: 0
            ))
        }

        return if (charInfos.isNotEmpty()) {
            GivenNameInfo(
                korean = charInfos.joinToString("") { it.korean.ifEmpty { "◯" } },
                hanja = charInfos.joinToString("") { it.hanja.ifEmpty { "◯" } },
                charInfos = charInfos
            )
        } else null
    }

    override fun reset() {
        nameCharDataList.clear()
        selectedHanjaInfo.clear()
        nameCharDataList.add(NameCharData())
        displayCount = 1
    }
}