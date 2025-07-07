// ui/compare/filter/FilterBottomSheet.kt
package com.ssc.namespring.ui.compare.filter

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.RangeSlider
import com.google.android.material.textfield.TextInputEditText
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namespring.ui.compare.CompareViewModel.FilterType
import java.text.SimpleDateFormat
import java.util.*

class FilterBottomSheet(
    context: Context,
    private val onFiltersApplied: (List<Pair<FilterType, Any>>) -> Unit
) : BottomSheetDialog(context) {

    private val favoriteRepository = FavoriteNameRepository.getInstance(context)
    private val gson = com.google.gson.Gson()
    private val appliedFilters = mutableListOf<Pair<FilterType, Any>>()
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)

    // 날짜 필터 관련 변수
    private var startDate: Long? = null
    private var endDate: Long? = null
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button

    // 체크박스들
    private lateinit var cbScoreRange: CheckBox
    private lateinit var cbSurname: CheckBox
    private lateinit var cbElement: CheckBox
    private lateinit var cbHanja: CheckBox
    private lateinit var cbMeaning: CheckBox
    private lateinit var cbDateRange: CheckBox

    // 동적 점수 범위
    private var minScore = 0
    private var maxScore = 100

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_filter, null)
        setContentView(view)
        setupViews(view)
        calculateScoreRange()

        // BottomSheet 높이 설정
        behavior.peekHeight = context.resources.displayMetrics.heightPixels * 3 / 4
    }

    private fun calculateScoreRange() {
        val favorites = favoriteRepository.getFavoritesList()
        if (favorites.isEmpty()) return

        var calculatedMin = Int.MAX_VALUE
        var calculatedMax = Int.MIN_VALUE

        favorites.forEach { favorite ->
            try {
                val generatedName = gson.fromJson(
                    favorite.jsonData,
                    com.ssc.namingengine.data.GeneratedName::class.java
                )
                val score = generatedName.analysisInfo?.totalScore ?: 0
                calculatedMin = minOf(calculatedMin, score)
                calculatedMax = maxOf(calculatedMax, score)
            } catch (e: Exception) {
                // 에러 무시
            }
        }

        // 유효한 범위가 계산되었으면 적용
        if (calculatedMin != Int.MAX_VALUE && calculatedMax != Int.MIN_VALUE) {
            minScore = calculatedMin
            maxScore = calculatedMax
        }
    }

    private fun setupViews(view: View) {
        // 체크박스 찾기
        cbScoreRange = view.findViewById(R.id.cbScoreRange)
        cbSurname = view.findViewById(R.id.cbSurname)
        cbElement = view.findViewById(R.id.cbElement)
        cbHanja = view.findViewById(R.id.cbHanja)
        cbMeaning = view.findViewById(R.id.cbMeaning)
        cbDateRange = view.findViewById(R.id.cbDateRange)

        // 점수 범위 필터
        setupScoreFilter(view)

        // 날짜 범위 필터
        setupDateFilter(view)

        // 성씨 필터
        setupSurnameFilter(view)

        // 한자 필터
        setupHanjaFilter(view)

        // 오행 필터
        setupElementFilter(view)

        // 의미 필터
        setupMeaningFilter(view)

        // 적용 버튼
        view.findViewById<MaterialButton>(R.id.btnApply).setOnClickListener {
            collectAndApplyFilters()
        }

        // 초기화 버튼
        view.findViewById<MaterialButton>(R.id.btnReset).setOnClickListener {
            appliedFilters.clear()
            startDate = null
            endDate = null
            resetAllFilters(view)
            Toast.makeText(context, "필터가 초기화되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun collectAndApplyFilters() {
        appliedFilters.clear()

        // 체크된 필터만 수집
        if (cbScoreRange.isChecked) {
            val rangeSlider = findViewById<RangeSlider>(R.id.sliderScore)
            val values = rangeSlider?.values
            if (values != null) {
                val min = values[0].toInt()
                val max = values[1].toInt()
                // 전체 범위가 아닌 경우만 추가
                if (min != minScore || max != maxScore) {
                    appliedFilters.add(FilterType.SCORE_RANGE to IntRange(min, max))
                }
            }
        }

        if (cbDateRange.isChecked && startDate != null && endDate != null) {
            appliedFilters.add(FilterType.DATE_RANGE to Pair(startDate!!, endDate!!))
        }

        // 기존 로직으로 수집된 필터들은 체크 여부 확인
        val tempFilters = appliedFilters.toList()
        appliedFilters.clear()

        tempFilters.forEach { (type, value) ->
            when (type) {
                FilterType.SURNAME -> if (cbSurname.isChecked) appliedFilters.add(type to value)
                FilterType.ELEMENT -> if (cbElement.isChecked) appliedFilters.add(type to value)
                FilterType.HANJA_CONTAINS -> if (cbHanja.isChecked) appliedFilters.add(type to value)
                FilterType.MEANING -> if (cbMeaning.isChecked) appliedFilters.add(type to value)
                else -> appliedFilters.add(type to value)
            }
        }

        if (appliedFilters.isNotEmpty()) {
            onFiltersApplied(appliedFilters)
            dismiss()
        } else {
            Toast.makeText(context, "적용할 필터를 선택해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupScoreFilter(view: View) {
        val rangeSlider = view.findViewById<RangeSlider>(R.id.sliderScore)
        val tvScoreRange = view.findViewById<TextView>(R.id.tvScoreRange)
        val containerScoreRange = view.findViewById<LinearLayout>(R.id.containerScoreRange)

        // 동적 범위 설정
        rangeSlider.valueFrom = minScore.toFloat()
        rangeSlider.valueTo = maxScore.toFloat()
        rangeSlider.values = listOf(minScore.toFloat(), maxScore.toFloat())
        tvScoreRange.text = "$minScore ~ ${maxScore}점"

        // 체크박스 리스너
        cbScoreRange.setOnCheckedChangeListener { _, isChecked ->
            containerScoreRange.alpha = if (isChecked) 1.0f else 0.5f
            rangeSlider.isEnabled = isChecked

            if (!isChecked) {
                // 체크 해제 시 전체 범위로 리셋
                rangeSlider.values = listOf(minScore.toFloat(), maxScore.toFloat())
                tvScoreRange.text = "$minScore ~ ${maxScore}점"
            }
        }

        rangeSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            val min = values[0].toInt()
            val max = values[1].toInt()
            tvScoreRange.text = "$min ~ ${max}점"
        }
    }

    private fun setupDateFilter(view: View) {
        btnStartDate = view.findViewById(R.id.btnStartDate)
        btnEndDate = view.findViewById(R.id.btnEndDate)
        val containerDateRange = view.findViewById<LinearLayout>(R.id.containerDateRange)

        cbDateRange.setOnCheckedChangeListener { _, isChecked ->
            containerDateRange.alpha = if (isChecked) 1.0f else 0.5f
            btnStartDate.isEnabled = isChecked
            btnEndDate.isEnabled = isChecked

            if (!isChecked) {
                startDate = null
                endDate = null
                btnStartDate.text = "시작일"
                btnEndDate.text = "종료일"
            }
        }

        btnStartDate.setOnClickListener {
            if (cbDateRange.isChecked) {
                showDatePicker { date ->
                    startDate = date
                    btnStartDate.text = dateFormat.format(Date(date))
                    checkDateRangeValid()
                }
            }
        }

        btnEndDate.setOnClickListener {
            if (cbDateRange.isChecked) {
                showDatePicker { date ->
                    endDate = date
                    btnEndDate.text = dateFormat.format(Date(date))
                    checkDateRangeValid()
                }
            }
        }
    }

    private fun checkDateRangeValid() {
        if (startDate != null && endDate != null) {
            if (startDate!! > endDate!!) {
                Toast.makeText(context, "시작일이 종료일보다 늦을 수 없습니다", Toast.LENGTH_SHORT).show()
                endDate = null
                btnEndDate.text = "종료일"
            }
        }
    }

    private fun setupSurnameFilter(view: View) {
        val chipGroupSurname = view.findViewById<ChipGroup>(R.id.chipGroupSurname)

        cbSurname.setOnCheckedChangeListener { _, isChecked ->
            chipGroupSurname.alpha = if (isChecked) 1.0f else 0.5f
            for (i in 0 until chipGroupSurname.childCount) {
                chipGroupSurname.getChildAt(i).isEnabled = isChecked
            }

            if (!isChecked) {
                chipGroupSurname.clearCheck()
            }
        }

        val commonSurnames = listOf(
            "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
            "한", "오", "서", "신", "권", "황", "안", "송", "류", "전"
        )

        commonSurnames.forEach { surname ->
            val chip = Chip(context).apply {
                text = surname
                isCheckable = true
                isEnabled = false // 초기에는 비활성화

                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
                )
                val colors = intArrayOf(
                    context.getColor(R.color.primary_blue),
                    context.getColor(R.color.chip_background)
                )
                chipBackgroundColor = android.content.res.ColorStateList(states, colors)

                setTextAppearance(R.style.ChipTextAppearance)

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        appliedFilters.add(FilterType.SURNAME to surname)
                    } else {
                        appliedFilters.removeAll { it.first == FilterType.SURNAME && it.second == surname }
                    }
                }
            }
            chipGroupSurname.addView(chip)
        }
    }

    private fun setupElementFilter(view: View) {
        val chipGroupElement = view.findViewById<ChipGroup>(R.id.chipGroupElement)

        cbElement.setOnCheckedChangeListener { _, isChecked ->
            chipGroupElement.alpha = if (isChecked) 1.0f else 0.5f
            for (i in 0 until chipGroupElement.childCount) {
                chipGroupElement.getChildAt(i).isEnabled = isChecked
            }

            if (!isChecked) {
                chipGroupElement.clearCheck()
            }
        }

        val elements = mapOf(
            "목" to "木", "화" to "火", "토" to "土", "금" to "金", "수" to "水"
        )

        elements.forEach { (korean, chinese) ->
            val chip = Chip(context).apply {
                text = "$korean($chinese)"
                isCheckable = true
                isEnabled = false // 초기에는 비활성화

                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
                )
                val colors = intArrayOf(
                    context.getColor(R.color.primary_blue),
                    context.getColor(R.color.chip_background)
                )
                chipBackgroundColor = android.content.res.ColorStateList(states, colors)

                setTextAppearance(R.style.ChipTextAppearance)

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        appliedFilters.add(FilterType.ELEMENT to chinese)
                    } else {
                        appliedFilters.removeAll { it.first == FilterType.ELEMENT && it.second == chinese }
                    }
                }
            }
            chipGroupElement.addView(chip)
        }
    }

    private fun setupHanjaFilter(view: View) {
        val etHanja = view.findViewById<TextInputEditText>(R.id.etHanja)
        val btnAddHanja = view.findViewById<ImageButton>(R.id.btnAddHanja)
        val chipGroupHanja = view.findViewById<ChipGroup>(R.id.chipGroupAddedHanja)
        val containerHanja = view.findViewById<LinearLayout>(R.id.containerHanja)

        cbHanja.setOnCheckedChangeListener { _, isChecked ->
            containerHanja.alpha = if (isChecked) 1.0f else 0.5f
            etHanja.isEnabled = isChecked
            btnAddHanja.isEnabled = isChecked

            if (!isChecked) {
                chipGroupHanja.removeAllViews()
                etHanja.text?.clear()
            }
        }

        btnAddHanja.setOnClickListener {
            if (cbHanja.isChecked) {
                val hanja = etHanja.text?.toString()?.trim()
                if (!hanja.isNullOrBlank()) {
                    val isDuplicate = appliedFilters.any {
                        it.first == FilterType.HANJA_CONTAINS && it.second == hanja
                    }

                    if (!isDuplicate) {
                        appliedFilters.add(FilterType.HANJA_CONTAINS to hanja)

                        val chip = Chip(context).apply {
                            text = hanja
                            isCloseIconVisible = true
                            setChipBackgroundColorResource(R.color.chip_background)

                            setOnCloseIconClickListener {
                                appliedFilters.removeAll { it.first == FilterType.HANJA_CONTAINS && it.second == hanja }
                                chipGroupHanja.removeView(this)
                            }
                        }
                        chipGroupHanja.addView(chip)
                        etHanja.text?.clear()
                    } else {
                        Toast.makeText(context, "이미 추가된 한자입니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupMeaningFilter(view: View) {
        val etMeaning = view.findViewById<TextInputEditText>(R.id.etMeaning)
        val btnAddMeaning = view.findViewById<ImageButton>(R.id.btnAddMeaning)
        val chipGroupMeaning = view.findViewById<ChipGroup>(R.id.chipGroupAddedMeaning)
        val containerMeaning = view.findViewById<LinearLayout>(R.id.containerMeaning)

        cbMeaning.setOnCheckedChangeListener { _, isChecked ->
            containerMeaning.alpha = if (isChecked) 1.0f else 0.5f
            etMeaning.isEnabled = isChecked
            btnAddMeaning.isEnabled = isChecked

            if (!isChecked) {
                chipGroupMeaning.removeAllViews()
                etMeaning.text?.clear()
            }
        }

        btnAddMeaning.setOnClickListener {
            if (cbMeaning.isChecked) {
                val meaning = etMeaning.text?.toString()?.trim()
                if (!meaning.isNullOrBlank()) {
                    val isDuplicate = appliedFilters.any {
                        it.first == FilterType.MEANING && it.second == meaning
                    }

                    if (!isDuplicate) {
                        appliedFilters.add(FilterType.MEANING to meaning)

                        val chip = Chip(context).apply {
                            text = meaning
                            isCloseIconVisible = true
                            setChipBackgroundColorResource(R.color.chip_background)

                            setOnCloseIconClickListener {
                                appliedFilters.removeAll { it.first == FilterType.MEANING && it.second == meaning }
                                chipGroupMeaning.removeView(this)
                            }
                        }
                        chipGroupMeaning.addView(chip)
                        etMeaning.text?.clear()
                    } else {
                        Toast.makeText(context, "이미 추가된 키워드입니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(selectedCalendar.timeInMillis)
        }, year, month, day).show()
    }

    private fun resetAllFilters(view: View) {
        // 모든 체크박스 해제
        cbScoreRange.isChecked = false
        cbSurname.isChecked = false
        cbElement.isChecked = false
        cbHanja.isChecked = false
        cbMeaning.isChecked = false
        cbDateRange.isChecked = false

        // 점수 슬라이더 초기화
        view.findViewById<RangeSlider>(R.id.sliderScore).values = listOf(minScore.toFloat(), maxScore.toFloat())
        view.findViewById<TextView>(R.id.tvScoreRange).text = "$minScore ~ ${maxScore}점"

        // 날짜 버튼 초기화
        btnStartDate.text = "시작일"
        btnEndDate.text = "종료일"

        // ChipGroup 초기화
        view.findViewById<ChipGroup>(R.id.chipGroupSurname).clearCheck()
        view.findViewById<ChipGroup>(R.id.chipGroupElement).clearCheck()
        view.findViewById<ChipGroup>(R.id.chipGroupAddedHanja).removeAllViews()
        view.findViewById<ChipGroup>(R.id.chipGroupAddedMeaning).removeAllViews()

        // EditText 초기화
        view.findViewById<TextInputEditText>(R.id.etHanja).text?.clear()
        view.findViewById<TextInputEditText>(R.id.etMeaning).text?.clear()
    }
}