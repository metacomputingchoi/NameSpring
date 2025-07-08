// ui/compare/manager/SortingManager.kt
package com.ssc.namespring.ui.compare.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.ui.compare.SortInfo
import com.ssc.namespring.ui.compare.SortType

class SortingManager {
    private val gson = com.google.gson.Gson()
    private val _activeSorts = MutableLiveData<List<SortInfo>>(emptyList())
    val activeSorts: LiveData<List<SortInfo>> = _activeSorts

    fun toggleSort(type: SortType) {
        val currentSorts = _activeSorts.value?.toMutableList() ?: mutableListOf()
        val existingSort = currentSorts.find { it.type == type }

        if (existingSort != null) {
            val index = currentSorts.indexOf(existingSort)
            currentSorts[index] = existingSort.copy(ascending = !existingSort.ascending)
        } else {
            currentSorts.add(SortInfo(type, false))
        }

        _activeSorts.value = currentSorts
    }

    fun removeSort(type: SortType) {
        val currentSorts = _activeSorts.value?.toMutableList() ?: mutableListOf()
        currentSorts.removeAll { it.type == type }
        _activeSorts.value = currentSorts
    }

    fun applySorting(list: List<FavoriteName>): List<FavoriteName> {
        val sorts = _activeSorts.value ?: return list
        if (sorts.isEmpty()) return list

        var result = list

        sorts.reversed().forEach { sortInfo ->
            result = when (sortInfo.type) {
                SortType.NAME -> sortByName(result, sortInfo.ascending)
                SortType.SCORE -> sortByScore(result, sortInfo.ascending)
                SortType.BIRTH_DATE -> sortByBirthDate(result, sortInfo.ascending)
                SortType.ADDED_DATE -> sortByAddedDate(result, sortInfo.ascending)
            }
        }

        return result
    }

    private fun sortByName(list: List<FavoriteName>, ascending: Boolean): List<FavoriteName> {
        return if (ascending) {
            list.sortedBy { it.fullNameKorean }
        } else {
            list.sortedByDescending { it.fullNameKorean }
        }
    }

    private fun sortByScore(list: List<FavoriteName>, ascending: Boolean): List<FavoriteName> {
        return list.sortedBy { favorite ->
            val score = getScore(favorite)
            if (ascending) score else -score
        }
    }

    private fun getScore(favorite: FavoriteName): Int {
        return try {
            val generatedName = gson.fromJson(
                favorite.jsonData,
                com.ssc.namingengine.data.GeneratedName::class.java
            )
            generatedName.analysisInfo?.totalScore ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun sortByBirthDate(list: List<FavoriteName>, ascending: Boolean): List<FavoriteName> {
        return if (ascending) {
            list.sortedBy { it.birthDateTime }
        } else {
            list.sortedByDescending { it.birthDateTime }
        }
    }

    private fun sortByAddedDate(list: List<FavoriteName>, ascending: Boolean): List<FavoriteName> {
        return if (ascending) {
            list.sortedBy { it.addedAt }
        } else {
            list.sortedByDescending { it.addedAt }
        }
    }
}