// model/domain/usecase/profileform/ProfileFormDateTimeManager.kt
package com.ssc.namespring.model.domain.usecase.profileform

import android.annotation.SuppressLint
import java.util.Calendar

class ProfileFormDateTimeManager {
    private val calendar: Calendar = Calendar.getInstance()

    fun setDateTime(date: Calendar) {
        calendar.time = date.time
    }

    fun updateDate(newDate: Calendar) {
        calendar.set(Calendar.YEAR, newDate.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, newDate.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, newDate.get(Calendar.DAY_OF_MONTH))
    }

    fun updateTime(newTime: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, newTime.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, newTime.get(Calendar.MINUTE))
    }

    @SuppressLint("DefaultLocale")
    fun getFormattedDate(): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%d년 %d월 %d일", year, month, day)
    }

    @SuppressLint("DefaultLocale")
    fun getFormattedTime(): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format("%02d시 %02d분", hour, minute)
    }

    fun getCalendar(): Calendar = calendar.clone() as Calendar

    fun reset() {
        calendar.time = Calendar.getInstance().time
    }
}