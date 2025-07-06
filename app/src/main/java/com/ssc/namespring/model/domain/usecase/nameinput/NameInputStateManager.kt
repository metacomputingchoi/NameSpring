// model/domain/usecase/nameinput/NameInputStateManager.kt
package com.ssc.namespring.model.domain.usecase.nameinput

import android.text.TextWatcher
import android.widget.EditText

class NameInputStateManager {
    private val textWatchers = mutableMapOf<Int, Pair<TextWatcher, TextWatcher>>()

    fun addTextWatchers(index: Int, koreanWatcher: TextWatcher, hanjaWatcher: TextWatcher) {
        textWatchers[index] = Pair(koreanWatcher, hanjaWatcher)
    }

    fun removeTextWatchers(index: Int, etKorean: EditText, etHanja: EditText) {
        textWatchers[index]?.let { (koreanWatcher, hanjaWatcher) ->
            etKorean.removeTextChangedListener(koreanWatcher)
            etHanja.removeTextChangedListener(hanjaWatcher)
        }
        textWatchers.remove(index)
    }

    fun cleanup() {
        textWatchers.clear()
    }
}