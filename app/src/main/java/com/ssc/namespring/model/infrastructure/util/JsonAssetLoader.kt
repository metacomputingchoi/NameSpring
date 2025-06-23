// model/infrastructure/util/JsonAssetLoader.kt
package com.ssc.namespring.model.infrastructure.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class JsonAssetLoader(private val context: Context) {

    fun loadJsonArray(fileName: String): JSONArray {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return JSONArray(jsonString)
    }

    fun loadJsonObject(fileName: String): JSONObject {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return JSONObject(jsonString)
    }
}