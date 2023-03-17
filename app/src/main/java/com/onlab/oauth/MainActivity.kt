package com.onlab.oauth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.onlab.oauth.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {


    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = ActivityMainBinding.inflate(layoutInflater)



        this.gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                   .requestEmail()
                   .build()
        this.gsc = GoogleSignIn.getClient(this, this.gso)

        this.signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleSignInResult(result.data)
            }
        }

        // todo check for existing user signed in

        this.binding.btnSignIn.setOnClickListener { this.signIn() }
        this.binding.btnSignOut.setOnClickListener { this.signOut() }

        setContentView(this.binding.root)
    }





    private fun handleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.

            this.binding.tvGreet.text = "Hello, ${account.displayName}!"

            Log.d(TAG, "Name: $account.displayName")
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }


    private fun signIn() {
        val signInIntent = this.gsc.signInIntent
        this.signInLauncher.launch(signInIntent)
    }



    private fun handleSignOut(task: Task<Void>) {
        if (task.isSuccessful) {
            // Sign-out succeeded, update UI and launch the login activity
            // TODO: update UI and launch login activity
            this.binding.tvGreet.text = "Hello, World!"
            Log.d(TAG, "Sign-out succeeded")
        } else {
            // Sign-out failed, display an error message
            Log.w(TAG, "Sign-out failed")
        }
    }



    private fun signOut() {
        this.gsc.signOut().addOnCompleteListener(this) { task ->
            this.handleSignOut(task)
        }
    }




}