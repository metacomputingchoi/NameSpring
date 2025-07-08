// model/domain/factory/NameCompositionFactory.kt
package com.ssc.namespring.model.domain.factory

import com.ssc.namespring.model.domain.entity.*

/**
 * NameComposition 인스턴스 생성을 담당하는 Factory 클래스
 */
object NameCompositionFactory {

    fun create(): NameComposition {
        return NameComposition()
    }

    fun fromGivenNameInfo(givenNameInfo: GivenNameInfo?): NameComposition {
        if (givenNameInfo == null) {
            return NameComposition()
        }

        // charInfos가 비어있어도 처리
        val characters = if (givenNameInfo.charInfos.isEmpty()) {
            createCharactersFromStrings(givenNameInfo)
        } else {
            createCharactersFromInfos(givenNameInfo)
        }

        return NameComposition(
            characters = characters,
            visibleCount = characters.size
        )
    }

    private fun createCharactersFromStrings(givenNameInfo: GivenNameInfo): List<NameCharacter> {
        val koreanChars = givenNameInfo.korean.toList()
        val hanjaChars = givenNameInfo.hanja.toList()
        val maxLength = maxOf(koreanChars.size, hanjaChars.size, 1)

        return (0 until maxLength).map { index ->
            NameCharacter(
                position = index,
                korean = koreanChars.getOrNull(index)?.toString() ?: "",
                hanja = hanjaChars.getOrNull(index)?.toString() ?: "",
                charInfo = CharInfo(
                    korean = koreanChars.getOrNull(index)?.toString() ?: "",
                    hanja = hanjaChars.getOrNull(index)?.toString() ?: ""
                )
            )
        }
    }

    private fun createCharactersFromInfos(givenNameInfo: GivenNameInfo): List<NameCharacter> {
        return givenNameInfo.charInfos.mapIndexed { index, charInfo ->
            NameCharacter(
                position = index,
                korean = charInfo.korean,
                hanja = charInfo.hanja,
                charInfo = charInfo
            )
        }
    }
}
