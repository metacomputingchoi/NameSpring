// model/test/TestCaseLoader.kt
package com.ssc.namespring.model.test

import android.content.Context
import com.ssc.namespring.model.data.TestCase
import com.ssc.namespring.model.data.TestConfig
import com.ssc.namespring.model.data.BirthInfo
import org.json.JSONObject

class TestCaseLoader(private val context: Context) {

    fun loadTestConfig(fileName: String = "testcases.json"): TestConfig {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)

        val birthInfoJson = jsonObject.getJSONObject("birthInfo")
        val birthInfo = BirthInfo(
            year = birthInfoJson.getInt("year"),
            month = birthInfoJson.getInt("month"),
            day = birthInfoJson.getInt("day"),
            hour = birthInfoJson.getInt("hour"),
            minute = birthInfoJson.getInt("minute")
        )

        val testCasesArray = jsonObject.getJSONArray("testCases")
        val testCases = mutableListOf<TestCase>()

        for (i in 0 until testCasesArray.length()) {
            val tcJson = testCasesArray.getJSONObject(i)
            testCases.add(TestCase(
                name = tcJson.getString("name"),
                surHangul = tcJson.optString("surHangul", null).takeIf { it != "null" },
                surHanja = tcJson.getString("surHanja"),
                name1Hangul = tcJson.optString("name1Hangul", null).takeIf { it != "null" },
                name1Hanja = tcJson.optString("name1Hanja", null).takeIf { it != "null" },
                name2Hangul = tcJson.optString("name2Hangul", null).takeIf { it != "null" },
                name2Hanja = tcJson.optString("name2Hanja", null).takeIf { it != "null" }
            ))
        }

        return TestConfig(
            targetName = jsonObject.getString("targetName"),
            targetCaseIndex = jsonObject.getInt("targetCaseIndex"),
            birthInfo = birthInfo,
            testCases = testCases
        )
    }
}