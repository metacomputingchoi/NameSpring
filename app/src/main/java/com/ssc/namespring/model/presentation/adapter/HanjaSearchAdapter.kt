// model/presentation/adapter/HanjaSearchAdapter.kt
package com.ssc.namespring.model.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.HanjaSearchResult

class HanjaSearchAdapter(
    private val onItemClick: (HanjaSearchResult) -> Unit
) : RecyclerView.Adapter<HanjaSearchAdapter.HanjaViewHolder>() {

    private var results = listOf<HanjaSearchResult>()
    var onItemSelected: (() -> Unit)? = null

    init {
        // SurnameViewHolder 재활용 향상을 위한 고정 ID 설정
        setHasStableIds(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<HanjaSearchResult>) {
        // 리스트가 같으면 업데이트하지 않음
        if (results == list) return

        results = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HanjaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hanja_search, parent, false)
        return HanjaViewHolder(view)
    }

    override fun onBindViewHolder(holder: HanjaViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount() = results.size

    override fun getItemId(position: Int): Long {
        // tripleKey를 기반으로 안정적인 ID 생성
        return results[position].tripleKey.hashCode().toLong()
    }

    inner class HanjaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHanja: TextView = itemView.findViewById(R.id.tvHanja)
        private val tvKorean: TextView = itemView.findViewById(R.id.tvKorean)
        private val tvMeaning: TextView = itemView.findViewById(R.id.tvMeaning)
        private val tvOhaeng: TextView = itemView.findViewById(R.id.tvOhaeng)
        private val tvStrokes: TextView = itemView.findViewById(R.id.tvStrokes)
        private val tvSoundCount: TextView = itemView.findViewById(R.id.tvSoundCount)

        @SuppressLint("SetTextI18n")
        fun bind(result: HanjaSearchResult) {
            tvHanja.text = result.hanja
            tvKorean.text = result.korean
            tvMeaning.text = result.meaning ?: ""
            tvOhaeng.text = result.ohaeng
            tvStrokes.text = "${result.strokes}획"

            val ohaengColor = when(result.ohaeng) {
                "木" -> R.color.ohaeng_wood
                "火" -> R.color.ohaeng_fire
                "土" -> R.color.ohaeng_earth
                "金" -> R.color.ohaeng_metal
                "水" -> R.color.ohaeng_water
                else -> R.color.text_secondary
            }
            tvOhaeng.setTextColor(itemView.context.getColor(ohaengColor))

            if (result.soundCount > 1) {
                tvSoundCount.visibility = View.VISIBLE
                tvSoundCount.text = "(다음 ${result.soundCount})"
            } else {
                tvSoundCount.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(result)
                onItemSelected?.invoke()
            }
        }
    }
}