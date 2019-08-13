package com.example.anhquoc.mycustom

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_layout.view.*

/**
 * Copyright (C) 2019, VNG Corporation.
 * Created by quocha2
 * On 06/08/2019
 */
class ListAdapter(private val numColumn: Int) : RecyclerView.Adapter<ListAdapter.ItemViewHolder>() {

    private val items = mutableListOf<String>()

    private var rightSpace: Int = 50

    private lateinit var layoutManager: LinearLayoutManager

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        layoutManager = recyclerView.layoutManager as LinearLayoutManager
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.list_item_layout, p0, false)

        (view.layoutParams as RecyclerView.LayoutParams).apply {
            layoutManager.let {
                width = it.width - rightSpace - marginStart - marginEnd
            }
        }
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindView(items[position])
    }

    fun setData(data: List<String>) {
        items.clear()
        if (!data.isNullOrEmpty()) {
            items.addAll(data)
        }
        notifyDataSetChanged()
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(text: String) {
            itemView.tv.text = text
        }
    }
}