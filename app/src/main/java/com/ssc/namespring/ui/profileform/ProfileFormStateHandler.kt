// ui/profileform/ProfileFormStateHandler.kt
package com.ssc.namespring.ui.profileform

import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.LifecycleOwner
import com.ssc.namespring.model.domain.usecase.ProfileFormManager

class ProfileFormStateHandler(
    private val progressBar: ProgressBar,
    private val uiComponents: ProfileFormUIComponents,
    private val formManager: ProfileFormManager,
    private val uiUpdater: ProfileFormUIUpdater
) {
    private var isInitialized = false

    fun setInitialized(value: Boolean) {
        isInitialized = value
    }

    fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        uiComponents.scrollView?.isEnabled = !show
        uiComponents.btnSave.isEnabled = !show && isInitialized
        uiComponents.btnReset.isEnabled = !show
    }

    fun observeFormState(owner: LifecycleOwner) {
        formManager.uiState.observe(owner) { state ->
            uiUpdater.updateUI(state)
        }
    }

    fun syncUiStateWithInput(position: Int, korean: String, hanja: String) {
        formManager.setHanjaInfo(position, korean, hanja)
    }
}
