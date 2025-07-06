// model/domain/usecase/nameinput/NameInputStateManager.kt
package com.ssc.namespring.model.domain.usecase.nameinput

import android.text.TextWatcher
import android.widget.EditText

class NameInputStateManager {
    private val textWatchers = mutableMapOf<Int, Pair<TextWatcher, TextWatcher>>()
    private val activePositions = mutableSetOf<Int>()

    fun addTextWatchers(index: Int, koreanWatcher: TextWatcher, hanjaWatcher: TextWatcher) {
        textWatchers[index] = Pair(koreanWatcher, hanjaWatcher)
        activePositions.add(index)
    }

    fun removeTextWatchers(index: Int, etKorean: EditText, etHanja: EditText) {
        textWatchers[index]?.let { (koreanWatcher, hanjaWatcher) ->
            etKorean.removeTextChangedListener(koreanWatcher)
            etHanja.removeTextChangedListener(hanjaWatcher)
        }
        textWatchers.remove(index)
        activePositions.remove(index)
    }

    fun cleanup() {
        // 모든 활성 position에 대해 cleanup
        activePositions.forEach { position ->
            NameInputButtonUpdater.cancelUpdate(position)
        }
        textWatchers.clear()
        activePositions.clear()
    }
}