// model/domain/usecase/NameDataManager.kt
package com.ssc.namespring.model.domain.usecase

import android.util.Log
import com.ssc.namespring.model.presentation.components.NameCharData
import com.ssc.namespring.model.domain.entity.CharInfo
import com.ssc.namespring.model.domain.entity.GivenNameInfo
import com.ssc.namespring.model.domain.entity.Profile
import com.ssc.namespring.model.domain.entity.NameCharacter
import com.ssc.namespring.model.domain.entity.NameComposition
import com.ssc.namespring.model.data.mapper.CharTripleInfo
import com.ssc.namespring.model.domain.service.interfaces.INameDataManager
import com.ssc.namespring.model.domain.service.name.NameCompositionService

class NameDataManager : INameDataManager {
    companion object {
        private const val TAG = "NameDataManager"
    }

    private var nameComposition = NameComposition()
    private val compositionService = NameCompositionService()

    // 한자 정보를 별도로 관리 (CharTripleInfo 저장)
    private val hanjaInfoMap = mutableMapOf<Int, CharTripleInfo>()

    // 현재 상태를 추적하기 위한 맵 (디버깅용)
    private val currentStateMap = mutableMapOf<Int, Pair<String, String>>()

    override fun initialize() {
        Log.d(TAG, "initialize()")
        nameComposition = NameComposition()
        hanjaInfoMap.clear()
        currentStateMap.clear()
    }

    override fun loadFromProfile(profile: Profile) {
        Log.d(TAG, "loadFromProfile: profile=${profile.profileName}")

        nameComposition = NameComposition.fromGivenNameInfo(profile.givenName)
        hanjaInfoMap.clear()
        currentStateMap.clear()

        // 프로필에서 한자 정보 복원
        profile.givenName?.charInfos?.forEachIndexed { index, charInfo ->
            if (charInfo.korean.isNotEmpty() && charInfo.hanja.isNotEmpty()) {
                // 현재 상태 저장
                currentStateMap[index] = Pair(charInfo.korean, charInfo.hanja)

                // CharTripleInfo 복원 시도
                com.ssc.namespring.model.domain.entity.NameData.getCharInfo(
                    charInfo.korean,
                    charInfo.hanja
                )?.let { info ->
                    hanjaInfoMap[index] = info
                    Log.d(TAG, "Loaded hanja info for position $index: ${charInfo.korean}/${charInfo.hanja}")
                }
            }
        }
    }

    override fun canAddChar(): Boolean = nameComposition.canAddCharacter()

    override fun canRemoveChar(): Boolean = nameComposition.canRemoveCharacter()

    override fun addChar() {
        Log.d(TAG, "addChar()")
        nameComposition = nameComposition.addCharacter()
    }

    override fun removeChar() {
        Log.d(TAG, "removeChar()")
        val lastIndex = nameComposition.visibleCount - 1
        hanjaInfoMap.remove(lastIndex)
        currentStateMap.remove(lastIndex)
        nameComposition = nameComposition.removeCharacter()
    }

    override fun setCharData(position: Int, korean: String, hanja: String) {
        Log.d(TAG, "setCharData: position=$position, korean='$korean', hanja='$hanja'")

        val currentChar = nameComposition.getCharacter(position)
        val previousState = currentStateMap[position]
        val koreanChanged = previousState?.first != korean

        // 상태 업데이트
        currentStateMap[position] = Pair(korean, hanja)

        // 한글이 변경되었고 기존에 한자가 있었으면 한자 정보 제거
        if (koreanChanged && currentChar?.hanja?.isNotEmpty() == true) {
            hanjaInfoMap.remove(position)
            Log.d(TAG, "Korean changed, removing hanja info for position $position")
        }

        // NameComposition 업데이트
        nameComposition = nameComposition.updateCharacter(position) { character ->
            when {
                koreanChanged && character.hanja.isNotEmpty() -> {
                    // 한글이 변경되면 한자를 초기화
                    character.withKorean(korean).clearHanja()
                }
                korean.isNotEmpty() && hanja.isNotEmpty() -> {
                    // 한글과 한자가 모두 있는 경우
                    val charInfo = hanjaInfoMap[position]?.let { info ->
                        convertToCharInfo(info)
                    } ?: CharInfo(korean = korean, hanja = hanja)

                    character
                        .withKorean(korean)
                        .withHanja(hanja)
                        .withCharInfo(charInfo)
                }
                else -> {
                    // 그 외의 경우
                    character
                        .withKorean(korean)
                        .withHanja(hanja)
                        .withCharInfo(null)
                }
            }
        }

        Log.d(TAG, "Updated character at position $position: korean='$korean', hanja='$hanja'")
    }

    override fun setHanjaInfo(position: Int, info: CharTripleInfo) {
        try {
            val korean = info.koreanInfo?.character ?: return
            val hanja = info.hanjaInfo?.character ?: return

            Log.d(TAG, "setHanjaInfo: position=$position, korean='$korean', hanja='$hanja'")

            // 한자 정보 저장
            hanjaInfoMap[position] = info
            currentStateMap[position] = Pair(korean, hanja)

            // CharInfo 생성
            val charInfo = convertToCharInfo(info)

            // NameComposition 업데이트
            nameComposition = nameComposition.updateCharacter(position) { character ->
                character
                    .withKorean(korean)
                    .withHanja(hanja)
                    .withCharInfo(charInfo)
            }

            Log.d(TAG, "Hanja info set successfully at position $position")

        } catch (e: Exception) {
            Log.e(TAG, "Error in setHanjaInfo", e)
        }
    }

    override fun removeHanjaInfo(position: Int) {
        Log.d(TAG, "removeHanjaInfo: position=$position")

        hanjaInfoMap.remove(position)
        currentStateMap[position]?.let { (korean, _) ->
            currentStateMap[position] = Pair(korean, "")
        }

        nameComposition = nameComposition.updateCharacter(position) { character ->
            character.clearHanja()
        }
    }

    override fun getCharCount(): Int = nameComposition.size

    override fun getCharDataList(): List<NameCharData> {
        return (0 until nameComposition.visibleCount).map { index ->
            val character = nameComposition.getCharacter(index)
            NameCharData(
                korean = character?.korean ?: "",
                hanja = character?.hanja ?: ""
            )
        }
    }

    override fun getCharData(position: Int): NameCharData? {
        return nameComposition.getCharacter(position)?.let { character ->
            NameCharData(
                korean = character.korean,
                hanja = character.hanja
            )
        }
    }

    override fun getHanjaInfo(position: Int): CharTripleInfo? = hanjaInfoMap[position]

    override fun createGivenNameInfo(): GivenNameInfo? {
        Log.d(TAG, "createGivenNameInfo called")

        // 현재 상태 확인
        Log.d(TAG, "Current state:")
        currentStateMap.forEach { (pos, state) ->
            Log.d(TAG, "  Position $pos: korean='${state.first}', hanja='${state.second}'")
        }

        val givenNameInfo = nameComposition.toGivenNameInfo()

        if (givenNameInfo != null) {
            Log.d(TAG, "Created GivenNameInfo: korean='${givenNameInfo.korean}', hanja='${givenNameInfo.hanja}'")
            givenNameInfo.charInfos.forEachIndexed { index, charInfo ->
                Log.d(TAG, "  CharInfo[$index]: korean='${charInfo.korean}', hanja='${charInfo.hanja}', " +
                        "meaning='${charInfo.meaning}', strokes=${charInfo.strokes}, " +
                        "ohaeng='${charInfo.ohaeng}', eumyang=${charInfo.eumyang}")
            }
        } else {
            Log.d(TAG, "GivenNameInfo is null")
        }

        return givenNameInfo
    }

    override fun reset() {
        Log.d(TAG, "reset()")
        nameComposition = NameComposition()
        hanjaInfoMap.clear()
        currentStateMap.clear()
    }

    private fun convertToCharInfo(tripleInfo: CharTripleInfo): CharInfo {
        return CharInfo(
            korean = tripleInfo.koreanInfo?.character ?: "",
            hanja = tripleInfo.hanjaInfo?.character ?: "",
            meaning = tripleInfo.integratedInfo?.nameMeaning,
            strokes = tripleInfo.hanjaInfo?.strokes ?: 0,
            ohaeng = tripleInfo.hanjaInfo?.ohaeng ?: "",
            eumyang = tripleInfo.hanjaInfo?.eumyang ?: 0
        )
    }
}