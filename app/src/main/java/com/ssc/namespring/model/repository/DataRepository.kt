// model/repository/DataRepository.kt
package com.ssc.namespring.model.repository

import com.ssc.namespring.model.Constants.JsonKeys
import com.ssc.namespring.model.util.JsonParser

class DataRepository {
    lateinit var ymdData: List<Map<String, Any>>
    lateinit var nameCharTripleDict: Map<String, Map<String, Any>>
    lateinit var surnameCharTripleDict: Map<String, Map<String, Any>>
    lateinit var nameKoreanToTripleKeys: Map<String, List<String>>
    lateinit var nameHanjaToTripleKeys: Map<String, List<String>>
    lateinit var surnameKoreanToTripleKeys: Map<String, List<String>>
    lateinit var surnameHanjaToTripleKeys: Map<String, List<String>>
    lateinit var surnameHanjaPairMapping: Map<String, Any>
    lateinit var dictHangulGivenNames: Set<String>
    lateinit var surnameChosungToKorean: Map<String, List<String>>

    fun loadFromJson(
        ymdJson: String,
        nameCharTripleJson: String,
        surnameCharTripleJson: String,
        nameKoreanToTripleJson: String,
        nameHanjaToTripleJson: String,
        surnameKoreanToTripleJson: String,
        surnameHanjaToTripleJson: String,
        surnameHanjaPairJson: String,
        dictHangulGivenNamesJson: String,
        surnameChosungToKoreanJson: String
    ) {
        ymdData = JsonParser.parseYmdData(ymdJson)
        nameCharTripleDict = JsonParser.parseJsonToMap(nameCharTripleJson)
        surnameCharTripleDict = JsonParser.parseJsonToMap(surnameCharTripleJson)
        nameKoreanToTripleKeys = JsonParser.parseJsonToMapList(nameKoreanToTripleJson)
        nameHanjaToTripleKeys = JsonParser.parseJsonToMapList(nameHanjaToTripleJson)
        surnameKoreanToTripleKeys = JsonParser.parseJsonToMapList(surnameKoreanToTripleJson)
        surnameHanjaToTripleKeys = JsonParser.parseJsonToMapList(surnameHanjaToTripleJson)
        surnameHanjaPairMapping = JsonParser.parseSurnameHanjaPairMapping(surnameHanjaPairJson)
        surnameChosungToKorean = JsonParser.parseJsonToMapList(surnameChosungToKoreanJson)
        dictHangulGivenNames = JsonParser.parseDictHangulGivenNames(dictHangulGivenNamesJson)
    }
}