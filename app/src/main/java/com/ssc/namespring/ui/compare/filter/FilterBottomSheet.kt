// ui/compare/filter/FilterBottomSheet.kt
package com.ssc.namespring.ui.compare.filter

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.ssc.namespring.R
import com.ssc.namespring.ui.compare.FilterType  // 변경됨
import com.ssc.namespring.ui.compare.filter.FilterCoordinator
import com.ssc.namespring.ui.compare.filter.FilterScoreCalculator

class FilterBottomSheet(
    context: Context,
    private val onFiltersApplied: (List<Pair<FilterType, Any>>) -> Unit
) : BottomSheetDialog(context) {
    // 나머지 코드는 동일
    private val filterCoordinator = FilterCoordinator(context)
    private val scoreCalculator = FilterScoreCalculator(context)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_filter, null)
        setContentView(view)

        initializeFilters()
        setupViews(view)
        setupButtons()

        behavior.peekHeight = context.resources.displayMetrics.heightPixels * 3 / 4
    }

    private fun initializeFilters() {
        val (minScore, maxScore) = scoreCalculator.calculateScoreRange()
        filterCoordinator.initialize(minScore, maxScore)
    }

    private fun setupViews(view: android.view.View) {
        filterCoordinator.setupViews(view)
    }

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.btnApply)?.setOnClickListener {
            applyFilters()
        }

        findViewById<MaterialButton>(R.id.btnReset)?.setOnClickListener {
            resetFilters()
        }
    }

    private fun applyFilters() {
        val activeFilters = filterCoordinator.collectActiveFilters()

        if (activeFilters.isNotEmpty()) {
            onFiltersApplied(activeFilters)
            dismiss()
        } else {
            Toast.makeText(context, "적용할 필터를 선택해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetFilters() {
        filterCoordinator.resetAll()
        Toast.makeText(context, "필터가 초기화되었습니다", Toast.LENGTH_SHORT).show()
    }
}