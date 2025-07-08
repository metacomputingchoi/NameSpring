// model/data/repository/task/helpers/TaskInitializationHelper.kt
package com.ssc.namespring.model.data.repository.task.helpers

import com.ssc.namespring.model.data.repository.task.managers.TaskLiveDataManager
import com.ssc.namespring.model.data.repository.task.managers.TaskPersistenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class TaskInitializationHelper(
    private val scope: CoroutineScope,
    private val persistenceManager: TaskPersistenceManager,
    private val liveDataManager: TaskLiveDataManager
) {
    fun loadAllHistories() {
        scope.launch {
            val histories = persistenceManager.loadAllHistories()
            liveDataManager.updateMap(histories)
        }
    }
}