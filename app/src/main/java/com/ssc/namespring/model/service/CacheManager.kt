// model/service/CacheManager.kt
package com.ssc.namespring.model.service

class CacheManager {
    val hangulElementCache = mutableMapOf<Char, String?>()
    val hangulPnCache = mutableMapOf<Char, Int?>()
    val harmoniousCache = mutableMapOf<String, Boolean>()
}