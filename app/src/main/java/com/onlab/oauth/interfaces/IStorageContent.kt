package com.onlab.oauth.interfaces

import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.enums.ContentSource

interface IStorageContent {
    val name: String
    val id: String
    val type: ContentType
    val source: ContentSource
}