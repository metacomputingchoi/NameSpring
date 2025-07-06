// model/domain/entity/NameCompositionState.kt
package com.ssc.namespring.model.domain.entity

import com.ssc.namespring.model.data.mapper.CharTripleInfo

data class NameCompositionState(
    val composition: NameComposition = NameComposition(),
    val hanjaInfoMap: Map<Int, CharTripleInfo> = emptyMap(),
    val currentStateMap: Map<Int, Pair<String, String>> = emptyMap()
) {
    fun withComposition(composition: NameComposition): NameCompositionState {
        return copy(composition = composition)
    }

    fun withHanjaInfo(position: Int, info: CharTripleInfo): NameCompositionState {
        return copy(hanjaInfoMap = hanjaInfoMap + (position to info))
    }

    fun withoutHanjaInfo(position: Int): NameCompositionState {
        return copy(hanjaInfoMap = hanjaInfoMap - position)
    }

    fun withCurrentState(position: Int, korean: String, hanja: String): NameCompositionState {
        return copy(currentStateMap = currentStateMap + (position to Pair(korean, hanja)))
    }

    fun withoutCurrentState(position: Int): NameCompositionState {
        return copy(currentStateMap = currentStateMap - position)
    }

    fun reset(): NameCompositionState {
        return NameCompositionState()
    }
}