package com.onlab.oauth.interfaces

interface IConnectionService {
    fun isLoggedIn(): Boolean
    fun signIn(callback_success: (() -> Unit)?, callback_fail: (() -> Unit)?)
    fun signOut(callback_success: (() -> Unit)?, callback_fail: (() -> Unit)?)
    fun getCloudStorage(): Any
}