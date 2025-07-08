// ui/compare/filter/DateFilterHandler.kt
package com.ssc.namespring.ui.compare.filter

import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.filter.FilterConfig
import com.ssc.namespring.ui.compare.FilterType  // 변경됨
import java.text.SimpleDateFormat
import java.util.*

class DateFilterHandler(private val context: Context) : IFilterHandler {
    // 나머지 코드는 동일
    private var checkBox: CheckBox? = null
    private var btnStartDate: Button? = null
    private var btnEndDate: Button? = null
    private var container: LinearLayout? = null

    private var startDate: Long? = null
    private var endDate: Long? = null
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)

    override fun setupView(view: View) {
        checkBox = view.findViewById(R.id.cbDateRange)
        btnStartDate = view.findViewById(R.id.btnStartDate)
        btnEndDate = view.findViewById(R.id.btnEndDate)
        container = view.findViewById(R.id.containerDateRange)

        setupCheckBox()
        setupDateButtons()
    }

    private fun setupCheckBox() {
        checkBox?.setOnCheckedChangeListener { _, isChecked ->
            container?.alpha = if (isChecked) 1.0f else 0.5f
            btnStartDate?.isEnabled = isChecked
            btnEndDate?.isEnabled = isChecked

            if (!isChecked) {
                reset()
            }
        }
    }

    private fun setupDateButtons() {
        btnStartDate?.setOnClickListener {
            if (isEnabled()) showDatePicker { date ->
                startDate = date
                btnStartDate?.text = dateFormat.format(Date(date))
                validateDateRange()
            }
        }

        btnEndDate?.setOnClickListener {
            if (isEnabled()) showDatePicker { date ->
                endDate = date
                btnEndDate?.text = dateFormat.format(Date(date))
                validateDateRange()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(context, { _, year, month, day ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(selectedCalendar.timeInMillis)
        }, calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun validateDateRange() {
        if (startDate != null && endDate != null && startDate!! > endDate!!) {
            Toast.makeText(context, "시작일이 종료일보다 늦을 수 없습니다", Toast.LENGTH_SHORT).show()
            endDate = null
            btnEndDate?.text = "종료일"
        }
    }

    override fun collectFilters(): List<FilterConfig> {
        return if (isEnabled() && startDate != null && endDate != null) {
            listOf(FilterConfig(FilterType.DATE_RANGE, Pair(startDate!!, endDate!!)))
        } else emptyList()
    }

    override fun reset() {
        startDate = null
        endDate = null
        btnStartDate?.text = "시작일"
        btnEndDate?.text = "종료일"
    }

    override fun isEnabled(): Boolean = checkBox?.isChecked ?: false
}