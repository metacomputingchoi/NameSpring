// utils/ViewUtils.kt
package com.ssc.namespring.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ssc.namespring.R
import com.ssc.namespring.model.data.Profile
import com.ssc.namespring.model.repository.ProfileManager
import java.util.Calendar

object ViewUtils {

    fun applyTheme(
        context: Context,
        rootLayout: ConstraintLayout,
        ivScoreIcon: ImageView,
        theme: Profile.ScoreTheme
    ) {
        when (theme) {
            Profile.ScoreTheme.SUNNY_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_sunny_spring)
                ivScoreIcon.setImageResource(R.drawable.ic_flower_full)
            }
            Profile.ScoreTheme.WARM_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_warm_spring)
                ivScoreIcon.setImageResource(R.drawable.ic_sprout_bloom)
            }
            Profile.ScoreTheme.CLOUDY_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_cloudy_spring)
                ivScoreIcon.setImageResource(R.drawable.ic_sprout)
            }
            Profile.ScoreTheme.RAINY_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_rainy_spring)
                ivScoreIcon.setImageResource(R.drawable.ic_seed)
            }
            Profile.ScoreTheme.COLD_SPRING -> {
                rootLayout.setBackgroundResource(R.drawable.bg_cold_spring)
                ivScoreIcon.setImageResource(R.drawable.ic_dormant_seed)
            }
            Profile.ScoreTheme.NOT_EVALUATED -> {
                rootLayout.setBackgroundResource(R.drawable.bg_not_evaluated)
                ivScoreIcon.setImageResource(R.drawable.ic_seed)
            }
        }
    }

    fun applyOhaengTheme(
        context: Context,
        containers: List<LinearLayout>,
        theme: Profile.ScoreTheme,
        values: List<Int>
    ) {
        val ohaengElements = listOf("木", "火", "土", "金", "水")

        containers.forEachIndexed { index, container ->
            if (index < values.size) {
                val value = values[index]
                val isLacking = value == 0

                when (theme) {
                    Profile.ScoreTheme.SUNNY_SPRING -> {
                        if (isLacking) {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_lacking_sunny)
                        } else {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_normal_sunny)
                        }
                    }
                    Profile.ScoreTheme.WARM_SPRING -> {
                        if (isLacking) {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_lacking_warm)
                        } else {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_normal_warm)
                        }
                    }
                    Profile.ScoreTheme.CLOUDY_SPRING -> {
                        if (isLacking) {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_lacking_cloudy)
                        } else {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_normal_cloudy)
                        }
                    }
                    Profile.ScoreTheme.RAINY_SPRING -> {
                        if (isLacking) {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_lacking_rainy)
                        } else {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_normal_rainy)
                        }
                    }
                    Profile.ScoreTheme.COLD_SPRING -> {
                        if (isLacking) {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_lacking_cold)
                        } else {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_normal_cold)
                        }
                    }
                    Profile.ScoreTheme.NOT_EVALUATED -> {
                        if (isLacking) {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_lacking_neutral)
                        } else {
                            container.setBackgroundResource(R.drawable.bg_ohaeng_normal_neutral)
                        }
                    }
                }
            }
        }
    }

    fun showDatePicker(context: Context, currentDate: Calendar, onDateSet: (Calendar) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate.set(Calendar.YEAR, year)
                newDate.set(Calendar.MONTH, month)
                newDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                onDateSet(newDate)
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker(context: Context, currentTime: Calendar, onTimeSet: (Calendar) -> Unit) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newTime = Calendar.getInstance()
                newTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                newTime.set(Calendar.MINUTE, minute)
                onTimeSet(newTime)
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            false
        ).show()
    }

    fun setupProfileNameInput(
        etProfileName: TextInputEditText,
        profileNameLayout: TextInputLayout
    ) {
        etProfileName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etProfileName.clearFocus()
                profileNameLayout.isEndIconVisible = true
                true
            } else {
                false
            }
        }
    }

    fun setupSortChips(
        chipGroup: ChipGroup,
        inflater: LayoutInflater,
        onSortChanged: (ProfileManager.SortType) -> Unit
    ) {
        val sortOptions = listOf(
            "최신순" to ProfileManager.SortType.DATE_DESC,
            "오래된순" to ProfileManager.SortType.DATE_ASC,
            "점수높은순" to ProfileManager.SortType.SCORE_DESC,
            "점수낮은순" to ProfileManager.SortType.SCORE_ASC,
            "이름순" to ProfileManager.SortType.NAME_ASC,
            "이름역순" to ProfileManager.SortType.NAME_DESC
        )

        sortOptions.forEachIndexed { index, (label, sortType) ->
            val chip = inflater.inflate(R.layout.chip_sort, chipGroup, false) as Chip
            chip.text = label
            chip.isChecked = index == 0
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onSortChanged(sortType)
                }
            }
            chipGroup.addView(chip)
        }
    }

    fun updateLayoutManager(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    }

    fun setupInfiniteScroll(recyclerView: RecyclerView, onLoadMore: () -> Unit) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem >= totalItemCount - 5) {
                    onLoadMore()
                }
            }
        })
    }

    fun updateSelectionModeUI(
        isSelectionMode: Boolean,
        searchView: androidx.appcompat.widget.SearchView,
        chipGroup: ChipGroup,
        tvSelectedCount: TextView,
        bottomActionBar: LinearLayout,
        fabAdd: FloatingActionButton,
        fabSelectAll: ExtendedFloatingActionButton,
        selectedCount: Int,
        totalCount: Int
    ) {
        Log.d("ViewUtils", "updateSelectionModeUI: isSelectionMode=$isSelectionMode, selectedCount=$selectedCount")

        searchView.isVisible = !isSelectionMode
        chipGroup.isVisible = !isSelectionMode
        tvSelectedCount.isVisible = isSelectionMode

        bottomActionBar.isVisible = isSelectionMode
        Log.d("ViewUtils", "bottomActionBar.isVisible = ${bottomActionBar.isVisible}")
        Log.d("ViewUtils", "bottomActionBar.visibility = ${bottomActionBar.visibility}")

        if (isSelectionMode) {
            // 삭제 버튼 활성화/비활성화 처리 추가
            val deleteButton = bottomActionBar.findViewById<Button>(R.id.btnDeleteSelected)
            deleteButton?.isEnabled = selectedCount > 0
            Log.d("ViewUtils", "Delete button enabled: ${deleteButton?.isEnabled}, selectedCount: $selectedCount")

            fabAdd.hide()
            fabSelectAll.show()

            if (selectedCount == totalCount && totalCount > 0) {
                fabSelectAll.text = "전체 해제"
                fabSelectAll.setIconResource(R.drawable.ic_clear)
            } else {
                fabSelectAll.text = "전체 선택"
                fabSelectAll.setIconResource(R.drawable.ic_check)
            }
        } else {
            fabAdd.show()
            fabSelectAll.hide()
        }
    }

    fun updateEmptyView(
        recyclerView: RecyclerView,
        emptyView: LinearLayout,
        fabAdd: FloatingActionButton,
        isEmpty: Boolean,
        isSelectionMode: Boolean
    ) {
        if (isEmpty) {
            recyclerView.isVisible = false
            emptyView.isVisible = true
            fabAdd.hide()
        } else {
            recyclerView.isVisible = true
            emptyView.isVisible = false
            if (!isSelectionMode) {
                fabAdd.show()
            }
        }
    }
}
