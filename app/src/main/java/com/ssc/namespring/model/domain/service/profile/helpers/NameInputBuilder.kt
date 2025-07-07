// model/domain/service/profile/helpers/NameInputBuilder.kt
package com.ssc.namespring.model.domain.service.profile.helpers

import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.SurnameInfo

internal class NameInputBuilder {

    fun buildNameInput(surname: SurnameInfo, givenName: GivenNameInfo): String {
        val surnameInput = "[${surname.korean}/${surname.hanja}]"
        val givenNameInput = givenName.charInfos.joinToString("") { charInfo ->
            "[${charInfo.korean}/${charInfo.hanja}]"
        }
        return surnameInput + givenNameInput
    }
}