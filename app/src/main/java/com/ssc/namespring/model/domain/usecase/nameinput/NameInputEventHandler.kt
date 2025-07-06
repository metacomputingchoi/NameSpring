// model/domain/usecase/nameinput/NameInputEventHandler.kt
package com.ssc.namespring.model.domain.usecase.nameinput

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager

class NameInputEventHandler(
    private val nameDataManager: INameDataManager,
    private val stateManager: NameInputStateManager,
    private val onHanjaSearchClick: (Int) -> Unit
) {
    fun createKoreanTextWatcher(
        index: Int,
        etHanja: EditText,
        btnSearchHanja: Button,
        context: Context
    ): TextWatcher {
        return object : TextWatcher {
            private var previousText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val korean = s?.toString() ?: ""
                val currentData = nameDataManager.getCharData(index)
                val currentHanja = currentData?.hanja ?: ""

                // Korean changed and there was existing hanja - clear hanja
                if (korean != previousText && currentHanja.isNotEmpty()) {
                    nameDataManager.setCharData(index, korean, "")
                    nameDataManager.removeHanjaInfo(index)
                    etHanja.setText("")
                    Toast.makeText(context, "한글이 변경되어 한자가 초기화되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    nameDataManager.setCharData(index, korean, currentHanja)
                }

                NameInputButtonUpdater.updateButtonText(context, btnSearchHanja, korean, etHanja.text.toString())
            }
        }
    }

    fun createHanjaTextWatcher(
        index: Int,
        etKorean: EditText,
        btnSearchHanja: Button,
        context: Context
    ): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val hanja = s?.toString() ?: ""
                val korean = etKorean.text.toString()
                nameDataManager.setCharData(index, korean, hanja)
                NameInputButtonUpdater.updateButtonText(context, btnSearchHanja, korean, hanja)
            }
        }
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
        stateManager.removeTextWatchers(index, etKorean, etHanja)
        etKorean.setText("")
        etHanja.setText("")
        nameDataManager.removeHanjaInfo(index)
        nameDataManager.setCharData(index, "", "")
        NameInputButtonUpdater.updateButtonText(etKorean.context, btnSearchHanja, "", "")
    }
}