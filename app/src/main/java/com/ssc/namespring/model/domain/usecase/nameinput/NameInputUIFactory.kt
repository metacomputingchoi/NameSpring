// model/domain/usecase/nameinput/NameInputUIFactory.kt
package com.ssc.namespring.model.domain.usecase.nameinput

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.ssc.namespring.R
import com.ssc.namespring.model.presentation.components.NameCharData

class NameInputUIFactory(
    private val eventHandler: NameInputEventHandler,
    private val stateManager: NameInputStateManager
) {
    fun createNameInputView(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup,
        index: Int,
        data: NameCharData?
    ): View {
        val view = inflater.inflate(R.layout.item_name_input, parent, false)

        setupViews(view, index, data ?: NameCharData(), context)

        return view
    }

    private fun setupViews(view: View, index: Int, data: NameCharData, context: Context) {
        val tvPosition = view.findViewById<TextView>(R.id.tvPosition)
        val etKorean = view.findViewById<EditText>(R.id.etKorean)
        val etHanja = view.findViewById<EditText>(R.id.etHanja)
        val btnSearchHanja = view.findViewById<Button>(R.id.btnSearchHanja)
        val btnClearChar = view.findViewById<ImageButton>(R.id.btnClearChar)

        // Set position label
        val positions = arrayOf("첫째", "둘째", "셋째", "넷째")
        tvPosition.text = positions[index]

        // Remove existing text watchers
        stateManager.removeTextWatchers(index, etKorean, etHanja)

        // Set initial values without text watchers
        etKorean.setText(data.korean)
        etHanja.setText(data.hanja)
        NameInputButtonUpdater.updateButtonText(context, btnSearchHanja, data.korean, data.hanja)

        // Create and add new text watchers
        val koreanWatcher = eventHandler.createKoreanTextWatcher(index, etHanja, btnSearchHanja, context)
        val hanjaWatcher = eventHandler.createHanjaTextWatcher(index, etKorean, btnSearchHanja, context)

        etKorean.addTextChangedListener(koreanWatcher)
        etHanja.addTextChangedListener(hanjaWatcher)

        stateManager.addTextWatchers(index, koreanWatcher, hanjaWatcher)

        // Set button listeners
        btnSearchHanja.setOnClickListener {
            eventHandler.handleHanjaSearchClick(index)
        }

        btnClearChar.setOnClickListener {
            eventHandler.handleClearClick(index, etKorean, etHanja, btnSearchHanja)
        }
    }
}