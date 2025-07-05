// ui/profilelist/ProfileListObserver.kt
package com.ssc.namespring.ui.profilelist

import androidx.lifecycle.LifecycleOwner
import com.ssc.namespring.model.presentation.adapter.ProfileAdapter
import com.ssc.namespring.model.domain.usecase.ProfileListManager

class ProfileListObserver(
    private val lifecycleOwner: LifecycleOwner,
    private val listManager: ProfileListManager,
    private val components: ProfileListComponents,
    private val adapter: ProfileAdapter
) {
    fun startObserving() {
        listManager.uiState.observe(lifecycleOwner) { state ->
            components.uiUpdater.updateUI(state, adapter)
        }
    }
}