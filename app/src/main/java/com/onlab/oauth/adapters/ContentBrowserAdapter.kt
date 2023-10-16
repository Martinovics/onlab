package com.onlab.oauth.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlab.oauth.databinding.ContentBrowserItemBinding
import com.onlab.oauth.interfaces.IStorageContent
import com.onlab.oauth.interfaces.IRecyclerItemClickedListener


class ContentBrowserAdapter(private val listener: IRecyclerItemClickedListener) : RecyclerView.Adapter<ContentBrowserAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(val binding: ContentBrowserItemBinding) : RecyclerView.ViewHolder(binding.root)

    private var _contents = mutableListOf<IStorageContent>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        ContentBrowserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val content = this._contents[position]

        holder.binding.tvTitle.text = content.name
        holder.binding.tvDescription.text = content.type.toString()

        holder.binding.root.setOnClickListener { listener.onItemClicked(position) }
        holder.binding.root.setOnLongClickListener { listener.onItemLongClicked(position) }
    }

    override fun getItemCount(): Int {
        return this._contents.size
    }


    fun add(content: IStorageContent) {
        this._contents.add(content)
        notifyItemInserted(itemCount - 1)
    }

    fun addRange(contents: List<IStorageContent>) {
        if (contents.isEmpty()) {
            return
        }
        if (contents.size == 1) {
            this.add(contents[0])
            return
        }

        val positionFrom = this.itemCount - 1
        this._contents.addAll(contents)
        notifyItemRangeInserted(positionFrom, this.itemCount - 1)
    }

    fun removeAt(position: Int) {
        this._contents.removeAt(position)
        notifyItemRemoved(position)
    }

    fun clear() {
        val size = this._contents.size
        this._contents.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun getItemAt(position: Int): IStorageContent {
        return this._contents[position]
    }
}