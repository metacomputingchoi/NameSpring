package com.ssc.namespring.model.domain.entity

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
        val visibleChars = visibleCharacters

        // 최소한 하나의 문자가 있어야 함 (한글 또는 한자)
        if (visibleChars.isEmpty() || visibleChars.all { it.korean.isEmpty() && it.hanja.isEmpty() }) {
            return null
        }

        val charInfos = visibleChars.map { nameChar ->
            nameChar.charInfo ?: CharInfo(
                korean = nameChar.korean,
                hanja = nameChar.hanja,
                meaning = null,
                strokes = 0,
                ohaeng = null,
                eumyang = 0
            )
        }

        // 한글 이름: 비어있으면 빈 문자열로 처리 (◯ 표시는 UI에서 처리)
        val koreanName = visibleChars.joinToString("") { it.korean }
        // 한자 이름: 비어있으면 빈 문자열로 처리
        val hanjaName = visibleChars.joinToString("") { it.hanja }

        return GivenNameInfo(
            korean = koreanName,
            hanja = hanjaName,
            charInfos = charInfos
        )
    }

    fun reset(): NameComposition {
        return NameComposition()
    }

    companion object {
        fun fromGivenNameInfo(givenNameInfo: GivenNameInfo?): NameComposition {
            if (givenNameInfo == null || givenNameInfo.charInfos.isEmpty()) {
                return NameComposition()
            }

            val characters = givenNameInfo.charInfos.mapIndexed { index, charInfo ->
                NameCharacter(
                    position = index,
                    korean = charInfo.korean,
                    hanja = charInfo.hanja,
                    charInfo = charInfo
                )
            }

            return NameComposition(
                characters = characters,
                visibleCount = characters.size
            )
        }
    }
}