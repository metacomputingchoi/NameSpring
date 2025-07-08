// model/data/repository/task/TaskDataService.kt
package com.ssc.namespring.model.data.repository.task

import androidx.lifecycle.LiveData
import com.ssc.namespring.model.data.repository.task.helpers.*
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskDataService
import com.ssc.namespring.model.data.repository.task.interfaces.ITaskFileService
import com.ssc.namespring.model.data.repository.task.managers.TaskCRUDManager
import com.ssc.namespring.model.data.repository.task.managers.TaskLiveDataManager
import com.ssc.namespring.model.data.repository.task.managers.TaskPersistenceManager
import com.ssc.namespring.model.domain.entity.*
import kotlinx.coroutines.CoroutineScope

class TaskDataService(
    fileService: ITaskFileService,
    scope: CoroutineScope
) : ITaskDataService {

    companion object {
        private const val TAG = "TaskDataService"
    }

    private val liveDataManager = TaskLiveDataManager()
    private val persistenceManager = TaskPersistenceManager(fileService)
    private val crudManager = TaskCRUDManager()

    private val synchronizationHelper = TaskSynchronizationHelper()
    private val operationsDelegate = TaskOperationsDelegate(
        liveDataManager, 
        persistenceManager, 
        crudManager
    )
    private val initializationHelper = TaskInitializationHelper(
        scope, 
        persistenceManager, 
        liveDataManager
    )

    override val taskHistoryMap: LiveData<Map<String, TaskHistory>> = 
        liveDataManager.taskHistoryMap

    init {
        initializationHelper.loadAllHistories()
    }

    override suspend fun saveTask(task: Task): Boolean = 
        synchronizationHelper.withLock {
            operationsDelegate.saveTask(task)
        }

    override suspend fun updateTask(task: Task): Boolean = saveTask(task)

    override suspend fun deleteTask(taskId: String): Boolean = 
        synchronizationHelper.withLock {
            operationsDelegate.deleteTask(taskId)
        }

    override suspend fun getTask(taskId: String): Task? = 
        operationsDelegate.getTask(taskId)

    override suspend fun getTaskHistory(profileId: String): List<Task> = 
        operationsDelegate.getTaskHistory(profileId)

    override suspend fun getAllTasks(): List<Task> = 
        operationsDelegate.getAllTasks()

    override suspend fun clearTaskHistory(profileId: String): Boolean = 
        synchronizationHelper.withLock {
            operationsDelegate.clearTaskHistory(profileId)
        }

    override suspend fun getTaskCount(
        profileId: String, 
        type: TaskType?, 
        status: TaskStatus?
    ): Int = operationsDelegate.getTaskCount(profileId, type, status)
}