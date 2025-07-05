// ui/profileform/ProfileFormEventHandler.kt
package com.ssc.namespring.ui.profileform

import com.ssc.namespring.ProfileFormActivity
import com.ssc.namespring.model.domain.usecase.ProfileFormManager
import com.ssc.namespring.model.domain.service.ProfileFormService
import com.ssc.namespring.model.presentation.components.SearchDialogManager
import com.ssc.namespring.utils.ui.ViewUtils

class ProfileFormEventHandler(
    private val activity: ProfileFormActivity,
    private val formManager: ProfileFormManager,
    private val searchDialogManager: SearchDialogManager,
    private val profileFormService: ProfileFormService,
    private val uiComponents: ProfileFormUIComponents,
    private val nameInputHandler: ProfileFormNameInputHandler
) {
    fun setupListeners() {
        uiComponents.btnBack.setOnClickListener { activity.finish() }

        uiComponents.btnSelectDate.setOnClickListener {
            ViewUtils.showDatePicker(activity, formManager.getSelectedDate()) { date ->
                formManager.updateDate(date)
            }
        }

        uiComponents.btnSelectTime.setOnClickListener {
            ViewUtils.showTimePicker(activity, formManager.getSelectedDate()) { time ->
                formManager.updateTime(time)
            }
        }

        uiComponents.btnSelectSurname.setOnClickListener {
            searchDialogManager.showSurnameDialog(activity) { surname ->
                formManager.setSurname(surname)
            }
        }

        uiComponents.btnAddChar.setOnClickListener {
            formManager.addNameChar()
        }

        uiComponents.btnRemoveChar.setOnClickListener {
            formManager.removeNameChar()
        }

        uiComponents.btnSave.setOnClickListener {
            activity.saveProfile()
        }

        uiComponents.btnReset.setOnClickListener {
            profileFormService.showResetDialog(activity) {
                formManager.resetAllFields()
            }
        }

        uiComponents.cbYajaTime.setOnCheckedChangeListener { _, isChecked ->
            formManager.updateYajaTime(isChecked)
        }

        ViewUtils.setupProfileNameInput(
            uiComponents.etProfileName,
            uiComponents.profileNameLayout
        )
    }
}