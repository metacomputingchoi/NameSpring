// model/domain/service/profileform/ProfileFormOperationService.kt
package com.ssc.namespring.model.domain.service.profileform

import android.widget.LinearLayout
import com.ssc.namespring.model.domain.entity.SurnameInfo
import com.ssc.namespring.model.domain.helper.NameDataHandler
import com.ssc.namespring.model.domain.usecase.NameDataManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormDateTimeManager
import com.ssc.namespring.model.domain.usecase.profileform.ProfileFormStateManager
import com.ssc.namespring.model.domain.service.interfaces.INameDataService
import java.util.Calendar

class ProfileFormOperationService(
    private val dateTimeManager: ProfileFormDateTimeManager,
    private val nameDataManager: NameDataManager,
    private val stateManager: ProfileFormStateManager,
    private val nameDataService: INameDataService,
    private val onStateUpdate: () -> Unit
) {
    private val nameDataHandler = NameDataHandler(nameDataManager, nameDataService)

    fun updateDate(calendar: Calendar) {
        dateTimeManager.updateDate(calendar)
        onStateUpdate()
    }

    fun updateTime(calendar: Calendar) {
        dateTimeManager.updateTime(calendar)
        onStateUpdate()
    }

    fun updateYajaTime(isChecked: Boolean) {
        stateManager.updateYajaTime(isChecked)
        onStateUpdate()
    }

    fun setSurname(surname: SurnameInfo?) {
        stateManager.setSurname(surname)
        onStateUpdate()
    }

    fun addNameChar() {
        if (nameDataHandler.addCharIfPossible()) onStateUpdate()
    }

    fun removeNameChar() {
        if (nameDataHandler.removeCharIfPossible()) onStateUpdate()
    }

    fun setHanjaInfo(position: Int, korean: String, hanja: String) {
        if (nameDataHandler.updateHanjaInfo(position, korean, hanja)) onStateUpdate()
    }

    fun syncWithUiValues(containerView: LinearLayout) {
        nameDataHandler.syncWithUI(containerView)
        onStateUpdate()
    }

    fun resetAllFields() {
        dateTimeManager.reset()
        nameDataManager.reset()
        stateManager.reset()
        onStateUpdate()
    }
}