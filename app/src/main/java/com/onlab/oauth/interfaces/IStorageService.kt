package com.onlab.oauth.interfaces

import java.io.InputStream


interface IStorageService {
    suspend fun listDir(directoryID: String): List<IStorageContent>?
    suspend fun getCustomRootFolder(): IStorageContent?
    suspend fun createDir(parentDirectoryId: String, directoryName: String): IStorageContent?
    suspend fun removeContent(contentId: String): Boolean
    suspend fun uploadFile(inputStream: InputStream, mimeType: String, parentFolderId: String, fileName: String): IStorageContent?
    suspend fun downloadFile(fileId: String): InputStream?
}