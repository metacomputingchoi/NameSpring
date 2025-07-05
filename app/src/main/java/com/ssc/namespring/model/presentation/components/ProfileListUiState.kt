// model/presentation/components/ProfileListUiState.kt
package com.ssc.namespring.model.presentation.components

import com.ssc.namespring.model.domain.entity.Profile

data class ProfileListUiState(
    val profiles: List<Profile> = emptyList(),
    val isLoading: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet()
)