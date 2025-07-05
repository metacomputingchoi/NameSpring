// model/domain/usecase/ProfileListSelectionManager.kt
package com.ssc.namespring.model.domain.usecase

import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile

class ProfileListSelectionManager {
    fun toggleSelection(currentSelectedIds: Set<String>, profileId: String): Set<String> {
        return try {
            if (currentSelectedIds.contains(profileId)) {
                Log.d("ProfileListManager", "Deselecting profile: $profileId")
                currentSelectedIds - profileId
            } else {
                Log.d("ProfileListManager", "Selecting profile: $profileId")
                currentSelectedIds + profileId
            }
        } catch (e: Exception) {
            Log.e("ProfileListManager", "Error toggling selection", e)
            currentSelectedIds
        }
    }

    fun toggleSelectAll(profiles: List<Profile>, currentSelectedIds: Set<String>): Set<String> {
        val allIds = profiles.map { it.id }.toSet()
        return if (currentSelectedIds.size == allIds.size) {
            emptySet()
        } else {
            allIds
        }
    }
}