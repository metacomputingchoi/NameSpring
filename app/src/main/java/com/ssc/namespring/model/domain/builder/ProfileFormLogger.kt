// model/domain/builder/ProfileFormLogger.kt
package com.ssc.namespring.model.domain.builder

import android.util.Log
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.GivenNameInfo

/**
 * ProfileForm 관련 로깅을 담당
 */
internal class ProfileFormLogger {

    companion object {
        private const val TAG = "ProfileFormBuilder"
    }

    fun logProfileCreation(profileName: String, givenNameInfo: GivenNameInfo?) {
        Log.d(TAG, "Creating profile with name: $$profileName")
        logGivenNameInfo(givenNameInfo)
    }

    fun logCreatedProfile(profile: Profile) {
        Log.d(TAG, "Created profile: surname=${profile.surname?.korean}(${profile.surname?.hanja}), " +
                "givenName=${profile.givenName?.korean}(${profile.givenName?.hanja})")
    }

    private fun logGivenNameInfo(givenNameInfo: GivenNameInfo?) {
        if (givenNameInfo != null) {
            Log.d(TAG, "GivenNameInfo:")
            Log.d(TAG, "  Korean: '${givenNameInfo.korean}'")
            Log.d(TAG, "  Hanja: '${givenNameInfo.hanja}'")
            givenNameInfo.charInfos.forEachIndexed { index, charInfo ->
                Log.d(TAG, "  CharInfo[$$index]: korean='${charInfo.korean}', hanja='${charInfo.hanja}'")
            }
        } else {
            Log.d(TAG, "  GivenNameInfo is null")
        }
    }
}