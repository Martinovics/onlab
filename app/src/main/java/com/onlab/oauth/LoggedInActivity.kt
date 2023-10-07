package com.onlab.oauth

import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.onlab.oauth.adapters.ContentBrowserAdapter
import com.onlab.oauth.classes.GoogleHelper
import com.onlab.oauth.databinding.ActivityLoggedInBinding
import com.onlab.oauth.interfaces.IConnectionService
import com.onlab.oauth.interfaces.IViewItemClickedListener
import com.onlab.oauth.services.DriveService
import com.onlab.oauth.services.GoogleConnectionService
import java.util.Dictionary

class LoggedInActivity : AppCompatActivity(), IViewItemClickedListener {

    private var TAG = this::class.java.simpleName
    private lateinit var binding: ActivityLoggedInBinding
    private lateinit var adapter: ContentBrowserAdapter
    private lateinit var googleConnectionService: IConnectionService
    private lateinit var loggedIn: Dictionary<String, Boolean>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.googleConnectionService = GoogleConnectionService(this)
        this.binding = ActivityLoggedInBinding.inflate(layoutInflater)

        this.binding.btnTest.setOnClickListener { this.testFeature() }

        initRecycleView()

        setContentView(this.binding.root)

        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.title = ""

        this.binding.toolbar.btnToolbarHamburgerMenu.setOnClickListener {
            this.binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        initNavigationView()

    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this.binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }


    private fun initRecycleView() {
        this.adapter = ContentBrowserAdapter(this)
        this.binding.rvContents.adapter = this.adapter
        this.binding.rvContents.layoutManager = LinearLayoutManager(this)
    }


    private fun initNavigationView() {

        // check google connection | set texts
        val drawerMenuItem = this.binding.drawerMenu.menu.findItem(R.id.drawerMenuGoogleDrive)
        if (this.googleConnectionService.isLoggedIn()) {
            drawerMenuItem.title = "Disconnect Google Drive"
        } else {
            drawerMenuItem.title = "Connect Google Drive"
        }

        // check OneDrive connection

        // check Dropbox connection

        this.binding.drawerMenu.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.drawerMenuGoogleDrive -> {
                    Log.d(TAG, "${menuItem.title} clicked")
                    if (googleConnectionService.isLoggedIn()) {
                        this.googleConnectionService.signOut(
                            callback_success = { googleDisconnectedCallback(menuItem) },
                            callback_fail = { Toast.makeText(this, "Disconnect failed", Toast.LENGTH_SHORT).show() })
                    } else {
                        this.googleConnectionService.signIn(
                            callback_success = { googleConnectedCallback(menuItem) },
                            callback_fail = { Toast.makeText(this, "Connect failed", Toast.LENGTH_SHORT).show() })
                    }
                    true
                }
                R.id.drawerMenuOneDrive -> {
                    Log.d(TAG, "${menuItem.title} clicked")
                    true
                }
                R.id.drawerMenuDropBox -> {
                    Log.d(TAG, "${menuItem.title} clicked")
                    true
                }
                else -> {
                    Log.d(TAG, "Unknown drawer item clicked")
                    false
                }
            }
        }
    }

    private fun googleConnectedCallback(menuItem: MenuItem) {
        menuItem.title = "Disconnect from Google Drive"
        Toast.makeText(this, "Connected to Google Drive", Toast.LENGTH_SHORT).show()
    }

    private fun googleDisconnectedCallback(menuItem: MenuItem) {
        menuItem.title = "Connect to Google Drive"
        Toast.makeText(this, "Disconnected from Google Drive", Toast.LENGTH_SHORT).show()
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