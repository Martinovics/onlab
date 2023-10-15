package com.onlab.oauth.viewModels.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.api.services.drive.Drive
import com.onlab.oauth.R
import com.onlab.oauth.adapters.ContentBrowserAdapter
import com.onlab.oauth.classes.FolderHistory
import com.onlab.oauth.classes.StorageRepository
import com.onlab.oauth.databinding.ActivityMainBinding
import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.enums.ContentSource
import com.onlab.oauth.interfaces.*
import com.onlab.oauth.services.GoogleDriveService
import com.onlab.oauth.services.GoogleConnectionService
import com.onlab.oauth.viewModels.Fragments.AddContentBottomFragment
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), IViewItemClickedListener, IAddDirectoryDialogListener {

    private var TAG = this::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ContentBrowserAdapter
    private lateinit var googleConnectionService: IConnectionService
    private val folderHistory = FolderHistory()


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

        // navigate in the folder history
        if (folderHistory.size == 1) {  // load roots
            this.folderHistory.removeLast()
            listRegisteredStorageRootFolders()
            return
        } else if (1 < folderHistory.size) {  // go to previous folder
            navigateToFolder(folderHistory.previous!!)
            return
        }

        onBackPressedDispatcher.onBackPressed()
    }

    private fun initToolbar() {
        setContentView(this.binding.root)
        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.title = ""
        this.binding.toolbar.btnHamburgerMenu.setOnClickListener {
            this.binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        this.binding.toolbar.btnRemove.setOnClickListener {
            this.binding.toolbar.btnRemove.visibility = View.GONE
        }
    }

    private fun initStorages() {
        if (this.googleConnectionService.isLoggedIn()) {
            StorageRepository.registerStorage(
                ContentSource.GOOGLE_DRIVE.toString(),
                GoogleDriveService(googleConnectionService.getCloudStorage() as Drive)
            )
            listRegisteredStorageRootFolders()
        }
    }

    private fun listRegisteredStorageRootFolders() {
        val storagesKeys = StorageRepository.getRegisteredStorageKeys()
        for (storageKey in storagesKeys) {
            val storageService = StorageRepository.getStorage(storageKey)
            if (storageService == null) {
                Log.d(TAG, "Couldn't get storage service. Storage service $storageKey was null")
                continue
            }

            CoroutineScope(Dispatchers.Main).launch {
                val rootFolder = storageService.getCustomRootFolder()
                if (rootFolder == null) {
                    Log.d(TAG,"Couldn't not get root folder for $storageKey")
                    return@launch
                }

                val contents = storageService.listDir(rootFolder.id)
                if (contents == null) {
                    Log.d(TAG,"Couldn't list contents of folder ${rootFolder.name}")
                } else {
                    // todo log
                    this@MainActivity.adapter.addRange(contents)
                }
            }
        }
    }

    private fun initAddContentBottomSheet() {
        this.binding.btnAddContent.setOnClickListener {
            val bottomSheet = AddContentBottomFragment(this)
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
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
            "GoogleDrive", GoogleDriveService(googleConnectionService.getCloudStorage() as Drive)
        )
    }

    private fun googleDisconnectedCallback(menuItem: MenuItem) {
        menuItem.title = "Connect to Google Drive"
        Toast.makeText(this, "Disconnected from Google Drive", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToFolder(content: IStorageContent) {
        if (content.type != ContentType.DIRECTORY) {
            return
        }

        val storageService = StorageRepository.getStorage(content.source.toString())
        if (storageService == null) {
            Toast.makeText(this, "Connection error. Try to reconnect to ${content.source}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Couldn't get storage service. Storage service ${content.source} was null")
            return
        }

        this.binding.toolbar.tvCurrentFolder.text = content.name
        this.folderHistory.add(content)
        this.adapter.clear()

        CoroutineScope(Dispatchers.Main).launch {
            val contents = storageService.listDir(content.id)
            if (contents == null) {
                Log.d(TAG,"Couldn't list contents of folder ${content.name}")
            } else {
                // todo log
                this@MainActivity.adapter.addRange(contents)
            }
        }
    }

    override fun onItemClicked(position: Int) {
        this.binding.toolbar.btnRemove.visibility = View.GONE
        navigateToFolder(this.adapter.getItemAt(position))
    }

    override fun onItemLongClicked(position: Int): Boolean {
        this.binding.toolbar.btnRemove.visibility = View.VISIBLE

        val content = this.adapter.getItemAt(position)

        val storageService = StorageRepository.getStorage(content.source.toString())
        if (storageService == null) {
            Log.d(TAG, "Couldn't get storage service. Storage service ${content.source} was null")
            return true
        }

        this.binding.toolbar.btnRemove.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if (storageService.removeContent(content.id)) {
                    this@MainActivity.adapter.removeAt(position)
                    this@MainActivity.binding.toolbar.btnRemove.visibility = View.GONE

                    Toast.makeText(this@MainActivity, "Removed ${content.name}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Removed content with name: ${content.name}")
                } else {
                    Toast.makeText(this@MainActivity, "Couldn't remove ${content.name}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Couldn't remove content with name: ${content.name}")
                }
            }
        }
        return true
    }

    override fun onAddDirectoryDialogPositiveClicked(directoryName: String) {
        val currentFolder = this.folderHistory.current
        if (currentFolder == null) {
            Toast.makeText(this, "Couldn't create folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Couldn't create folder. No parent directory found.")
            return
        }

        val storageService = StorageRepository.getStorage(currentFolder.source.toString())
        if (storageService == null) {
            Toast.makeText(this, "Couldn't create folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Couldn't get storage service. Storage service ${currentFolder.source} was null")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val newDirectory = storageService.createDir(currentFolder.id, directoryName)
            if (newDirectory != null) {
                this@MainActivity.adapter.add(newDirectory)
                Log.d(TAG, "Created folder with name ${newDirectory.name}")
            } else {
                Log.d(TAG, "Couldn't create folder. Storage service returned null.")
            }
        }
    }

    override fun onAddDirectoryDialogNegativeClicked() {
    }
}