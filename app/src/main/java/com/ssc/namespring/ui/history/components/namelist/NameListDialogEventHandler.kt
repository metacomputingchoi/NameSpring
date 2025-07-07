// ui/history/components/namelist/NameListDialogEventHandler.kt
package com.ssc.namespring.ui.history.components.namelist

import android.content.Context
import com.ssc.namespring.model.data.repository.TaskRepository
import com.ssc.namespring.model.domain.entity.Task
import com.ssc.namespring.ui.history.NameDetailDialog
import com.ssc.namespring.ui.history.TaskDetailDialog
import com.ssc.namingengine.data.GeneratedName

class NameListDialogEventHandler(
    private val context: Context,
    private val taskRepository: TaskRepository
) {

    fun showTaskDetail(task: Task) {
        TaskDetailDialog(context, task, taskRepository).show()
    }

    fun showNameDetail(name: GeneratedName) {
        NameDetailDialog(context, name).show()
    }

    fun handleSearchQueryChange(onQueryChanged: (String) -> Unit): (String) -> Unit {
        return { query ->
            onQueryChanged(query)
        }
    }

    fun handleSortOrderChange(onSortOrderChanged: (Int) -> Unit): (Int) -> Unit {
        return { sortOrder ->
            onSortOrderChanged(sortOrder)
        }
    }
}