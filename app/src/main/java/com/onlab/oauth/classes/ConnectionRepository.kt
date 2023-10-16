package com.onlab.oauth.classes

import com.onlab.oauth.interfaces.IConnectionService


object ConnectionRepository {
    private val connections: MutableMap<String, IConnectionService> = mutableMapOf()

    val registeredEntries: Set<Map.Entry<String, IConnectionService>>
        get() {
            return connections.entries
        }

    fun register(key: String, connectionService: IConnectionService): IConnectionService {
        connections[key] = connectionService
        return connections[key]!!
    }

    fun remove(key: String): Boolean {
        return connections.remove(key) != null
    }

    fun get(key: String): IConnectionService? {
        return connections[key]
    }
}