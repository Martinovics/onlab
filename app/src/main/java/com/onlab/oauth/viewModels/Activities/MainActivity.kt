package com.onlab.oauth.viewModels.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.api.services.drive.Drive
import com.onlab.oauth.R
import com.onlab.oauth.adapters.ContentBrowserAdapter
import com.onlab.oauth.classes.StorageRepository
import com.onlab.oauth.databinding.ActivityMainBinding
import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.enums.StorageSource
import com.onlab.oauth.interfaces.*
import com.onlab.oauth.models.StorageContent
import com.onlab.oauth.services.DriveService
import com.onlab.oauth.services.GoogleConnectionService
import com.onlab.oauth.viewModels.Fragments.AddContentBottomSheetFragment
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), IViewItemClickedListener, IAddDirectoryDialogListener {

    private var TAG = this::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ContentBrowserAdapter
    private lateinit var googleConnectionService: IConnectionService
    private var folderHistory = mutableListOf<ICloudStorageContent>(
        StorageContent("Root", "root", ContentType.DIRECTORY, StorageSource.GOOGLE_DRIVE)  // ezt ki kell cserélni, mert a root az mind a 3 source
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)

        this.googleConnectionService = GoogleConnectionService(this)

        initToolbar()
        initRecycleView()
        initNavigationView()
        initStorages()
        initAddContentBottomSheet()
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // when drawer is opened -> close
        if (this.binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.binding.drawerLayout.closeDrawer(GravityCompat.START)
            return
        }

        // go back in the folder history
        if (1 < folderHistory.count()) {
            folderHistory.removeLast()
            navigateToFolder(folderHistory.last())
            return
        }

        onBackPressedDispatcher.onBackPressed()
    }

    private fun initToolbar() {
        setContentView(this.binding.root)
        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.title = ""
        this.binding.toolbar.btnToolbarHamburgerMenu.setOnClickListener {
            this.binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        this.binding.toolbar.btnDelete.setOnClickListener {
            this.binding.toolbar.btnDelete.visibility = View.GONE
        }
    }

    private fun initStorages() {
        if (this.googleConnectionService.isLoggedIn()) {
            val storage = StorageRepository.registerStorage(
                StorageSource.GOOGLE_DRIVE.toString(),
                DriveService(googleConnectionService.getCloudStorage() as Drive)
            )
            listDir(storage, "root")
        }
    }

    private fun initAddContentBottomSheet() {
        this.binding.btnAddContent.setOnClickListener {
            val bottomSheet = AddContentBottomSheetFragment(this)
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
    }

    private fun listDir(storage: ICloudStorage, directoryID: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val contents = storage.listDir(directoryID)
            this@MainActivity.adapter.addRange(contents)
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

        StorageRepository.registerStorage(
            "GoogleDrive", DriveService(googleConnectionService.getCloudStorage() as Drive)
        )
    }

    private fun googleDisconnectedCallback(menuItem: MenuItem) {
        menuItem.title = "Connect to Google Drive"
        Toast.makeText(this, "Disconnected from Google Drive", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToFolder(storageItem: ICloudStorageContent) {
        if (storageItem.type == ContentType.DIRECTORY) {
            this.binding.toolbar.tvToolbarCurrentFolder.text = storageItem.name
            if (folderHistory.last().id != storageItem.id) {
                folderHistory.add(storageItem)
            }
            this.adapter.clear()
            listDir(StorageRepository.getStorage(storageItem.source.toString())!!, storageItem.id)
        } else {
            return
        }
    }

    override fun onItemClicked(position: Int) {
        this.binding.toolbar.btnDelete.visibility = View.GONE
        navigateToFolder(this.adapter.getItemAt(position))
    }

    override fun onItemLongClicked(position: Int): Boolean {
        Toast.makeText(this, "Item at $position long-clicked", Toast.LENGTH_LONG).show()
        this.binding.toolbar.btnDelete.visibility = View.VISIBLE

        val storageContent = this.adapter.getItemAt(position)
        this.binding.toolbar.btnDelete.setOnClickListener {
            val service = StorageRepository.getStorage(storageContent.source.toString())
            if (service == null) {
                Log.d(TAG, "Storage service was null. Source: ${storageContent.source}")
            }
            CoroutineScope(Dispatchers.Main).launch {
                val isRemoved = service!!.removeContent(storageContent.id)
                if (isRemoved) {
                    this@MainActivity.adapter.removeAt(position)
                    this@MainActivity.binding.toolbar.btnDelete.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Removed ${storageContent.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "Could not delete content. Name: ${storageContent.name}")
                }
            }
        }
        return true
    }

    override fun onAddDirectoryDialogPositiveClicked(directoryName: String) {
        val currentDirectory = folderHistory.last()
        val storageService = StorageRepository.getStorage(currentDirectory.source.toString())
        CoroutineScope(Dispatchers.Main).launch {
            val newDirectory = storageService?.createDir(currentDirectory.id, directoryName)
            if (newDirectory != null) {
                this@MainActivity.adapter.add(newDirectory)
            }
        }
    }

    override fun onAddDirectoryDialogNegativeClicked() {
        // nem kell csinálni semmit
    }
}