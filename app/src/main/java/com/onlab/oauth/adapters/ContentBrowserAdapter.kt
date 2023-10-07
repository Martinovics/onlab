package com.onlab.oauth.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlab.oauth.databinding.ContentBrowserItemBinding
import com.onlab.oauth.interfaces.ICloudStorageContent
import com.onlab.oauth.interfaces.IViewItemClickedListener
import com.onlab.oauth.models.GoogleDriveContent


class ContentBrowserAdapter(private val onClickListener: IViewItemClickedListener) : RecyclerView.Adapter<ContentBrowserAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(val binding: ContentBrowserItemBinding) : RecyclerView.ViewHolder(binding.root)

    private var items = mutableListOf<ICloudStorageContent>(
//        GoogleDriveContent("name1", "id1", "folder"),
//        GoogleDriveContent("name2", "id2", "folder"),
//        GoogleDriveContent("name3", "id3", "file"),
//        GoogleDriveContent("name4", "id4", "file"),
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        ContentBrowserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = this.items[position]

        holder.binding.tvTitle.text = item.name
        holder.binding.tvDescription.text = item.type.toString()

        holder.binding.root.setOnClickListener { onClickListener.onItemClicked(position) }
    }


    override fun getItemCount(): Int {
        return this.items.size
    }


    fun add(item: ICloudStorageContent): Unit {
        this.items.add(item)
        notifyItemInserted(itemCount - 1)
    }


    fun addRange(items: List<ICloudStorageContent>): Unit {
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

    fun getItemAt(position: Int): ICloudStorageContent {
        return this.items[position]
    }
}