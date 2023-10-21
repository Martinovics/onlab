package com.onlab.oauth.interfaces

interface IRecyclerItemClickedListener {
    fun onItemClicked(position: Int)
    fun onItemLongClicked(position: Int): Boolean
    fun onMoreClicked(position: Int)
}