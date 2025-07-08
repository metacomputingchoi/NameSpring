// ui/compare/filter/strategies/TextInputFilterStrategy.kt
package com.ssc.namespring.ui.compare.filter.strategies

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.R
import com.ssc.namespring.ui.compare.FilterType

abstract class TextInputFilterStrategy(
    protected val context: Context,
    protected val inflater: LayoutInflater
) : FilterStrategy {

    abstract fun getHintText(): String

    override fun createView(): View {
        val view = inflater.inflate(R.layout.filter_text_input, null, false)
        view.findViewById<TextInputLayout>(R.id.textInputLayout).hint = getHintText()
        return view
    }

    override fun extractFilterData(view: View): String {
        return view.findViewById<TextInputEditText>(R.id.etInput)
            ?.text?.toString() ?: ""
    }

    override fun validateInput(view: View): Boolean {
        return extractFilterData(view).isNotEmpty()
    }
}

class SurnameFilterStrategy(
    context: Context,
    inflater: LayoutInflater
) : TextInputFilterStrategy(context, inflater) {

    override fun getHintText(): String = "성씨 입력 (예: 김, 金)"
    override fun getFilterType(): FilterType = FilterType.SURNAME
}

class HanjaFilterStrategy(
    context: Context,
    inflater: LayoutInflater
) : TextInputFilterStrategy(context, inflater) {

    override fun getHintText(): String = "한자 입력"
    override fun getFilterType(): FilterType = FilterType.HANJA_CONTAINS
}