// model/repository/ProfileRepository.kt
package com.ssc.namespring.model.repository

import com.ssc.namespring.model.data.Profile

interface ProfileRepository {
    suspend fun insert(profile: Profile)
    suspend fun update(profile: Profile)
    suspend fun delete(id: String)
    suspend fun getById(id: String): Profile?
    suspend fun getAll(): List<Profile>
    suspend fun getByName(surname: String, givenName: String): Profile?
    suspend fun getDefaultProfile(): Profile?
    suspend fun setDefaultProfile(id: String)
}