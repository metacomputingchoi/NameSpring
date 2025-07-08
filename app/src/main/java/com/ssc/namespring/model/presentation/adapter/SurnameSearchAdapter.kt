// model/presentation/adapter/SurnameSearchAdapter.kt
package com.ssc.namespring.model.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssc.namespring.R
import com.ssc.namespring.model.domain.entity.SurnameSearchResult

class SurnameSearchAdapter(
    private val onItemClick: (SurnameSearchResult) -> Unit
) : RecyclerView.Adapter<SurnameSearchAdapter.SurnameViewHolder>() {

    private var results = listOf<SurnameSearchResult>()
    var onItemSelected: (() -> Unit)? = null

    fun submitList(list: List<SurnameSearchResult>) {
        results = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurnameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_surname_search, parent, false)
        return SurnameViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurnameViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount() = results.size

    inner class SurnameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvKorean: TextView = itemView.findViewById(R.id.tvKorean)
        private val tvHanja: TextView = itemView.findViewById(R.id.tvHanja)
        private val tvMeaning: TextView = itemView.findViewById(R.id.tvMeaning)

        fun bind(result: SurnameSearchResult) {
            tvKorean.text = result.korean
            tvHanja.text = result.hanja
            tvMeaning.text = result.meaning ?: ""

            if (result.isCompound) {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.sunny_spring_bg))
            } else {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.white))
            }

            itemView.setOnClickListener {
                onItemClick(result)
                onItemSelected?.invoke()
            }
        }
    }
}