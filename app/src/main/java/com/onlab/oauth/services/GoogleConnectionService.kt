package com.onlab.oauth.services

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.onlab.oauth.R
import com.onlab.oauth.classes.GoogleHelper
import com.onlab.oauth.enums.ContentSource
import com.onlab.oauth.interfaces.ICallback
import com.onlab.oauth.interfaces.IConnectionService
import com.onlab.oauth.interfaces.IStorageService


class GoogleConnectionService(private val activity: AppCompatActivity) : IConnectionService {

    private val tag = "GoogleConnectionService"
    private val helper: GoogleHelper = GoogleHelper(activity)
    private val gsc = helper.getSignInClient()

    private lateinit var loginCallback: ICallback
    private val signInLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleSignIn(result.data)
        }
    }

    override val title: String
        get() = "Google Drive"

    override val menuItemId: Int
        get() = R.id.drawerMenuGoogleDrive

    override val source: ContentSource
        get() = ContentSource.GOOGLE_DRIVE

    override fun isLoggedIn(): Boolean {
        return helper.isLoggedIn()
    }

    override fun signIn(callback: ICallback) {
        this.loginCallback = callback
        this.signInLauncher.launch(this.gsc.signInIntent)
    }

    private fun handleSignIn(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d(tag, "Logged to google as ${account.displayName}")
            loginCallback.onSuccess()
        } catch (e: ApiException) {
            Log.w(tag, "Failed to login to google: err=" + e.statusCode)
            loginCallback.onFailure()
        }
    }

    override fun signOut(callback: ICallback) {
        this.gsc.signOut().addOnCompleteListener(activity) { task ->
            this.handleSignOut(task, callback)
        }
    }

    private fun handleSignOut(task: Task<Void>, callback: ICallback) {
        if (task.isSuccessful) {
            Log.d(tag, "Signed-out from google")
            callback.onSuccess()
        } else {
            Log.w(tag, "Sign-out from google failed")
            callback.onFailure()
        }
    }

    override fun getStorage(): IStorageService? {
        val drive = helper.getDriveService() ?: return null
        return GoogleDriveService(drive)
    }
}