// ui/compare/FilterOptionsDialog.kt
package com.ssc.namespring.ui.compare

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.R
import com.ssc.namespring.ui.compare.CompareViewModel.FilterType
import java.text.SimpleDateFormat
import java.util.*

class FilterOptionsDialog(
    context: Context,
    private val onFilterSelected: (FilterType, Any) -> Unit
) : Dialog(context) {

    private lateinit var spinnerFilterType: Spinner
    private lateinit var containerFilterOptions: LinearLayout
    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_filter_options)

        setupViews()
    }

    private fun setupViews() {
        spinnerFilterType = findViewById(R.id.spinnerFilterType)
        containerFilterOptions = findViewById(R.id.containerFilterOptions)

        val filterTypes = arrayOf(
            "점수 범위",
            "생년월일 범위",
            "성씨",
            "한자 포함",
            "오행"
        )

        spinnerFilterType.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            filterTypes
        )

        spinnerFilterType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                showFilterOptions(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        findViewById<MaterialButton>(R.id.btnApply).setOnClickListener {
            applyFilter()
        }

        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }
    }

    private fun showFilterOptions(position: Int) {
        containerFilterOptions.removeAllViews()

        when (position) {
            0 -> showScoreRangeOptions()
            1 -> showDateRangeOptions()
            2 -> showSurnameOptions()
            3 -> showHanjaOptions()
            4 -> showElementOptions()
        }
    }

    private fun showScoreRangeOptions() {
        val view = layoutInflater.inflate(R.layout.filter_score_range, containerFilterOptions, false)
        containerFilterOptions.addView(view)
    }

    private fun showDateRangeOptions() {
        val view = layoutInflater.inflate(R.layout.filter_date_range, containerFilterOptions, false)

        val btnStartDate = view.findViewById<Button>(R.id.btnStartDate)
        val btnEndDate = view.findViewById<Button>(R.id.btnEndDate)

        var startDate: Long? = null
        var endDate: Long? = null

        btnStartDate.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                btnStartDate.text = dateFormat.format(Date(date))
            }
        }

        btnEndDate.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                btnEndDate.text = dateFormat.format(Date(date))
            }
        }

        containerFilterOptions.addView(view)
    }

    private fun showSurnameOptions() {
        val view = layoutInflater.inflate(R.layout.filter_text_input, containerFilterOptions, false)
        view.findViewById<TextInputLayout>(R.id.textInputLayout).hint = "성씨 입력 (예: 김, 金)"
        containerFilterOptions.addView(view)
    }

    private fun showHanjaOptions() {
        val view = layoutInflater.inflate(R.layout.filter_text_input, containerFilterOptions, false)
        view.findViewById<TextInputLayout>(R.id.textInputLayout).hint = "한자 입력"
        containerFilterOptions.addView(view)
    }

    private fun showElementOptions() {
        val view = layoutInflater.inflate(R.layout.filter_element, containerFilterOptions, false)
        containerFilterOptions.addView(view)
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day, 0, 0, 0)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun applyFilter() {
        val selectedPosition = spinnerFilterType.selectedItemPosition

        when (selectedPosition) {
            0 -> { // 점수 범위
                val minScore = containerFilterOptions.findViewById<EditText>(R.id.etMinScore)?.text?.toString()?.toIntOrNull() ?: 0
                val maxScore = containerFilterOptions.findViewById<EditText>(R.id.etMaxScore)?.text?.toString()?.toIntOrNull() ?: 100
                onFilterSelected(FilterType.SCORE_RANGE, Pair(minScore, maxScore))
            }
            1 -> { // 날짜 범위
                // 구현 생략 (위의 showDateRangeOptions에서 처리)
            }
            2 -> { // 성씨
                val surname = containerFilterOptions.findViewById<TextInputEditText>(R.id.etInput)?.text?.toString() ?: ""
                if (surname.isNotEmpty()) {
                    onFilterSelected(FilterType.SURNAME, surname)
                }
            }
            3 -> { // 한자
                val hanja = containerFilterOptions.findViewById<TextInputEditText>(R.id.etInput)?.text?.toString() ?: ""
                if (hanja.isNotEmpty()) {
                    onFilterSelected(FilterType.HANJA_CONTAINS, hanja)
                }
            }
            4 -> { // 오행
                val radioGroup = containerFilterOptions.findViewById<RadioGroup>(R.id.radioGroupElement)
                val selectedId = radioGroup?.checkedRadioButtonId ?: -1
                if (selectedId != -1) {
                    val element = when (selectedId) {
                        R.id.radioWood -> "木"
                        R.id.radioFire -> "火"
                        R.id.radioEarth -> "土"
                        R.id.radioMetal -> "金"
                        R.id.radioWater -> "水"
                        else -> ""
                    }
                    if (element.isNotEmpty()) {
                        onFilterSelected(FilterType.ELEMENT, element)
                    }
                }
            }
        }

        dismiss()
    }
}