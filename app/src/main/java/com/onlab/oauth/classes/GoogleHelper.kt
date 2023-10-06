package com.onlab.oauth.classes

import android.app.Activity
import android.content.Context
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
import com.onlab.oauth.R


object GoogleHelper {

    private var gso: GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE))
            .build()

    val signInOptions: GoogleSignInOptions
        get() {
            return gso
        }

    fun getSignInClient(activity: Activity): GoogleSignInClient {
        return GoogleSignIn.getClient(activity, this.signInOptions)
    }

    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun getDriveService(context: Context): Drive {
        val account = this.getLastSignedInAccount(context)
                      ?: throw java.lang.Exception("Account was null. Sign in first.")

        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account!!

        return Drive.Builder(
            //AndroidHttp.newCompatibleTransport(),
            NetHttpTransport(),
            //JacksonFactory.getDefaultInstance(),
            GsonFactory.getDefaultInstance(),
            credential
        )
        .setApplicationName("Szakdolgozat")
        .build()
    }
}