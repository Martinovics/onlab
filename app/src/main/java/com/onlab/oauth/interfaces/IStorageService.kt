package com.onlab.oauth.interfaces


interface IStorageService {
    suspend fun listDir(directoryID: String): List<IStorageContent>?
    suspend fun getCustomRootFolder(): IStorageContent?
    suspend fun createDir(parentDirectoryId: String, directoryName: String): IStorageContent?
    suspend fun removeContent(contentId: String): Boolean
    // suspend fun uploadFile(): Unit
}