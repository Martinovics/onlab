package com.onlab.oauth.models

import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.enums.StorageSource
import com.onlab.oauth.interfaces.ICloudStorageContent

data class StorageContent(
    override val name: String,
    override val id: String,
    override val type: ContentType,
    override val source: StorageSource
) : ICloudStorageContent