// utils/NamingSystemInitializer.kt
package com.ssc.namespring.utils

import com.ssc.namespring.model.core.NamingSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class NamingSystemInitializer(private val jsonFileLoader: JsonFileLoader) {

    private val namingSystem = NamingSystem.instance

    suspend fun initialize() {
        try {
            val jsonData = withContext(Dispatchers.IO) {
                jsonFileLoader.loadAllJsonFiles()
            }

            withContext(Dispatchers.IO) {
                namingSystem.initializeFromJson(
                    ymdJson = jsonData["ymd"]!!,
                    nameCharTripleJson = jsonData["nameCharTriple"]!!,
                    surnameCharTripleJson = jsonData["surnameCharTriple"]!!,
                    nameKoreanToTripleJson = jsonData["nameKoreanToTriple"]!!,
                    nameHanjaToTripleJson = jsonData["nameHanjaToTriple"]!!,
                    surnameKoreanToTripleJson = jsonData["surnameKoreanToTriple"]!!,
                    surnameHanjaToTripleJson = jsonData["surnameHanjaToTriple"]!!,
                    surnameHanjaPairJson = jsonData["surnameHanjaPair"]!!,
                    dictHangulGivenNamesJson = jsonData["dictHangulGivenNames"]!!,
                    surnameChosungToKoreanJson = jsonData["surnameChosungToKorean"]!!
                )
            }
        } catch (e: IOException) {
            throw IOException("파일 로드 중 오류 발생", e)
        } catch (e: Exception) {
            throw Exception("NamingSystem 초기화 중 오류 발생", e)
        }
    }
}