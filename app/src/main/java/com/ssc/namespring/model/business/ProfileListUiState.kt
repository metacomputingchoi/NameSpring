// model/business/ProfileListUiState.kt
package com.ssc.namespring.model.business

import com.ssc.namespring.model.data.Profile

data class ProfileListUiState(
    val profiles: List<Profile> = emptyList(),
    val isLoading: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet()
)