// model/repository/surname/SurnameStore.kt
package com.ssc.namespring.model.repository.surname

class SurnameStore {
    var surnameMapping: Map<String, List<String>> = emptyMap()
    var surnameHanjaMapping: Map<String, List<String>> = emptyMap()
    var chosungMapping: Map<String, List<String>> = emptyMap()
    var charTripleDict: Map<String, CharTripleInfo> = emptyMap()
}