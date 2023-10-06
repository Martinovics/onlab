package com.onlab.oauth.interfaces

interface IViewItemClickedListener {
    fun onItemClicked(position: Int): Unit
    fun onItemLongClicked(position: Int): Unit
}