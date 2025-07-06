// model/domain/service/name/NameCharacterUpdateService.kt
package com.ssc.namespring.model.domain.service.name

import android.util.Log
import com.ssc.namespring.model.domain.entity.NameComposition
import com.ssc.namespring.model.domain.entity.CharInfo
import com.ssc.namespring.model.data.mapper.CharTripleInfo

class NameCharacterUpdateService(
    private val stateManager: NameCompositionStateManager,
    private val compositionService: NameCompositionService
) {
    companion object {
        private const val TAG = "NameCharacterUpdateService"
    }

    fun updateCharacterData(
        composition: NameComposition,
        position: Int,
        korean: String,
        hanja: String
    ): NameComposition {
        Log.d(TAG, "updateCharacterData: position=$position, korean='$korean', hanja='$hanja'")

        val koreanChanged = stateManager.hasKoreanChanged(position, korean)
        val currentChar = composition.getCharacter(position)

        // 상태 업데이트
        stateManager.updateCurrentState(position, korean, hanja)

        // 한글이 변경되었고 기존에 한자가 있었으면 한자 정보 제거
        if (koreanChanged && currentChar?.hanja?.isNotEmpty() == true) {
            stateManager.removeHanjaInfo(position)
            Log.d(TAG, "Korean changed, removing hanja info for position $position")
        }

        // NameComposition 업데이트
        return composition.updateCharacter(position) { character ->
            when {
                koreanChanged && character.hanja.isNotEmpty() -> {
                    // 한글이 변경되면 한자를 초기화
                    character.withKorean(korean).clearHanja()
                }
                korean.isNotEmpty() && hanja.isNotEmpty() -> {
                    // 한글과 한자가 모두 있는 경우
                    val charInfo = stateManager.getHanjaInfo(position)?.let { info ->
                        convertToCharInfo(info)
                    } ?: CharInfo(korean = korean, hanja = hanja)

                    character
                        .withKorean(korean)
                        .withHanja(hanja)
                        .withCharInfo(charInfo)
                }
                else -> {
                    // 그 외의 경우
                    character
                        .withKorean(korean)
                        .withHanja(hanja)
                        .withCharInfo(null)
                }
            }
        }
    }

    fun updateCharacterWithHanjaInfo(
        composition: NameComposition,
        position: Int,
        info: CharTripleInfo
    ): NameComposition {
        try {
            val korean = info.koreanInfo?.character ?: return composition
            val hanja = info.hanjaInfo?.character ?: return composition

            Log.d(TAG, "updateCharacterWithHanjaInfo: position=$position, korean='$korean', hanja='$hanja'")

            // 한자 정보 저장
            stateManager.addHanjaInfo(position, info)
            stateManager.updateCurrentState(position, korean, hanja)

            // NameComposition 업데이트
            return composition.updateCharacter(position) { character ->
                compositionService.updateCharacterWithTripleInfo(character, info)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in updateCharacterWithHanjaInfo", e)
            return composition
        }
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