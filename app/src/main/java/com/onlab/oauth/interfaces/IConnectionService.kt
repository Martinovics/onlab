package com.onlab.oauth.interfaces

import com.onlab.oauth.enums.ContentSource

interface IConnectionService {
    val title: String
    val menuItemId: Int
    val source: ContentSource
    fun isLoggedIn(): Boolean
    fun signIn(callback: ICallback)
    fun signOut(callback: ICallback)
    fun getStorage(): IStorageService?
}