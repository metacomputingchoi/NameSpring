// model/domain/usecase/nameinput/NameInputEventHandler.kt
package com.ssc.namespring.model.domain.usecase.nameinput

import android.content.Context
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import com.ssc.namespring.model.domain.usecase.nameinput.handlers.ClearHandler
import com.ssc.namespring.model.domain.usecase.nameinput.handlers.HanjaInputHandler
import com.ssc.namespring.model.domain.usecase.nameinput.handlers.KoreanInputHandler

class NameInputEventHandler(
    private val nameDataManager: INameDataManager,
    private val stateManager: NameInputStateManager,
    private val onHanjaSearchClick: (Int) -> Unit
) {
    private val clearHandler = ClearHandler(nameDataManager, stateManager)

    fun createKoreanTextWatcher(
        index: Int,
        etHanja: EditText,
        btnSearchHanja: Button,
        context: Context
    ): TextWatcher {
        return KoreanInputHandler(
            context = context,
            nameDataManager = nameDataManager,
            etHanja = etHanja,
            btnSearchHanja = btnSearchHanja,
            index = index
        )
    }

    fun createHanjaTextWatcher(
        index: Int,
        etKorean: EditText,
        btnSearchHanja: Button,
        context: Context
    ): TextWatcher {
        return HanjaInputHandler(
            context = context,
            nameDataManager = nameDataManager,
            etKorean = etKorean,
            btnSearchHanja = btnSearchHanja,
            index = index
        )
    }

    fun handleHanjaSearchClick(index: Int) {
        onHanjaSearchClick(index)
    }

    fun handleClearClick(
        index: Int,
        etKorean: EditText,
        etHanja: EditText,
        btnSearchHanja: Button
    ) {
        clearHandler.clearInput(
            index = index,
            etKorean = etKorean,
            etHanja = etHanja,
            btnSearchHanja = btnSearchHanja
        ) {
            // TextWatcher 다시 등록
            val koreanWatcher = createKoreanTextWatcher(index, etHanja, btnSearchHanja, etKorean.context)
            val hanjaWatcher = createHanjaTextWatcher(index, etKorean, btnSearchHanja, etKorean.context)

            etKorean.addTextChangedListener(koreanWatcher)
            etHanja.addTextChangedListener(hanjaWatcher)

            stateManager.addTextWatchers(index, koreanWatcher, hanjaWatcher)
        }
    }

    fun cleanup() {
        // cleanup 시 모든 position의 job 취소
        for (i in 0..3) {
            NameInputButtonUpdater.cancelUpdate(i)
        }
    }
}
