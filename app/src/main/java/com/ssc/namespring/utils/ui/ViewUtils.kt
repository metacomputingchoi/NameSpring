// utils/ui/ViewUtils.kt
package com.ssc.namespring.utils.ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.usecase.ProfileManager
import com.ssc.namespring.utils.data.ProfileInputUtils
import com.ssc.namespring.utils.common.DateTimeUtils
import com.ssc.namespring.utils.common.SortingUtils
import java.util.Calendar

object ViewUtils {

    fun applyTheme(
        context: Context,
        rootLayout: ConstraintLayout,
        tvScoreIcon: TextView,
        theme: Profile.ScoreTheme
    ) = ThemeUtils.applyTheme(context, rootLayout, tvScoreIcon, theme)

    fun applyOhaengTheme(
        context: Context,
        containers: List<LinearLayout>,
        theme: Profile.ScoreTheme,
        values: List<Int>
    ) = ThemeUtils.applyOhaengTheme(context, containers, theme, values)

    fun showDatePicker(
        context: Context,
        currentDate: Calendar,
        onDateSet: (Calendar) -> Unit
    ) = DateTimeUtils.showDatePicker(context, currentDate, onDateSet)

    fun showTimePicker(
        context: Context,
        currentTime: Calendar,
        onTimeSet: (Calendar) -> Unit
    ) = DateTimeUtils.showTimePicker(context, currentTime, onTimeSet)

    fun setupProfileNameInput(
        etProfileName: TextInputEditText,
        profileNameLayout: TextInputLayout
    ) = ProfileInputUtils.setupProfileNameInput(etProfileName, profileNameLayout)

    fun setupSortChips(
        chipGroup: ChipGroup,
        inflater: LayoutInflater,
        onSortChanged: (ProfileManager.ProfileManagerSortType) -> Unit
    ) = SortingUtils.setupSortChips(chipGroup, inflater, onSortChanged)

    fun updateLayoutManager(
        recyclerView: RecyclerView
    ) = RecyclerViewUtils.updateLayoutManager(recyclerView)

    fun setupInfiniteScroll(
        recyclerView: RecyclerView,
        onLoadMore: () -> Unit
    ) = RecyclerViewUtils.setupInfiniteScroll(recyclerView, onLoadMore)

    fun updateSelectionModeUI(
        isSelectionMode: Boolean,
        searchView: SearchView,
        chipGroup: ChipGroup,
        tvSelectedCount: TextView,
        bottomActionBar: LinearLayout,
        fabAdd: FloatingActionButton,
        fabSelectAll: ExtendedFloatingActionButton,
        selectedCount: Int,
        totalCount: Int
    ) = UIStateUtils.updateSelectionModeUI(
        isSelectionMode, searchView, chipGroup, tvSelectedCount,
        bottomActionBar, fabAdd, fabSelectAll, selectedCount, totalCount
    )

    fun updateEmptyView(
        recyclerView: RecyclerView,
        emptyView: LinearLayout,
        fabAdd: FloatingActionButton,
        isEmpty: Boolean,
        isSelectionMode: Boolean
    ) = UIStateUtils.updateEmptyView(
        recyclerView, emptyView, fabAdd, isEmpty, isSelectionMode
    )
}