// model/domain/service/name/GivenNameInfoFactory.kt
package com.ssc.namespring.model.domain.service.name

import android.util.Log
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.NameComposition
import com.ssc.namespring.model.domain.entity.CharInfo
import com.ssc.namespring.model.presentation.components.NameCharData

class GivenNameInfoFactory {
    companion object {
        private const val TAG = "GivenNameInfoFactory"
    }

    fun create(
        nameComposition: NameComposition,
        stateManager: NameCompositionStateManager,
        charDataProvider: () -> List<NameCharData>
    ): GivenNameInfo? {
        Log.d(TAG, "createGivenNameInfo called")

        // 현재 상태 로깅
        logCurrentState(stateManager)

        // nameComposition에서 GivenNameInfo 생성 시도
        var givenNameInfo = nameComposition.toGivenNameInfo()

        // 실패 시 대체 방법으로 생성
        if (givenNameInfo == null) {
            givenNameInfo = createFromCharData(nameComposition, charDataProvider)
        }

        // 결과 로깅
        logResult(givenNameInfo)

        return givenNameInfo
    }

    private fun createFromCharData(
        nameComposition: NameComposition,
        charDataProvider: () -> List<NameCharData>
    ): GivenNameInfo? {
        val charDataList = charDataProvider()

        // 모든 문자가 비어있는 경우만 null 반환
        if (charDataList.all { it.korean.isEmpty() && it.hanja.isEmpty() }) {
            return null
        }

        // 빈 값도 포함하여 생성
        val korean = charDataList.joinToString("") { it.korean }
        val hanja = charDataList.joinToString("") { it.hanja }
        val charInfos = charDataList.map { charData ->
            CharInfo(korean = charData.korean, hanja = charData.hanja)
        }

        Log.d(TAG, "Creating GivenNameInfo with data: korean='$korean', hanja='$hanja'")
        return GivenNameInfo(korean, hanja, charInfos)
    }

    private fun logCurrentState(stateManager: NameCompositionStateManager) {
        Log.d(TAG, "Current state:")
        stateManager.getState().currentStateMap.forEach { (pos, stateData) ->
            Log.d(TAG, "  Position $pos: korean='${stateData.first}', hanja='${stateData.second}'")
        }
    }

    private fun logResult(givenNameInfo: GivenNameInfo?) {
        if (givenNameInfo != null) {
            Log.d(TAG, "Created GivenNameInfo: korean='${givenNameInfo.korean}', hanja='${givenNameInfo.hanja}'")
            givenNameInfo.charInfos.forEachIndexed { index, charInfo ->
                Log.d(TAG, "  CharInfo[$index]: korean='${charInfo.korean}', hanja='${charInfo.hanja}', " +
                        "meaning='${charInfo.meaning}', strokes=${charInfo.strokes}, " +
                        "ohaeng='${charInfo.ohaeng}', eumyang=${charInfo.eumyang}")
            }
        } else {
            Log.d(TAG, "GivenNameInfo is null - all characters are empty")
        }
    }
}