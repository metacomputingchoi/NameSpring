// ui/compare/filter/strategies/DateRangeFilterStrategy.kt
package com.ssc.namespring.ui.compare.filter.strategies

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.ssc.namespring.R
import com.ssc.namespring.ui.compare.FilterType  // 변경됨
import java.text.SimpleDateFormat
import java.util.*

class DateRangeFilterStrategy(
    private val context: Context,
    private val inflater: LayoutInflater
) : FilterStrategy {

    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)
    private var startDate: Long? = null
    private var endDate: Long? = null

    override fun createView(): View {
        val view = inflater.inflate(R.layout.filter_date_range, null, false)
        setupDateButtons(view)
        return view
    }

    override fun getFilterType(): FilterType = FilterType.DATE_RANGE

    override fun extractFilterData(view: View): Pair<Long?, Long?> {
        return Pair(startDate, endDate)
    }

    override fun validateInput(view: View): Boolean = true

    private fun setupDateButtons(view: View) {
        val btnStartDate = view.findViewById<Button>(R.id.btnStartDate)
        val btnEndDate = view.findViewById<Button>(R.id.btnEndDate)

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
}