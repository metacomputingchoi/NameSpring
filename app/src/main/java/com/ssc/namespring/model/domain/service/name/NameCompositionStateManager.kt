// model/domain/service/name/NameCompositionStateManager.kt
package com.ssc.namespring.model.domain.service.name

import android.util.Log
import com.ssc.namespring.model.domain.entity.NameCompositionState
import com.ssc.namespring.model.domain.entity.NameComposition
import com.ssc.namespring.model.data.mapper.CharTripleInfo

class NameCompositionStateManager {
    companion object {
        private const val TAG = "NameCompositionStateManager"
    }

    private var state = NameCompositionState()
    private val compositionService = NameCompositionService()

    fun getState(): NameCompositionState = state

    fun reset() {
        Log.d(TAG, "reset()")
        state = state.reset()
    }

    fun updateComposition(composition: NameComposition) {
        state = state.withComposition(composition)
    }

    fun addHanjaInfo(position: Int, info: CharTripleInfo) {
        Log.d(TAG, "addHanjaInfo: position=$position")
        state = state.withHanjaInfo(position, info)
    }

    fun removeHanjaInfo(position: Int) {
        Log.d(TAG, "removeHanjaInfo: position=$position")
        state = state.withoutHanjaInfo(position)
            .withoutCurrentState(position)
    }

    fun updateCurrentState(position: Int, korean: String, hanja: String) {
        state = state.withCurrentState(position, korean, hanja)
    }

    fun getHanjaInfo(position: Int): CharTripleInfo? {
        return state.hanjaInfoMap[position]
    }

    fun getCurrentState(position: Int): Pair<String, String>? {
        return state.currentStateMap[position]
    }

    fun hasKoreanChanged(position: Int, newKorean: String): Boolean {
        val previousState = state.currentStateMap[position]
        return previousState?.first != newKorean
    }

    fun clearPositionData(position: Int) {
        state = state.withoutHanjaInfo(position)
            .withoutCurrentState(position)
    }
}