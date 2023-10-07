package com.onlab.oauth.interfaces


interface ICloudStorage {
    suspend fun listDir(directoryID: String): List<ICloudStorageContent>
}