package com.onlab.oauth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
                handleSignIn(result.data)
            }
        }


        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {  // már be van jelentkezve
            switchToLoggedInActivity()
        }


        this.binding.btnSignIn.setOnClickListener { this.signIn() }

        setContentView(this.binding.root)
    }




    private fun switchToLoggedInActivity() {
        val intent = Intent(this, LoggedInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }




    private fun handleSignIn(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)

            Log.d(TAG, "Logged in as ${account.displayName}")
            this.switchToLoggedInActivity()
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }




    private fun signIn() {
        val signInIntent = this.gsc.signInIntent
        this.signInLauncher.launch(signInIntent)
    }


}