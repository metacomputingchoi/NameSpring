// model/application/service/report/utils/JsonAssetReader.kt
package com.ssc.namespring.model.application.service.report.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class JsonAssetReader(private val context: Context) {
    private val gson = Gson()

    fun <T> readAsset(fileName: String, type: Class<T>): T {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, type)
        } catch (e: IOException) {
            throw RuntimeException("Failed to read asset file: $fileName", e)
        }
    }

    fun <T> readAsset(fileName: String, typeToken: TypeToken<T>): T {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, typeToken.type)
        } catch (e: IOException) {
            throw RuntimeException("Failed to read asset file: $fileName", e)
        }
    }

    inline fun <reified T> readAssetInline(fileName: String): T {
        return readAsset(fileName, object : TypeToken<T>() {})
    }
}