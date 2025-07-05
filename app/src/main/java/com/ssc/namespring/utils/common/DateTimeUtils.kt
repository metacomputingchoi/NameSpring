// utils/common/DateTimeUtils.kt
package com.ssc.namespring.utils.common

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.util.Calendar

object DateTimeUtils {

    fun showDatePicker(
        context: Context,
        currentDate: Calendar,
        onDateSet: (Calendar) -> Unit
    ) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate.set(Calendar.YEAR, year)
                newDate.set(Calendar.MONTH, month)
                newDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                onDateSet(newDate)
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker(
        context: Context,
        currentTime: Calendar,
        onTimeSet: (Calendar) -> Unit
    ) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newTime = Calendar.getInstance()
                newTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                newTime.set(Calendar.MINUTE, minute)
                onTimeSet(newTime)
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            false
        ).show()
    }
}