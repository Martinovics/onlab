package com.onlab.oauth.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlab.oauth.databinding.ContentBrowserItemBinding
import com.onlab.oauth.interfaces.IStorageContent
import com.onlab.oauth.interfaces.IViewItemClickedListener


class ContentBrowserAdapter(private val onClickListener: IViewItemClickedListener) : RecyclerView.Adapter<ContentBrowserAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(val binding: ContentBrowserItemBinding) : RecyclerView.ViewHolder(binding.root)

    private var items = mutableListOf<IStorageContent>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        ContentBrowserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = this.items[position]

        holder.binding.tvTitle.text = item.name
        holder.binding.tvDescription.text = item.type.toString()

        holder.binding.root.setOnClickListener { onClickListener.onItemClicked(position) }
        holder.binding.root.setOnLongClickListener { onClickListener.onItemLongClicked(position) }
    }


    override fun getItemCount(): Int {
        return this.items.size
    }


    fun add(item: IStorageContent) {
        this.items.add(item)
        notifyItemInserted(itemCount - 1)
    }


    fun addRange(items: List<IStorageContent>) {
        if (items.isEmpty()) {
            return
        }
        if (items.size == 1) {
            this.add(items[0])
            return
        }

        val positionFrom = this.itemCount - 1
        this.items.addAll(items)
        notifyItemRangeInserted(positionFrom, this.itemCount - 1)
    }


    fun removeAt(position: Int) {
        this.items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun clear() {
        val size = this.items.size
        this.items.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun getItemAt(position: Int): IStorageContent {
        return this.items[position]
    }
}