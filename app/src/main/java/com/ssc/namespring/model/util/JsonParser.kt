// model/util/JsonParser.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.common.parsing.ParsingConstants.JsonKeys
import org.json.JSONArray
import org.json.JSONObject

object JsonParser {
    fun parseYmdData(json: String): List<Map<String, Any>> {
        return JSONArray(json).let { jsonArray ->
            (0 until jsonArray.length()).map { i ->
                jsonArray.getJSONObject(i).let { obj ->
                    mapOf(
                        JsonKeys.YEAR to obj.getInt(JsonKeys.YEAR),
                        JsonKeys.MONTH to obj.getInt(JsonKeys.MONTH),
                        JsonKeys.DAY to obj.getInt(JsonKeys.DAY),
                        JsonKeys.YEAR_PILLAR to obj.getString(JsonKeys.YEAR_PILLAR).normalizeNFC(),
                        JsonKeys.MONTH_PILLAR to obj.getString(JsonKeys.MONTH_PILLAR).normalizeNFC(),
                        JsonKeys.DAY_PILLAR to obj.getString(JsonKeys.DAY_PILLAR).normalizeNFC()
                    )
                }
            }
        }
    }

    fun parseJsonToMap(json: String): Map<String, Map<String, Any>> {
        return JSONObject(json).let { jsonObj ->
            jsonObj.keys().asSequence().mapNotNull { key ->
                try {
                    key.normalizeNFC() to parseInnerObject(jsonObj.getJSONObject(key))
                } catch (e: Exception) {
                    null
                }
            }.toMap()
        }
    }

    private fun parseInnerObject(jsonObj: JSONObject): Map<String, Any> {
        return jsonObj.keys().asSequence().associate { key ->
            key.normalizeNFC() to normalizeValue(jsonObj.get(key))
        }.toMap()
    }

    private fun normalizeValue(value: Any): Any {
        return when (value) {
            is String -> value.normalizeNFC()
            is JSONObject -> parseInnerObject(value)
            is JSONArray -> (0 until value.length()).map { normalizeValue(value.get(it)) }
            else -> value
        }
    }

    fun parseJsonToMapList(json: String): Map<String, List<String>> {
        return JSONObject(json).let { jsonObj ->
            jsonObj.keys().asSequence().mapNotNull { key ->
                try {
                    val jsonArray = jsonObj.getJSONArray(key)
                    key.normalizeNFC() to (0 until jsonArray.length()).map {
                        jsonArray.getString(it).normalizeNFC()
                    }
                } catch (e: Exception) {
                    null
                }
            }.toMap()
        }
    }

    fun parseSurnameHanjaPairMapping(json: String): Map<String, Any> {
        return JSONObject(json).let { jsonObj ->
            jsonObj.keys().asSequence().associate { key ->
                val value = jsonObj.get(key)
                key.normalizeNFC() to when (value) {
                    is JSONArray -> (0 until value.length()).map { value.getString(it).normalizeNFC() }
                    is String -> value.normalizeNFC()
                    else -> value
                }
            }.toMap()
        }
    }

    fun parseDictHangulGivenNames(json: String): Set<String> {
        return try {
            JSONArray(json).let { jsonArray ->
                (0 until jsonArray.length()).map {
                    jsonArray.getString(it).normalizeNFC()
                }.toSet()
            }
        } catch (e: Exception) {
            try {
                JSONObject(json).keys().asSequence().map { it.normalizeNFC() }.toSet()
            } catch (e2: Exception) {
                emptySet()
            }
        }
    }
}
