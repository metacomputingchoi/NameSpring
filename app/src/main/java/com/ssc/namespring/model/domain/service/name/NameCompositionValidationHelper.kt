// model/domain/service/name/NameCompositionValidationHelper.kt
package com.ssc.namespring.model.domain.service.name

import com.ssc.namespring.model.domain.entity.NameCharacter

/**
 * NameComposition 관련 유효성 검사를 담당하는 헬퍼 클래스
 * Single Responsibility Principle 적용
 */
class NameCompositionValidationHelper {

    fun isValidForGivenNameInfo(characters: List<NameCharacter?>): Boolean {
        // 최소 하나의 한글이라도 있으면 유효
        return characters.any { character ->
            character != null && character.korean.isNotEmpty()
        }
    }

    fun hasCompleteHanjaInfo(character: NameCharacter?): Boolean {
        return character != null &&
                character.korean.isNotEmpty() &&
                character.hanja.isNotEmpty()
    }

    fun hasPartialInfo(character: NameCharacter?): Boolean {
        return character != null &&
                (character.korean.isNotEmpty() || character.hanja.isNotEmpty())
    }

    fun hasCharInfo(character: NameCharacter?): Boolean {
        return character?.charInfo != null
    }

    fun isAllEmpty(characters: List<NameCharacter?>): Boolean {
        return characters.all { character ->
            character == null || (character.korean.isEmpty() && character.hanja.isEmpty())
        }
    }
}