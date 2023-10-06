package com.onlab.oauth.interfaces


interface ICloudStorage {
    fun listDir(directoryID: String): Unit
}