// ProfileListActivity.kt
package com.ssc.namespring

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.usecase.ProfileListManager
import com.ssc.namespring.model.presentation.adapter.ProfileAdapter
import com.ssc.namespring.ui.profilelist.ProfileListComponents
import com.ssc.namespring.ui.profilelist.ProfileListNavigator
import com.ssc.namespring.ui.profilelist.ProfileListAdapterManager
import com.ssc.namespring.ui.profilelist.ProfileListLauncherManager
import com.ssc.namespring.ui.profilelist.ProfileListMenuHandler
import com.ssc.namespring.ui.profilelist.ProfileListObserver

class ProfileListActivity : AppCompatActivity() {
    lateinit var listManager: ProfileListManager
    lateinit var adapter: ProfileAdapter
    lateinit var components: ProfileListComponents

    private lateinit var navigator: ProfileListNavigator
    private lateinit var launcherManager: ProfileListLauncherManager
    private lateinit var adapterManager: ProfileListAdapterManager
    private lateinit var menuHandler: ProfileListMenuHandler
    private lateinit var observer: ProfileListObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_list_improved)

        initializeCore()
        setupUI()
        setupBackPressedCallback()
        startObserving()
        listManager.loadProfiles()
    }

    private fun initializeCore() {
        listManager = ProfileListManager()
        components = ProfileListComponents(this)
        navigator = ProfileListNavigator(this)
        components.initializeAll()
    }

    private fun setupUI() {
        launcherManager = ProfileListLauncherManager(this, listManager)
        adapterManager = ProfileListAdapterManager(
            this,
            listManager,
            navigator,
            components
        )
        adapter = adapterManager.createAdapter()
        components.viewHolder.recyclerView.adapter = adapter

        menuHandler = ProfileListMenuHandler(listManager)
        observer = ProfileListObserver(this, listManager, components, adapter)
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (listManager.isInSelectionMode()) {
                    listManager.exitSelectionMode()
                } else {
                    finish()
                }
            }
        })
    }

    private fun startObserving() {
        observer.startObserving()
    }

    fun launchProfileForm(intent: Intent, isEdit: Boolean) {
        launcherManager.launchProfileForm(intent, isEdit)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return menuHandler.handleMenuItemSelected(this, item) ||
                super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        listManager.refreshProfiles()
    }
}