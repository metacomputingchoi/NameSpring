// utils/data/json/JsonFileLoader.kt
package com.ssc.namespring.utils.data.json

import android.content.Context
import com.google.gson.Gson
import com.ssc.namespring.utils.logger.AndroidLogger
import java.io.InputStreamReader

internal class JsonFileLoader(private val context: Context) {
    private val logger = AndroidLogger("JsonFileLoader")
    private val gson = Gson()

    inline fun <reified T> loadRequired(fileName: String): T {
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    gson.fromJson(reader, T::class.java)
                }
            }
        } catch (e: Exception) {
            logger.e("Failed to load required file: $fileName", e)
            throw e
        }
    }

    inline fun <reified T> loadOptional(fileName: String): T? {
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    gson.fromJson(reader, T::class.java)
                }
            }
        } catch (e: Exception) {
            logger.d("Optional file not found: $fileName")
            null
        }
    }
}