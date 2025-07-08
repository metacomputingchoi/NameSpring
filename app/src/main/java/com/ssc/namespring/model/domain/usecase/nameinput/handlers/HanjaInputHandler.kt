// model/domain/usecase/nameinput/handlers/HanjaInputHandler.kt
package com.ssc.namespring.model.domain.usecase.nameinput.handlers

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import com.ssc.namespring.model.domain.usecase.nameinput.NameInputButtonUpdater

class HanjaInputHandler(
    private val context: Context,
    private val nameDataManager: INameDataManager,
    private val etKorean: EditText,
    private val btnSearchHanja: Button,
    private val index: Int
) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        val hanja = s?.toString() ?: ""
        val korean = etKorean.text.toString()
        nameDataManager.setCharData(index, korean, hanja)

        NameInputButtonUpdater.updateButtonText(
            context,
            btnSearchHanja,
            korean,
            hanja,
            index
        )
    }
}
