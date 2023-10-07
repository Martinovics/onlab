package com.onlab.oauth.classes

import com.onlab.oauth.interfaces.ICloudStorage


object StorageRepository {
    private val storages: MutableMap<String, ICloudStorage> = mutableMapOf()

    fun registerStorage(key: String, storage: ICloudStorage): ICloudStorage {
        storages[key] = storage
        return storages[key]!!
    }

    fun getStorage(key: String): ICloudStorage? {
        return storages[key]
    }

    fun getRegisteredStorageKeys(): MutableIterable<String> {
        return storages.keys
    }
}