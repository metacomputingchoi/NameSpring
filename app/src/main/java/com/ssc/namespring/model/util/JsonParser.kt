// model/util/JsonParser.kt
package com.ssc.namespring.model.util

import com.ssc.namespring.model.common.parsing.ParsingConstants.JsonKeys
import com.ssc.namespring.model.exception.NamingException
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException

object JsonParser {

    fun parseYmdData(json: String): List<Map<String, Any>> {
        return parseJsonArray(json, "년월일 데이터 파싱 실패") { jsonArray ->
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
        return parseJsonObject(json, "JSON 맵 파싱 실패") { jsonObj ->
            jsonObj.keys().asSequence().mapNotNull { key ->
                try {
                    key.normalizeNFC() to parseInnerObject(jsonObj.getJSONObject(key))
                } catch (e: Exception) {
                    null
                }
            }.toMap()
        }
    }

    fun parseJsonToMapList(json: String): Map<String, List<String>> {
        return parseJsonObject(json, "JSON 리스트 맵 파싱 실패") { jsonObj ->
            jsonObj.keys().asSequence().mapNotNull { key ->
                try {
                    val jsonArray = jsonObj.getJSONArray(key)
                    key.normalizeNFC() to parseStringArray(jsonArray)
                } catch (e: Exception) {
                    null
                }
            }.toMap()
        }
    }

    fun parseSurnameHanjaPairMapping(json: String): Map<String, Any> {
        return parseJsonObject(json, "성씨 한자 매핑 파싱 실패") { jsonObj ->
            jsonObj.keys().asSequence().associate { key ->
                val value = jsonObj.get(key)
                key.normalizeNFC() to when (value) {
                    is JSONArray -> parseStringArray(value)
                    is String -> value.normalizeNFC()
                    else -> value
                }
            }.toMap()
        }
    }

    fun parseDictHangulGivenNames(json: String): Set<String> {
        return try {
            parseJsonArray(json, "한글 이름 사전 파싱") { jsonArray ->
                parseStringArray(jsonArray).toSet()
            }
        } catch (e: Exception) {
            try {
                parseJsonObject(json, "한글 이름 사전 파싱") { jsonObj ->
                    jsonObj.keys().asSequence().map { it.normalizeNFC() }.toSet()
                }
            } catch (e2: Exception) {
                emptySet()
            }
        }
    }

    // 제네릭 파싱 메소드들
    private inline fun <T> parseJsonArray(
        json: String,
        errorMessage: String,
        parser: (JSONArray) -> T
    ): T = parseJsonSafely(json, errorMessage, "JSON Array") {
        parser(JSONArray(json))
    }

    private inline fun <T> parseJsonObject(
        json: String,
        errorMessage: String,
        parser: (JSONObject) -> T
    ): T = parseJsonSafely(json, errorMessage, "JSON Object") {
        parser(JSONObject(json))
    }

    private inline fun <T> parseJsonSafely(
        json: String,
        errorMessage: String,
        dataType: String,
        parser: () -> T
    ): T {
        return try {
            parser()
        } catch (e: JSONException) {
            throw NamingException.DataNotFoundException(
                errorMessage,
                dataType = dataType,
                cause = e
            )
        }
    }

    private fun parseStringArray(jsonArray: JSONArray): List<String> {
        return (0 until jsonArray.length()).map {
            jsonArray.getString(it).normalizeNFC()
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
}