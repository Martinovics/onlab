package com.onlab.oauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.onlab.oauth.adapters.ContentBrowserAdapter
import com.onlab.oauth.classes.GoogleHelper
import com.onlab.oauth.databinding.ActivityLoggedInBinding
import com.onlab.oauth.interfaces.IViewItemClickedListener
import com.onlab.oauth.services.DriveService

class LoggedInActivity : AppCompatActivity(), IViewItemClickedListener {

    private var TAG = this::class.java.simpleName
    private lateinit var binding: ActivityLoggedInBinding
    private lateinit var gsc: GoogleSignInClient
    private lateinit var adapter: ContentBrowserAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityLoggedInBinding.inflate(layoutInflater)

        this.gsc = GoogleHelper.getSignInClient(this)
        val account = GoogleHelper.getLastSignedInAccount(this)!!

        this.binding.tvLoggedInGreet.text = "Logged in as ${account.displayName}"
        this.binding.btnSignOut.setOnClickListener { this.signOut() }
        this.binding.btnTest.setOnClickListener { this.testFeature() }

        initRecycleView()

        setContentView(this.binding.root)
    }


    private fun initRecycleView() {
        this.adapter = ContentBrowserAdapter(this)
        this.binding.rvContents.adapter = this.adapter
        this.binding.rvContents.layoutManager = LinearLayoutManager(this)
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


    private fun testFeature() {
        val drive = GoogleHelper.getDriveService(this)
        val driveService = DriveService(drive)
        driveService.listDir("root")
        driveService.listDir("144TQU7FoU6m7Y2ZW1GM7ZbqRnnipupLj")
    }


    override fun onItemClicked(position: Int) {
        Toast.makeText(this, "Item at $position clicked", Toast.LENGTH_LONG).show()
    }

    override fun onItemLongClicked(position: Int) {
        Toast.makeText(this, "Item at $position long-clicked", Toast.LENGTH_LONG).show()

    }
}