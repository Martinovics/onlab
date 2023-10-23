package com.onlab.oauth.interfaces

import com.onlab.oauth.adapters.ContentBrowserAdapter
import com.onlab.oauth.databinding.ActivityMainBinding

interface IMainActivityUIManager {
    val binding: ActivityMainBinding
    val contentList: ContentBrowserAdapter
    fun makeToast(message: String)
}