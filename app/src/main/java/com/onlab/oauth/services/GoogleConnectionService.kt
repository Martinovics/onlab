package com.onlab.oauth.services

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.onlab.oauth.classes.GoogleHelper2
import com.onlab.oauth.interfaces.IConnectionService


class GoogleConnectionService(private val activity: AppCompatActivity) : IConnectionService {

    private val Tag = "GoogleConnectionService"
    private val helper: GoogleHelper2 = GoogleHelper2(activity)
    private val gsc = helper.getSignInClient()

    private var loginCallbackSuccess: (() -> Unit)? = null
    private var logoutCallbackFail: (() -> Unit)? = null
    private val signInLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleSignIn(result.data)
        }
    }

    override fun isLoggedIn(): Boolean {
        return helper.isLoggedIn()
    }

    override fun signIn(callback_success: (() -> Unit)?, callback_fail: (() -> Unit)?) {
        this.loginCallbackSuccess = callback_success
        this.logoutCallbackFail = callback_fail
        this.signInLauncher.launch(this.gsc.signInIntent)
    }

    private fun handleSignIn(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d(Tag, "Logged to google as ${account.displayName}")
            this.loginCallbackSuccess?.invoke()
        } catch (e: ApiException) {
            Log.w(Tag, "Failed to login to google: err=" + e.statusCode)
            this.logoutCallbackFail?.invoke()
        }
    }

    override fun signOut(callback_success: (() -> Unit)?, callback_fail: (() -> Unit)?) {
        this.gsc.signOut().addOnCompleteListener(activity) { task ->
            this.handleSignOut(task, callback_success, callback_fail)
        }
    }

    private fun handleSignOut(task: Task<Void>, callback_success: (() -> Unit)?, callback_fail: (() -> Unit)?) {
        if (task.isSuccessful) {
            Log.d(Tag, "Signed-out from google")
            callback_success?.invoke()
        } else {
            Log.w(Tag, "Sign-out from google failed")
            callback_fail?.invoke()
        }
    }
}