// ui/compare/FilterOptionsDialog.kt
package com.ssc.namespring.ui.compare

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.android.material.button.MaterialButton
import com.ssc.namespring.R
import com.ssc.namespring.ui.compare.filter.FilterStrategyFactory
import com.ssc.namespring.ui.compare.filter.strategies.FilterStrategy

class FilterOptionsDialog(
    context: Context,
    private val onFilterSelected: (FilterType, Any) -> Unit
) : Dialog(context) {

    private lateinit var spinnerFilterType: Spinner
    private lateinit var containerFilterOptions: LinearLayout
    private lateinit var filterStrategyFactory: FilterStrategyFactory
    private var currentStrategy: FilterStrategy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_filter_options)

        filterStrategyFactory = FilterStrategyFactory(context, layoutInflater)
        setupViews()
    }

    private fun setupViews() {
        spinnerFilterType = findViewById(R.id.spinnerFilterType)
        containerFilterOptions = findViewById(R.id.containerFilterOptions)

        spinnerFilterType.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            filterStrategyFactory.getFilterTypes()
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

        currentStrategy = filterStrategyFactory.createStrategy(position)
        currentStrategy?.let { strategy ->
            val view = strategy.createView()
            containerFilterOptions.addView(view)
        }
    }

    private fun applyFilter() {
        currentStrategy?.let { strategy ->
            val view = containerFilterOptions.getChildAt(0)
            if (view != null && strategy.validateInput(view)) {
                val filterData = strategy.extractFilterData(view)
                filterData?.let {
                    onFilterSelected(strategy.getFilterType(), it)
                    dismiss()
                }
            }
        }
    }
}