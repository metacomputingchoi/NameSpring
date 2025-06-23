// model/infrastructure/repository/HanjaRepository.kt
package com.ssc.namespring.model.infrastructure.repository

import com.ssc.namespring.model.domain.hanja.entity.Hanja

interface HanjaRepository {
    fun findByHanja(hanja: String): List<Hanja>
    fun findByStroke(stroke: Int): List<Hanja>
    fun findByStrokeAndPronunciation(stroke: Int, pronunciation: String): List<Hanja>
    fun getStrokeMap(): Map<String, Int>
}