package com.onlab.oauth.interfaces

import com.onlab.oauth.enums.ContentSource

interface IConnectionService {
    val title: String
    val menuItemId: Int
    val source: ContentSource
    fun isLoggedIn(): Boolean
    fun signIn(callback_success: (() -> Unit)?, callback_fail: (() -> Unit)?)
    fun signOut(callback_success: (() -> Unit)?, callback_fail: (() -> Unit)?)
    fun getStorage(): IStorageService?
}