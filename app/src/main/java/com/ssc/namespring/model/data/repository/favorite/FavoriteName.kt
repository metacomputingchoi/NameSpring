// model/data/repository/favorite/FavoriteName.kt
package com.ssc.namespring.model.data.repository.favorite

data class FavoriteName(
    val birthDateTime: Long,
    val nameKorean: String,
    val nameHanja: String,
    val surnameKorean: String,
    val surnameHanja: String,
    val jsonData: String,
    val addedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) {
    val fullNameKorean: String get() = "$surnameKorean$nameKorean"
    val fullNameHanja: String get() = "$surnameHanja$nameHanja"

    fun getKey(): String = "$birthDateTime-$fullNameKorean-$fullNameHanja"
}