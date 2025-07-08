// model/domain/entity/NameCompositionOperations.kt
package com.ssc.namespring.model.domain.entity

/**
 * NameComposition의 캐릭터 추가/제거 연산을 담당하는 클래스
 */
internal object NameCompositionOperations {

    fun addCharacter(composition: NameComposition): NameComposition {
        if (!composition.canAddCharacter()) return composition

        // 이미 존재하는 캐릭터가 있으면 visibleCount만 증가
        if (composition.visibleCount < composition.characters.size) {
            return composition.copy(visibleCount = composition.visibleCount + 1)
        }

        // 새로운 캐릭터 추가
        return composition.copy(
            characters = composition.characters + NameCharacter(composition.characters.size),
            visibleCount = composition.visibleCount + 1
        )
    }

    fun removeCharacter(composition: NameComposition): NameComposition {
        if (!composition.canRemoveCharacter()) return composition
        // visibleCount만 줄이고 데이터는 유지
        return composition.copy(visibleCount = composition.visibleCount - 1)
    }
}
