// model/domain/entity/NameComposition.kt
package com.ssc.namespring.model.domain.entity

import com.ssc.namespring.model.domain.service.name.NameCompositionValidationHelper

data class NameComposition(
    val characters: List<NameCharacter> = listOf(NameCharacter(0)),
    val visibleCount: Int = 1
) {
    init {
        require(visibleCount in 1..characters.size) {
            "visibleCount must be between 1 and ${characters.size}"
        }
    }

    val size: Int get() = visibleCount

    val allCharacters: List<NameCharacter> get() = characters

    val visibleCharacters: List<NameCharacter> get() = characters.take(visibleCount)

    fun canAddCharacter(): Boolean = visibleCount < 4

    fun canRemoveCharacter(): Boolean = visibleCount > 1

    fun addCharacter(): NameComposition {
        if (!canAddCharacter()) return this

        // 이미 존재하는 캐릭터가 있으면 visibleCount만 증가
        if (visibleCount < characters.size) {
            return copy(visibleCount = visibleCount + 1)
        }

        // 새로운 캐릭터 추가
        return copy(
            characters = characters + NameCharacter(characters.size),
            visibleCount = visibleCount + 1
        )
    }

    fun removeCharacter(): NameComposition {
        if (!canRemoveCharacter()) return this
        // visibleCount만 줄이고 데이터는 유지
        return copy(visibleCount = visibleCount - 1)
    }

    fun updateCharacter(position: Int, updater: (NameCharacter) -> NameCharacter): NameComposition {
        if (position !in characters.indices) {
            // 필요한 경우 빈 캐릭터로 리스트 확장
            val newCharacters = characters.toMutableList()
            while (newCharacters.size <= position) {
                newCharacters.add(NameCharacter(newCharacters.size))
            }
            newCharacters[position] = updater(NameCharacter(position))
            return copy(characters = newCharacters)
        }

        return copy(
            characters = characters.mapIndexed { index, char ->
                if (index == position) updater(char) else char
            }
        )
    }

    fun getCharacter(position: Int): NameCharacter? =
        characters.getOrNull(position)

    fun clearCharacter(position: Int): NameComposition {
        return updateCharacter(position) {
            NameCharacter(position) // 빈 캐릭터로 초기화
        }
    }

    fun toGivenNameInfo(): GivenNameInfo? {
        // 보이는 캐릭터만 사용 (빈 값도 포함)
        val visibleChars = characters.take(visibleCount)

        // 모든 문자가 비어있는 경우만 null 반환
        if (visibleChars.all { it.korean.isEmpty() && it.hanja.isEmpty() }) {
            return null
        }

        // 빈 값도 포함하여 문자열 생성
        val korean = visibleChars.joinToString("") { it.korean }
        val hanja = visibleChars.joinToString("") { it.hanja }

        // 모든 visible characters를 CharInfo로 변환 (빈 값도 포함)
        val charInfos = visibleChars.map { character ->
            CharInfo(
                korean = character.korean,
                hanja = character.hanja,
                meaning = character.charInfo?.meaning,
                strokes = character.charInfo?.strokes ?: 0,
                ohaeng = character.charInfo?.ohaeng,
                eumyang = character.charInfo?.eumyang ?: 0
            )
        }

        return GivenNameInfo(korean, hanja, charInfos)
    }

    fun reset(): NameComposition {
        return NameComposition()
    }

    companion object {
        fun fromGivenNameInfo(givenNameInfo: GivenNameInfo?): NameComposition {
            if (givenNameInfo == null) {
                return NameComposition()
            }

            // charInfos가 비어있어도 처리
            val characters = if (givenNameInfo.charInfos.isEmpty()) {
                // charInfos가 없지만 korean/hanja 문자열이 있을 수 있음
                val koreanChars = givenNameInfo.korean.toList()
                val hanjaChars = givenNameInfo.hanja.toList()
                val maxLength = maxOf(koreanChars.size, hanjaChars.size, 1)

                (0 until maxLength).map { index ->
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
            } else {
                // charInfos에서 정보 가져오기 (빈 문자도 포함)
                givenNameInfo.charInfos.mapIndexed { index, charInfo ->
                    NameCharacter(
                        position = index,
                        korean = charInfo.korean,
                        hanja = charInfo.hanja,
                        charInfo = charInfo
                    )
                }
            }

            return NameComposition(
                characters = characters,
                visibleCount = characters.size
            )
        }
    }
}