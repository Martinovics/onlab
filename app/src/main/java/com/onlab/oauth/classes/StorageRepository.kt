package com.onlab.oauth.classes

import com.onlab.oauth.interfaces.IStorageService


object StorageRepository {
    private val storages: MutableMap<String, IStorageService> = mutableMapOf()

    fun registerStorage(key: String, storage: IStorageService): IStorageService {
        storages[key] = storage
        return storages[key]!!
    }

    fun getStorage(key: String): IStorageService? {
        return storages[key]
    }

    fun getRegisteredStorageKeys(): MutableIterable<String> {
        return storages.keys
    }
}