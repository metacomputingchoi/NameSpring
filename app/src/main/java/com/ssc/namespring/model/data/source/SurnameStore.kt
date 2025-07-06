// model/data/source/SurnameStore.kt
package com.ssc.namespring.model.data.source

import com.ssc.namespring.model.domain.entity.CharTripleInfoSurname

class SurnameStore {
    var surnameHanjaMapping: Map<String, List<String>> = emptyMap()
    var chosungMapping: Map<String, List<String>> = emptyMap()
    var charTripleDict: Map<String, CharTripleInfoSurname> = emptyMap()
}