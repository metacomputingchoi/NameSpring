// model/data/repository/task/helpers/TaskSynchronizationHelper.kt
package com.ssc.namespring.model.data.repository.task.helpers

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class TaskSynchronizationHelper {
    private val dataLock = Mutex()

    suspend fun <T> withLock(action: suspend () -> T): T {
        return dataLock.withLock {
            action()
        }
    }
}