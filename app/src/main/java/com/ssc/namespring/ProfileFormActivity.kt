// ProfileFormActivity.kt
package com.ssc.namespring

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ssc.namespring.model.domain.entity.ProfileFormConfig
import com.ssc.namespring.model.domain.entity.ProfileFormMode
import com.ssc.namespring.ui.profileform.ProfileFormActivityDelegate

class ProfileFormActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_CONFIG = "profile_form_config"

        fun newIntent(context: Context, config: ProfileFormConfig): Intent {
            return Intent(context, ProfileFormActivity::class.java).apply {
                putExtra(EXTRA_CONFIG, config)
            }
        }
    }

    private lateinit var config: ProfileFormConfig
    private lateinit var delegate: ProfileFormActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_form)

        config = intent.getSerializableExtra(EXTRA_CONFIG) as? ProfileFormConfig
            ?: ProfileFormConfig(ProfileFormMode.CREATE)

        delegate = ProfileFormActivityDelegate(this, config)
        delegate.onCreate()
    }

    fun syncUiStateWithInput(position: Int, korean: String, hanja: String) {
        delegate.syncUiStateWithInput(position, korean, hanja)
    }

    fun loadParentProfileData() {
        delegate.loadParentProfileData()
    }

    fun saveProfile() {
        delegate.saveProfile()
    }

    override fun onDestroy() {
        super.onDestroy()
        delegate.onDestroy()
    }
}
