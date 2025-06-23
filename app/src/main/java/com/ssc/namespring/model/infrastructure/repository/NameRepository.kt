// model/infrastructure/repository/NameRepository.kt
package com.ssc.namespring.model.infrastructure.repository

import org.json.JSONObject

interface NameRepository {
    fun existsHangulName(name: String): Boolean
    fun getHangulNameData(name: String): JSONObject
    fun getHangulSurnameFromHanja(hanjasSurname: String): String
}