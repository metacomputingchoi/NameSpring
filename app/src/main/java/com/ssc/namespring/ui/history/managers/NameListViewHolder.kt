// ui/history/managers/NameListViewHolder.kt
package com.ssc.namespring.ui.history.managers

import android.app.Dialog
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ssc.namespring.R

class NameListViewHolder(private val dialog: Dialog) {
    val searchView: SearchView by lazy { dialog.findViewById(R.id.searchView) }
    val sortSpinner: Spinner by lazy { dialog.findViewById(R.id.sortSpinner) }
    val recyclerView: RecyclerView by lazy { dialog.findViewById(R.id.recyclerView) }
    val emptyView: TextView by lazy { dialog.findViewById(R.id.emptyView) }
    val loadingView: ProgressBar by lazy { dialog.findViewById(R.id.loadingView) }
    val btnTaskInfo: MaterialButton by lazy { dialog.findViewById(R.id.btnTaskInfo) }
    val tvTitle: TextView by lazy { dialog.findViewById(R.id.tvTitle) }
    val btnClose: ImageButton by lazy { dialog.findViewById(R.id.btnClose) }
}