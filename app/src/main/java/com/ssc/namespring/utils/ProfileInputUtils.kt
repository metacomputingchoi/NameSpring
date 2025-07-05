// utils/ProfileInputUtils.kt
package com.ssc.namespring.utils

import android.view.inputmethod.EditorInfo
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

object ProfileInputUtils {

    fun setupProfileNameInput(
        etProfileName: TextInputEditText,
        profileNameLayout: TextInputLayout
    ) {
        etProfileName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etProfileName.clearFocus()
                profileNameLayout.isEndIconVisible = true
                true
            } else {
                false
            }
        }
    }
}