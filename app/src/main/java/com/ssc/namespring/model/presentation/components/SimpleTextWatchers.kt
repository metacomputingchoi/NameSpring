// model/presentation/components/SimpleTextWatchers.kt
package com.ssc.namespring.model.presentation.components

import android.text.Editable
import android.text.TextWatcher
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

    private var isInternalChange = false
    private var previousText = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        previousText = s?.toString() ?: ""
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isInternalChange) return

        val text = s?.toString() ?: ""

        // 한자가 아닌 문자가 포함되어 있는지 확인
        if (text.isNotEmpty() && !isValidHanja(text)) {
            isInternalChange = true
            s?.replace(0, s.length, previousText)
            isInternalChange = false
            return
        }

        onTextChanged(index, text)
    }

    private fun isValidHanja(text: String): Boolean {
        return text.all { char ->
            val code = char.code
            // CJK Unified Ideographs 범위
            code in 0x4E00..0x9FFF ||
                    code in 0x3400..0x4DBF ||
                    code in 0x20000..0x2A6DF ||
                    code in 0x2A700..0x2B73F ||
                    code in 0x2B740..0x2B81F ||
                    code in 0x2B820..0x2CEAF ||
                    code in 0xF900..0xFAFF ||
                    code in 0x2F800..0x2FA1F
        }
    }
}