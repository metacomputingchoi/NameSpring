// view/EvaluationInputView.kt
package com.ssc.namespring.view

import com.ssc.namespring.model.data.Profile

interface EvaluationInputView {
    fun showDynamicEvaluationInput()
    fun showProfileSelector(profiles: List<Profile>)
    fun getEvaluationName(): String
    fun getEvaluationHanja(): String
    fun getSelectedProfile(): Profile?
    fun validateInput(): Boolean
    fun showError(message: String)
    fun clearInputs()
}