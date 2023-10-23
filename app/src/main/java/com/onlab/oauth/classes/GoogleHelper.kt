package com.onlab.oauth.classes

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes


class GoogleHelper(private val activity: Activity) {

    private var gso: GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE))
            .build()

    val signInOptions: GoogleSignInOptions
        get() {
            return gso
        }

    fun getSignInClient(): GoogleSignInClient {
        return GoogleSignIn.getClient(this.activity, this.signInOptions)
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(this.activity)
    }

    fun isLoggedIn(): Boolean {
        return this.getLastSignedInAccount() != null
    }

    fun getDriveService(): Drive? {
        val account = this.getLastSignedInAccount() ?: return null

        val credential = GoogleAccountCredential.usingOAuth2(
            this.activity, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account!!

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Szakdolgozat")
            .build()
    }
}