// model/domain/service/theme/providers/SproutStateProvider.kt
package com.ssc.namespring.model.domain.service.theme.providers

import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.SproutState

class SproutStateProvider {

    fun getSproutIconText(state: SproutState): String {
        return when (state) {
            SproutState.DORMANT -> "🌰"
            SproutState.SEED -> "🌱"
            SproutState.SPROUTING -> "🌿"
            SproutState.GROWING -> "🌳"
            SproutState.BLOOMING -> "🌸"
        }
    }

    fun getSproutIconResource(state: SproutState): Int {
        return when (state) {
            SproutState.DORMANT -> R.drawable.ic_dormant_seed
            SproutState.SEED -> R.drawable.ic_seed
            SproutState.SPROUTING -> R.drawable.ic_sprout
            SproutState.GROWING -> R.drawable.ic_sprout_bloom
            SproutState.BLOOMING -> R.drawable.ic_flower_full
        }
    }

    fun getSproutIconColor(state: SproutState): Int {
        return when (state) {
            SproutState.DORMANT -> R.color.dormant_gray
            SproutState.SEED -> R.color.seed_brown
            SproutState.SPROUTING -> R.color.sprout_green
            SproutState.GROWING -> R.color.leaf_green
            SproutState.BLOOMING -> R.color.flower_pink
        }
    }

    fun getSproutStateDescription(state: SproutState): String {
        return when (state) {
            SproutState.DORMANT -> "씨앗이 땅속에서 잠들어 있어요"
            SproutState.SEED -> "씨앗이 심어졌어요"
            SproutState.SPROUTING -> "새싹이 돋아나고 있어요"
            SproutState.GROWING -> "무럭무럭 자라고 있어요"
            SproutState.BLOOMING -> "아름다운 꽃이 피었어요"
        }
    }
}
