// model/domain/usecase/nameinput/handlers/KoreanInputHandler.kt
package com.ssc.namespring.model.domain.usecase.nameinput.handlers

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputButtonUpdater

class KoreanInputHandler(
    private val context: Context,
    private val nameDataManager: INameDataManager,
    private val etHanja: EditText,
    private val btnSearchHanja: Button,
    private val index: Int
) : TextWatcher {
    private var previousText = ""
    private var isUpdating = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        previousText = s?.toString() ?: ""
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating) return

        isUpdating = true
        try {
            val korean = s?.toString() ?: ""
            val currentData = nameDataManager.getCharData(index)
            val currentHanja = currentData?.hanja ?: ""

            if (korean != previousText && currentHanja.isNotEmpty()) {
                nameDataManager.setCharData(index, korean, "")
                nameDataManager.removeHanjaInfo(index)
                etHanja.setText("")
                Toast.makeText(context, "한글이 변경되어 한자가 초기화되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                nameDataManager.setCharData(index, korean, currentHanja)
            }

            updateButtonState(korean)
        } finally {
            isUpdating = false
        }
    }

    private fun updateButtonState(korean: String) {
        if (korean.isNotEmpty()) {
            NameInputButtonUpdater.updateButtonText(
                context,
                btnSearchHanja,
                korean,
                etHanja.text.toString(),
                index
            )
        } else {
            btnSearchHanja.text = "한자 선택"
            btnSearchHanja.setTextColor(context.getColor(R.color.text_secondary))
        }
    }
}
