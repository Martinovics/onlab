package com.onlab.oauth.interfaces


interface ICloudStorage {
    suspend fun listDir(directoryID: String): List<ICloudStorageContent>
    suspend fun createDir(parentDirectoryId: String, directoryName: String): ICloudStorageContent?
    suspend fun removeContent(contentId: String): Boolean
    // suspend fun uploadFile(): Unit
}