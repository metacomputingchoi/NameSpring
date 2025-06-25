// model/service/CacheManager.kt
package com.ssc.namespring.model.service

class CacheManager {
    val baleumOhaengCache = mutableMapOf<Char, String?>()
    val baleumEumyangCache = mutableMapOf<Char, Int?>()
    val baleumOhaengHarmonyCache = mutableMapOf<String, Boolean>()
}
