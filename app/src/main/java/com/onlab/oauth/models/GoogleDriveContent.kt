package com.onlab.oauth.models

import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.enums.ContentSource
import com.onlab.oauth.interfaces.IStorageContent

class GoogleDriveContent(
    override val name: String,
    override val id: String,
    private val contentType: Any
) : IStorageContent {

    override val source = ContentSource.GOOGLE_DRIVE

    override val type: ContentType = when (contentType) {
        is String -> stringToContentType(contentType)
        is ContentType -> contentType
        else -> ContentType.OTHER
    }

    // https://developers.google.com/drive/api/guides/mime-types
    private fun stringToContentType(contentType: String): ContentType {
        if (contentType.endsWith("folder"))
            return ContentType.DIRECTORY
        if (contentType.endsWith("shortcut") || contentType.endsWith("drive-sdk"))
            return ContentType.OTHER
        return ContentType.FILE
    }
}