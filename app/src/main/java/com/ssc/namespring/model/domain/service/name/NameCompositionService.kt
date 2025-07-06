// model/domain/service/name/NameCompositionService.kt
package com.ssc.namespring.model.domain.service.name

import android.util.Log
import com.ssc.namespring.model.domain.entity.CharInfo
import com.ssc.namespring.model.domain.entity.NameCharacter
import com.ssc.namespring.model.domain.entity.NameComposition
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import com.ssc.namespring.model.domain.service.factory.NameDataServiceFactory

class NameCompositionService {
    companion object {
        private const val TAG = "NameCompositionService"
    }

    private val nameDataService: INameDataService = NameDataServiceFactory.getInstance()

    fun updateCharacterWithHanjaInfo(
        character: NameCharacter,
        korean: String,
        hanja: String
    ): NameCharacter {
        val charInfo = if (korean.isNotEmpty() && hanja.isNotEmpty()) {
            nameDataService.getCharInfo(korean, hanja)?.let { tripleInfo ->
                convertToCharInfo(tripleInfo)
            } ?: CharInfo(korean = korean, hanja = hanja)
        } else {
            CharInfo(korean = korean, hanja = hanja)
        }

        return character
            .withKorean(korean)
            .withHanja(hanja)
            .withCharInfo(charInfo)
    }

    fun updateCharacterWithTripleInfo(
        character: NameCharacter,
        tripleInfo: CharTripleInfo
    ): NameCharacter {
        try {
            val charInfo = convertToCharInfo(tripleInfo)
            val korean = tripleInfo.koreanInfo?.character ?: ""
            val hanja = tripleInfo.hanjaInfo?.character ?: ""

            return character
                .withKorean(korean)
                .withHanja(hanja)
                .withCharInfo(charInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting CharTripleInfo", e)
            return character
        }
    }

    fun updateCharacterKorean(
        character: NameCharacter,
        newKorean: String,
        shouldClearHanja: Boolean
    ): NameCharacter {
        return if (shouldClearHanja && character.korean != newKorean && character.hanja.isNotEmpty()) {
            character.withKorean(newKorean).clearHanja()
        } else {
            character.withKorean(newKorean)
        }
    }

    fun updateCharacterHanja(
        character: NameCharacter,
        newHanja: String
    ): NameCharacter {
        return character.withHanja(newHanja)
    }

    private fun convertToCharInfo(tripleInfo: CharTripleInfo): CharInfo {
        return CharInfo(
            korean = tripleInfo.koreanInfo?.character ?: "",
            hanja = tripleInfo.hanjaInfo?.character ?: "",
            meaning = tripleInfo.integratedInfo?.nameMeaning,
            strokes = tripleInfo.hanjaInfo?.strokes ?: 0,
            ohaeng = tripleInfo.hanjaInfo?.ohaeng ?: "",
            eumyang = tripleInfo.hanjaInfo?.eumyang ?: 0
        )
    }
}