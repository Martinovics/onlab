package com.onlab.oauth.interfaces

import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.enums.ContentSource

interface IStorageContent {
    val id: String
    val name: String
    val keyAlias: String
    val type: ContentType
    val source: ContentSource
}