package com.onlab.oauth.interfaces

interface IRecyclerItemClickedListener {
    fun onItemClicked(position: Int): Unit
    fun onItemLongClicked(position: Int): Boolean
}