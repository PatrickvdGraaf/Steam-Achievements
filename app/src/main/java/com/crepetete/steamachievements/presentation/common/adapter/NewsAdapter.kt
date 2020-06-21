package com.crepetete.steamachievements.presentation.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.presentation.common.adapter.callback.OnNewsItemClickListener
import com.crepetete.steamachievements.presentation.common.adapter.viewholder.NewsViewHolder

/**
 * ListAdapter for a list of [NewsItem]s.
 *
 * @author: Patrick van de Graaf.
 * @date: Wed 11 Dec, 2019; 13:26.
 */
class NewsAdapter(private val newsSelectionListener: OnNewsItemClickListener) :
    RecyclerView.Adapter<NewsViewHolder>() {
    private var items = listOf<NewsItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_holder_news, parent, false)
        return NewsViewHolder(view, newsSelectionListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun setItems(newItems: List<NewsItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}