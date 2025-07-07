// ui/history/components/NameListFavoriteHandler.kt
package com.ssc.namespring.ui.history.adapter.components

import android.util.Log
import com.google.gson.Gson
import com.ssc.namespring.model.data.repository.FavoriteName
import com.ssc.namespring.model.data.repository.FavoriteNameRepository
import com.ssc.namingengine.data.GeneratedName

class NameListFavoriteHandler(
    private val favoriteRepository: FavoriteNameRepository,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "NameListFavoriteHandler"
    }

    fun isFavorite(birthDateTimeMillis: Long, nameKorean: String, nameHanja: String): Boolean {
        return favoriteRepository.isFavorite(birthDateTimeMillis, nameKorean, nameHanja)
    }

    fun toggleFavorite(name: GeneratedName, birthDateTimeMillis: Long): Boolean {
        val fullNameKorean = "${name.surnameHangul}${name.combinedPronounciation}"
        val fullNameHanja = "${name.surnameHanja}${name.combinedHanja}"

        val favorite = createFavoriteName(name, birthDateTimeMillis)

        val isCurrentlyFavorite = isFavorite(
            birthDateTimeMillis,
            fullNameKorean,
            fullNameHanja
        )

        Log.d(TAG, "Toggle favorite: $fullNameKorean, current: $isCurrentlyFavorite")

        favoriteRepository.toggleFavorite(favorite)

        return isCurrentlyFavorite
    }

    private fun createFavoriteName(name: GeneratedName, birthDateTimeMillis: Long): FavoriteName {
        val givenNameKorean = name.combinedPronounciation
        val givenNameHanja = name.combinedHanja

        Log.d(TAG, "Creating favorite:")
        Log.d(TAG, "  Full name: ${name.surnameHangul}$givenNameKorean (${name.surnameHanja}$givenNameHanja)")
        Log.d(TAG, "  Surname: ${name.surnameHangul} (${name.surnameHanja})")
        Log.d(TAG, "  Given name: $givenNameKorean ($givenNameHanja)")

        return FavoriteName(
            birthDateTime = birthDateTimeMillis,
            nameKorean = givenNameKorean,
            nameHanja = givenNameHanja,
            surnameKorean = name.surnameHangul,
            surnameHanja = name.surnameHanja,
            jsonData = gson.toJson(name)
        )
    }
}