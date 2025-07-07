// ui/history/HistoryViewModelFactory.kt
package com.ssc.namespring.ui.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssc.namespring.model.data.repository.TaskRepository

class HistoryViewModelFactory(
    private val context: Context,
    private val profileId: String?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(
                context,
                TaskRepository.getInstance(context),
                profileId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}