// model/domain/service/interfaces/SearchService.kt
package com.ssc.namespring.model.domain.service.interfaces

interface SearchService<T> {
    fun search(query: String): List<T>
}
