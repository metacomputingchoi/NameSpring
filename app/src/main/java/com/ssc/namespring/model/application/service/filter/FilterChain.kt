// model/application/service/filter/FilterChain.kt
package com.ssc.namespring.model.application.service.filter

import com.ssc.namespring.model.application.service.filter.impl.*
import com.ssc.namespring.model.infrastructure.repository.NameRepository

class FilterChain {
    companion object {
        fun createStandardChain(nameRepository: NameRepository): NameFilter {
            val lengthFilter = LengthFilter()
            lengthFilter
                .setNext(ElementExtractionFilter())
                .setNext(CombinedLengthFilter())
                .setNext(YinYangDiversityFilter())
                .setNext(YinYangPositionFilter())
                .setNext(ElementHarmonyFilter())
                .setNext(JawonFilter())
                .setNext(NaturalnessFilter(nameRepository))

            return lengthFilter
        }
    }
}