package com.example.anhquoc.mycustom

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_layout.view.*

class MainAdapter : RecyclerView.Adapter<MainAdapter.ItemViewHolder>() {

    private val mList = mutableListOf<Profile>()

    private var mListener: OnClickListener? = null

    fun setList(list: List<Profile>) {
        mList.clear()
        list.isNotEmpty().let {
            mList.addAll(list)
        }
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: OnClickListener?) {
        mListener = listener
    }

    override fun onCreateViewHolder(p0: ViewGroup, i: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.list_item_layout, p0, false))
    }

    override fun onBindViewHolder(itemViewHolder: ItemViewHolder, i: Int) {
        itemViewHolder.bindView(mList[i])
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        fun bindView(item: Profile) {
            itemView.itemName.text = item.name
            itemView.itemEmail.text = item.email
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            mListener?.onItemClick(mList[adapterPosition])
        }
    }

    interface OnClickListener {
        fun onItemClick(item: Profile)
    }
}
