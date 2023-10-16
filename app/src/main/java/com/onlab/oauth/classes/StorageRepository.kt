package com.onlab.oauth.classes

import com.onlab.oauth.interfaces.IStorageService


object StorageRepository {
    private val storages: MutableMap<String, IStorageService> = mutableMapOf()

    val registeredEntries: Set<Map.Entry<String, IStorageService>>
        get() {
            return storages.entries
        }

    fun register(key: String, storage: IStorageService): IStorageService {
        storages[key] = storage
        return storages[key]!!
    }

    fun remove(key: String): Boolean {
        return storages.remove(key) != null
    }

    fun get(key: String): IStorageService? {
        return storages[key]
    }
}