// model/presentation/adapter/SelectionModeManager.kt
package com.ssc.namespring.model.presentation.adapter

class SelectionModeManager {
    private var isSelectionMode = false
    private var selectedIds = setOf<String>()

    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
    }

    fun setSelectedIds(ids: Set<String>) {
        selectedIds = ids
    }

    fun isSelectionMode(): Boolean = isSelectionMode

    fun isSelected(id: String): Boolean = selectedIds.contains(id)

    fun getSelectedIds(): Set<String> = selectedIds
}