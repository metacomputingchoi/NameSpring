// model/domain/converter/NameCompositionConverter.kt
package com.ssc.namespring.model.domain.converter

import com.ssc.namespring.model.domain.entity.*

/**
 * NameComposition과 GivenNameInfo 간의 변환을 담당하는 클래스
 */
object NameCompositionConverter {

    fun toGivenNameInfo(composition: NameComposition): GivenNameInfo? {
        // 보이는 캐릭터만 사용 (빈 값도 포함)
        val visibleChars = composition.characters.take(composition.visibleCount)

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
}
