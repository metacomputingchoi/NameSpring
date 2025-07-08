// model/domain/entity/NameCompositionUpdater.kt
package com.ssc.namespring.model.domain.entity

/**
 * NameComposition의 캐릭터 업데이트를 담당하는 클래스
 */
internal object NameCompositionUpdater {

    fun updateCharacter(
        composition: NameComposition,
        position: Int,
        updater: (NameCharacter) -> NameCharacter
    ): NameComposition {
        if (position !in composition.characters.indices) {
            // 필요한 경우 빈 캐릭터로 리스트 확장
            val newCharacters = composition.characters.toMutableList()
            while (newCharacters.size <= position) {
                newCharacters.add(NameCharacter(newCharacters.size))
            }
            newCharacters[position] = updater(NameCharacter(position))
            return composition.copy(characters = newCharacters)
        }

        return composition.copy(
            characters = composition.characters.mapIndexed { index, char ->
                if (index == position) updater(char) else char
            }
        )
    }

    fun clearCharacter(composition: NameComposition, position: Int): NameComposition {
        return updateCharacter(composition, position) {
            NameCharacter(position) // 빈 캐릭터로 초기화
        }
    }
}
