// model/business/SimpleTextWatchers.kt
package com.ssc.namespring.model.business

import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import java.text.Normalizer

class SimpleKoreanTextWatcher(
    private val index: Int,
    private val onTextChanged: (Int, String) -> Unit
) : TextWatcher {

    private var previousText = ""
    private var isInternalChange = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        previousText = s?.toString() ?: ""
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isInternalChange) return

        val text = s?.toString() ?: ""
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFC)

        if (text != normalized) {
            isInternalChange = true
            s?.replace(0, s.length, normalized)
            isInternalChange = false
            return
        }

        if (normalized.isNotEmpty() && !normalized.matches(Regex("^[가-힣ㄱ-ㅎㅏ-ㅣ]$"))) {
            isInternalChange = true
            s?.replace(0, s.length, previousText)
            isInternalChange = false
            return
        }

        onTextChanged(index, normalized)
    }
}

class SimpleHanjaTextWatcher(
    private val index: Int,
    private val onTextChanged: (Int, String) -> Unit
) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        val text = s?.toString() ?: ""
        onTextChanged(index, text)
    }
}