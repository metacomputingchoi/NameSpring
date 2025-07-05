// ProfileListActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ssc.namespring.model.business.ProfileListManager
import com.ssc.namespring.model.business.ProfileListUiState
import com.ssc.namespring.model.adapter.ProfileAdapter
import com.ssc.namespring.utils.ViewUtils

class ProfileListActivity : AppCompatActivity() {
    private lateinit var viewModel: ProfileListManager
    private lateinit var adapter: ProfileAdapter

    // UI Components
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var fabSelectAll: ExtendedFloatingActionButton
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup
    private lateinit var tvProfileCount: TextView
    private lateinit var tvSelectedCount: TextView
    private lateinit var bottomActionBar: LinearLayout
    private lateinit var progressBar: ProgressBar

    // Activity Result Launchers
    private lateinit var createProfileLauncher: ActivityResultLauncher<Intent>
    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_list_improved)

        viewModel = ProfileListManager()
        setupActivityResultLaunchers()
        initViews()
        setupRecyclerView()
        observeViewModel()
        viewModel.loadProfiles()
    }

    private fun setupActivityResultLaunchers() {
        val resultCallback = { _: androidx.activity.result.ActivityResult ->
            viewModel.refreshProfiles()
        }

        createProfileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(), resultCallback
        )

        editProfileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(), resultCallback
        )
    }

    private fun initViews() {
        Log.d("ProfileListActivity", "initViews() called")

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)
        fabAdd = findViewById(R.id.fabAdd)
        fabSelectAll = findViewById(R.id.fabSelectAll)
        searchView = findViewById(R.id.searchView)
        chipGroup = findViewById(R.id.chipGroup)
        tvProfileCount = findViewById(R.id.tvProfileCount)
        tvSelectedCount = findViewById(R.id.tvSelectedCount)
        bottomActionBar = findViewById(R.id.bottomActionBar)
        progressBar = findViewById(R.id.progressBar)

        Log.d("ProfileListActivity", "Basic views initialized")

        setSupportActionBar(toolbar)

        fabAdd.setOnClickListener { navigateToProfileForm() }
        fabSelectAll.setOnClickListener { viewModel.toggleSelectAll() }

        // 삭제 버튼 찾기 시도
        Log.d("ProfileListActivity", "Looking for btnDeleteSelected")
        val deleteButton = findViewById<Button>(R.id.btnDeleteSelected)
        if (deleteButton != null) {
            Log.d("ProfileListActivity", "btnDeleteSelected found!")
            Log.d("ProfileListActivity", "Delete button enabled: ${deleteButton.isEnabled}")
            Log.d("ProfileListActivity", "Delete button clickable: ${deleteButton.isClickable}")
            Log.d("ProfileListActivity", "Delete button visibility: ${deleteButton.visibility}")

            // 버튼의 부모 뷰 상태 확인
            val parent = deleteButton.parent as? View
            Log.d("ProfileListActivity", "Parent visibility: ${parent?.visibility}")
            Log.d("ProfileListActivity", "Parent enabled: ${parent?.isEnabled}")

            deleteButton.setOnClickListener {
                Log.d("ProfileListActivity", "Delete button clicked!")
                Log.d("ProfileListActivity", "Button is enabled: ${deleteButton.isEnabled}")
                Log.d("ProfileListActivity", "Button is clickable: ${deleteButton.isClickable}")
                viewModel.deleteSelected(this@ProfileListActivity)
            }

            // 터치 이벤트 리스너 추가하여 터치 자체가 되는지 확인
            deleteButton.setOnTouchListener { v, event ->
                Log.d("ProfileListActivity", "Delete button touched! Action: ${event.action}")
                false // false를 반환하여 onClick도 실행되도록 함
            }
        } else {
            Log.e("ProfileListActivity", "btnDeleteSelected is NULL!")
        }

        findViewById<Button>(R.id.btnCancelSelection).setOnClickListener {
            viewModel.exitSelectionMode()
        }

        findViewById<Button>(R.id.btnCreateProfile).setOnClickListener { 
            navigateToProfileForm() 
        }

        setupSearch()
        ViewUtils.setupSortChips(chipGroup, layoutInflater) { sortType ->
            viewModel.setSortType(sortType)
        }
    }

    private fun setupRecyclerView() {
        adapter = ProfileAdapter(
            onItemClick = { profile -> viewModel.onProfileClick(this, profile) },
            onItemLongClick = { profile -> viewModel.onProfileLongClick(profile) },
            onEditClick = { profile -> navigateToProfileForm(profile.id) },
            onDeleteClick = { profile -> viewModel.deleteProfile(this, profile) },
            onDuplicateClick = { _ -> viewModel.refreshProfiles() }  // 복제 시 목록 새로고침
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        ViewUtils.setupInfiniteScroll(recyclerView) { viewModel.loadMoreProfiles() }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            updateUI(state)
        }
    }

    private fun updateUI(state: ProfileListUiState) {
        Log.d("ProfileListActivity", "updateUI: isSelectionMode=${state.isSelectionMode}, selectedCount=${state.selectedIds.size}")

        adapter.submitList(state.profiles, state.isSelectionMode, state.selectedIds)

        progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        tvProfileCount.text = "총 ${state.profiles.size}개"
        tvSelectedCount.text = "${state.selectedIds.size}개 선택됨"

        ViewUtils.updateSelectionModeUI(
            state.isSelectionMode,
            searchView, chipGroup, tvSelectedCount, bottomActionBar,
            fabAdd, fabSelectAll, state.selectedIds.size,
            adapter.getSelectableItemCount()
        )

        ViewUtils.updateEmptyView(recyclerView, emptyView, fabAdd,
            state.profiles.isEmpty(), state.isSelectionMode)

        invalidateOptionsMenu()

        // bottomActionBar의 상태 직접 확인
        Log.d("ProfileListActivity", "bottomActionBar visibility after update: ${bottomActionBar.visibility}")
    }

    private fun navigateToProfileForm(profileId: String? = null) {
        val intent = Intent(this, ProfileFormActivity::class.java)
        profileId?.let { intent.putExtra("profileId", it) }

        if (profileId == null) {
            createProfileLauncher.launch(intent)
        } else {
            editProfileLauncher.launch(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_mode -> {
                viewModel.toggleSelectionMode()
                true
            }
            R.id.action_load_all -> {
                viewModel.loadAllProfiles(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (viewModel.isInSelectionMode()) {
            viewModel.exitSelectionMode()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshProfiles()
    }
}
