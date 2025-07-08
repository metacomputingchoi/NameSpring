// ui/history/managers/NameSortManager.kt
package com.ssc.namespring.ui.history.managers

import com.ssc.namingengine.data.GeneratedName
import java.text.Collator
import java.util.Locale

class NameSortManager {

    private val koreanCollator = Collator.getInstance(Locale.KOREAN)

    fun sort(names: List<GeneratedName>, nameSortOrder: NameSortOrder): List<GeneratedName> {
        return when (nameSortOrder) {
            NameSortOrder.SCORE_DESC -> {
                names.sortedByDescending { it.analysisInfo?.totalScore ?: 0 }
            }
            NameSortOrder.SCORE_ASC -> {
                names.sortedBy { it.analysisInfo?.totalScore ?: 0 }
            }
            NameSortOrder.NAME_ASC -> {
                names.sortedWith { a, b ->
                    val nameA = "${a.surnameHangul}${a.combinedPronounciation}"
                    val nameB = "${b.surnameHangul}${b.combinedPronounciation}"
                    koreanCollator.compare(nameA, nameB)
                }
            }
            NameSortOrder.NAME_DESC -> {
                names.sortedWith { a, b ->
                    val nameA = "${a.surnameHangul}${a.combinedPronounciation}"
                    val nameB = "${b.surnameHangul}${b.combinedPronounciation}"
                    koreanCollator.compare(nameB, nameA)
                }
            }
        }
    }

    enum class NameSortOrder {
        SCORE_DESC, SCORE_ASC, NAME_ASC, NAME_DESC
    }
}