// model/domain/entity/NameComposition.kt
package com.ssc.namespring.model.domain.entity

import com.ssc.namespring.model.domain.converter.NameCompositionConverter
import com.ssc.namespring.model.domain.factory.NameCompositionFactory

/**
 * 이름 구성을 관리하는 data class
 * 
 * @property characters 전체 이름 문자 리스트
 * @property visibleCount 화면에 표시되는 문자 개수 (1~4)
 */
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

    fun addCharacter(): NameComposition = 
        NameCompositionOperations.addCharacter(this)

    fun removeCharacter(): NameComposition = 
        NameCompositionOperations.removeCharacter(this)

    fun updateCharacter(position: Int, updater: (NameCharacter) -> NameCharacter): NameComposition =
        NameCompositionUpdater.updateCharacter(this, position, updater)

    fun getCharacter(position: Int): NameCharacter? =
        characters.getOrNull(position)

    fun clearCharacter(position: Int): NameComposition =
        NameCompositionUpdater.clearCharacter(this, position)

    fun toGivenNameInfo(): GivenNameInfo? =
        NameCompositionConverter.toGivenNameInfo(this)

    fun reset(): NameComposition = NameComposition()

    companion object {
        fun fromGivenNameInfo(givenNameInfo: GivenNameInfo?): NameComposition =
            NameCompositionFactory.fromGivenNameInfo(givenNameInfo)
    }
}
