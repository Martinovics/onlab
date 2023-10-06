package com.onlab.oauth.interfaces

import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.enums.StorageSource

interface ICloudStorageContent {
    val name: String
    val id: String
    val type: ContentType
    val source: StorageSource
}