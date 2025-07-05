// ui/profileform/ProfileFormUIComponents.kt
package com.ssc.namespring.ui.profileform

import android.app.Activity
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.R

class ProfileFormUIComponents(activity: Activity) {
    val etProfileName: TextInputEditText = activity.findViewById(R.id.etProfileName)
    val profileNameLayout: TextInputLayout = activity.findViewById(R.id.profileNameLayout)
    val tvBirthDate: TextView = activity.findViewById(R.id.tvBirthDate)
    val tvBirthTime: TextView = activity.findViewById(R.id.tvBirthTime)
    val btnSelectDate: Button = activity.findViewById(R.id.btnSelectDate)
    val btnSelectTime: Button = activity.findViewById(R.id.btnSelectTime)
    val cbYajaTime: CheckBox = activity.findViewById(R.id.cbYajaTime)
    val btnSelectSurname: Button = activity.findViewById(R.id.btnSelectSurname)
    val tvSelectedSurname: TextView = activity.findViewById(R.id.tvSelectedSurname)
    val btnSave: Button = activity.findViewById(R.id.btnSave)
    val btnReset: Button = activity.findViewById(R.id.btnReset)
    val nameInputContainer: LinearLayout = activity.findViewById(R.id.nameInputContainer)
    val btnAddChar: ImageButton = activity.findViewById(R.id.btnAddChar)
    val btnRemoveChar: ImageButton = activity.findViewById(R.id.btnRemoveChar)
    val tvCharCount: TextView = activity.findViewById(R.id.tvCharCount)
    val btnBack: ImageButton = activity.findViewById(R.id.btnBack)
}