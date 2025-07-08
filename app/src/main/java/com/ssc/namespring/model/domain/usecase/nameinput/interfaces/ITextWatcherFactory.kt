// model/domain/usecase/nameinput/interfaces/ITextWatcherFactory.kt
package com.ssc.namespring.model.domain.usecase.nameinput.interfaces

import android.text.TextWatcher

interface ITextWatcherFactory {
    fun createKoreanTextWatcher(index: Int): TextWatcher
    fun createHanjaTextWatcher(index: Int): TextWatcher
}

interface IInputHandler {
    fun handleInput(index: Int, text: String)
}
