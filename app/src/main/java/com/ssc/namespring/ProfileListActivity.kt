// ProfileListActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.business.ProfileListManager
import com.ssc.namespring.model.adapter.ProfileAdapter
import com.ssc.namespring.profile.ProfileListComponents
import com.ssc.namespring.profile.ProfileListNavigator

class ProfileListActivity : AppCompatActivity() {
    lateinit var viewModel: ProfileListManager
    lateinit var adapter: ProfileAdapter
    lateinit var components: ProfileListComponents

    private lateinit var navigator: ProfileListNavigator
    private lateinit var createProfileLauncher: ActivityResultLauncher<Intent>
    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_list_improved)

        viewModel = ProfileListManager()
        setupActivityResultLaunchers()

        components = ProfileListComponents(this)
        navigator = ProfileListNavigator(this)

        components.initializeAll()
        setupAdapter()
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

    private fun setupAdapter() {
        adapter = ProfileAdapter(
            onItemClick = { profile -> viewModel.onProfileClick(this, profile) },
            onItemLongClick = { profile -> viewModel.onProfileLongClick(profile) },
            onEditClick = { profile -> navigator.navigateToProfileForm(profile.id) },
            onDeleteClick = { profile -> viewModel.deleteProfile(this, profile) },
            onDuplicateClick = { _ -> viewModel.refreshProfiles() }
        )
        components.viewHolder.recyclerView.adapter = adapter
    }

    fun launchProfileForm(intent: Intent, isEdit: Boolean) {
        if (isEdit) {
            editProfileLauncher.launch(intent)
        } else {
            createProfileLauncher.launch(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            components.uiUpdater.updateUI(state, adapter)
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