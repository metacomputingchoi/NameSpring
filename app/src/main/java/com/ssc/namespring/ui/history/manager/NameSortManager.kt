package com.ssc.namespring.ui.history.manager

import com.ssc.namingengine.data.GeneratedName
import java.text.Collator
import java.util.Locale

class NameSortManager {

    private val koreanCollator = Collator.getInstance(Locale.KOREAN)

    fun sort(names: List<GeneratedName>, sortOrder: SortOrder): List<GeneratedName> {
        return when (sortOrder) {
            SortOrder.SCORE_DESC -> {
                names.sortedByDescending { it.analysisInfo?.totalScore ?: 0 }
            }
            SortOrder.SCORE_ASC -> {
                names.sortedBy { it.analysisInfo?.totalScore ?: 0 }
            }
            SortOrder.NAME_ASC -> {
                names.sortedWith { a, b ->
                    val nameA = "${a.surnameHangul}${a.combinedPronounciation}"
                    val nameB = "${b.surnameHangul}${b.combinedPronounciation}"
                    koreanCollator.compare(nameA, nameB)
                }
            }
            SortOrder.NAME_DESC -> {
                names.sortedWith { a, b ->
                    val nameA = "${a.surnameHangul}${a.combinedPronounciation}"
                    val nameB = "${b.surnameHangul}${b.combinedPronounciation}"
                    koreanCollator.compare(nameB, nameA)
                }
            }
        }
    }

    enum class SortOrder {
        SCORE_DESC, SCORE_ASC, NAME_ASC, NAME_DESC
    }
}