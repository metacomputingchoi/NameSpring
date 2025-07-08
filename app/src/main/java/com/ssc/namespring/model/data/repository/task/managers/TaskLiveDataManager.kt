// model/data/repository/task/managers/TaskLiveDataManager.kt
package com.ssc.namespring.model.data.repository.task.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssc.namespring.model.domain.entity.TaskHistory

internal class TaskLiveDataManager {
    private val _taskHistoryMap = MutableLiveData<Map<String, TaskHistory>>()
    val taskHistoryMap: LiveData<Map<String, TaskHistory>> = _taskHistoryMap

    fun getCurrentMap(): Map<String, TaskHistory> = _taskHistoryMap.value ?: emptyMap()

    fun updateMap(newMap: Map<String, TaskHistory>) {
        _taskHistoryMap.postValue(newMap)
    }

    fun updateHistory(profileId: String, history: TaskHistory) {
        val newMap = getCurrentMap().toMutableMap()
        newMap[profileId] = history
        _taskHistoryMap.postValue(newMap)
    }

    fun removeHistory(profileId: String) {
        val newMap = getCurrentMap().toMutableMap()
        newMap.remove(profileId)
        _taskHistoryMap.postValue(newMap)
    }
}