package com.onlab.oauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.services.drive.DriveScopes
import com.onlab.oauth.classes.SignInHelper
import com.onlab.oauth.databinding.ActivityLoggedInBinding

class LoggedInActivity : AppCompatActivity() {

    private var TAG = this::class.java.simpleName
    private lateinit var binding: ActivityLoggedInBinding
    private lateinit var gsc: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityLoggedInBinding.inflate(layoutInflater)

        this.gsc = SignInHelper.getSignInClient(this)
        val account = SignInHelper.getLastSignedInAccount(this)!!  // sosem kéne null-nak lennie, mert csak akkor jövünk át, amikor már beléptünk

        this.binding.tvLoggedInGreet.text = "Logged in as ${account.displayName}"
        this.binding.btnSignOut.setOnClickListener { this.signOut() }

        setContentView(this.binding.root)
    }


    private fun switchToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }


    private fun signOut() {
        this.gsc.signOut().addOnCompleteListener(this) { task ->
            this.handleSignOut(task)
        }
    }


    private fun handleSignOut(task: Task<Void>) {
        if (task.isSuccessful) {  // a sign-out sikeres volt
            Log.d(TAG, "Sign-out succeeded")
            this.switchToMainActivity()
        } else {
            Log.w(TAG, "Sign-out failed")
        }
    }
}