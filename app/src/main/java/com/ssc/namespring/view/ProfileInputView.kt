// view/ProfileInputView.kt
package com.ssc.namespring.view

import java.time.LocalDateTime

interface ProfileInputView {
    fun showDynamicNameInput(maxSurname: Int, maxGivenName: Int)
    fun showDateTimePicker(onDateTimeSelected: (LocalDateTime) -> Unit)
    fun showYajasiOption()
    fun validateInput(): Boolean
    fun getProfileName(): String
    fun getSurname(): String
    fun getSurnameHanja(): String
    fun getGivenName(): String
    fun getGivenNameHanja(): String
    fun getBirthDateTime(): LocalDateTime
    fun getUseYajasi(): Boolean
    fun showError(message: String)
    fun showSuccess(message: String)
    fun clearInputs()
}