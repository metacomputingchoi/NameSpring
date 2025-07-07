// ui/compare/adapter/CompareResultPagerAdapter.kt
package com.ssc.namespring.ui.compare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.ssc.namespring.R
import com.ssc.namespring.model.data.repository.FavoriteName

class CompareResultPagerAdapter(
    private val comparisonList: List<FavoriteName>
) : RecyclerView.Adapter<CompareResultPagerAdapter.PageViewHolder>() {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_compare_result, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(comparisonList[position])
    }

    override fun getItemCount(): Int = comparisonList.size

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJsonContent: TextView = itemView.findViewById(R.id.tvJsonContent)

        fun bind(favorite: FavoriteName) {
            // JSON 데이터를 보기 좋게 포맷팅
            val formattedJson = try {
                val jsonElement = gson.fromJson(favorite.jsonData, com.google.gson.JsonElement::class.java)
                gson.toJson(jsonElement)
            } catch (e: Exception) {
                favorite.jsonData
            }

            tvJsonContent.text = formattedJson
        }
    }
}