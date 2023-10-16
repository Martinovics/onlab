package com.onlab.oauth.viewModels.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.api.services.drive.Drive
import com.onlab.oauth.R
import com.onlab.oauth.adapters.ContentBrowserAdapter
import com.onlab.oauth.classes.ConnectionRepository
import com.onlab.oauth.classes.FolderHistory
import com.onlab.oauth.classes.StorageRepository
import com.onlab.oauth.databinding.ActivityMainBinding
import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.enums.ContentSource
import com.onlab.oauth.interfaces.*
import com.onlab.oauth.services.GoogleDriveService
import com.onlab.oauth.services.GoogleConnectionService
import com.onlab.oauth.viewModels.fragments.AddContentBottomFragment
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), IRecyclerItemClickedListener, IAddDirectoryDialogListener {

    private var TAG = this::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ContentBrowserAdapter
    private val folderHistory = FolderHistory()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)

        // order matters
        initToolbar()
        initRecycleView()
        initAddContentBottomFragment()
        initConnections()
        initNavigationView()
        initStorages()
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

    private fun initConnections() {
        // register connection services here ==========
        ConnectionRepository.register(ContentSource.GOOGLE_DRIVE.toString(), GoogleConnectionService(this))
    }

    private fun initStorages() {
        for (kv in ConnectionRepository.registeredEntries) {
            val key = kv.key
            val connectionService = kv.value

            if (connectionService.isLoggedIn()) {
                val storageService = connectionService.getCloudStorage()
                registerStorage(key, storageService)
            }

        }
        listRegisteredStorageRootFolders()
    }

    private fun registerStorage(key: String, storageService: Any) {
        // register storage services here ==========
        when (key) {
            ContentSource.GOOGLE_DRIVE.toString() ->
                StorageRepository.register(key, GoogleDriveService(storageService as Drive))
            else ->
                throw Exception("Unknown storage service: $key")
        }
    }

    private fun listRegisteredStorageRootFolders() {
        for (kv in StorageRepository.registeredEntries) {
            val storageKey = kv.key
            val storageService = kv.value

            CoroutineScope(Dispatchers.Main).launch {
                val rootFolder = storageService.getCustomRootFolder()
                if (rootFolder == null) {
                    Log.d(TAG,"Couldn't not get root folder for $storageKey")
                    return@launch
                }

                val contents = storageService.listDir(rootFolder.id)
                if (contents == null) {
                    Log.d(TAG,"Couldn't list contents of folder ${rootFolder.name}")
                    return@launch
                }
                this@MainActivity.adapter.addRange(contents)
                Log.d(TAG, "Listed ${contents.size} root folders from $storageKey")
                contents.forEach { content -> Log.d(TAG, "\t${content.name} (${content.id})") }
            }
        }
    }

    private fun initAddContentBottomFragment() {
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

        // set drawer menu connection titles
        for (kv in ConnectionRepository.registeredEntries) {
            val connectionService = kv.value
            val drawerMenuItem = this.binding.drawerMenu.menu.findItem(connectionService.menuItemId)
            if (connectionService.isLoggedIn()) {
                drawerMenuItem.title = "Disconnect ${connectionService.title}"
            } else {
                drawerMenuItem.title = "Connect ${connectionService.title}"
            }
        }

        this.binding.drawerMenu.setNavigationItemSelectedListener { menuItem ->
            var setListener = false
            for (kv in ConnectionRepository.registeredEntries) {
                val connectionService = kv.value
                if (connectionService.menuItemId == menuItem.itemId) {
                    connectionDrawerMenuListener(connectionService)
                    setListener = true
                }
            }
            setListener
        }
    }

    private fun connectionDrawerMenuListener(connectionService: IConnectionService) {
        val drawerMenuItem = this.binding.drawerMenu.menu.findItem(connectionService.menuItemId)
        if (connectionService.isLoggedIn()) {
            connectionService.signOut(
                callback_success = {
                    drawerMenuItem.title = "Connect to ${connectionService.title}"
                    StorageRepository.remove(connectionService.source.toString())
                    Log.i(TAG, "Disconnected from ${connectionService.title}")
                    Toast.makeText(this, "Disconnected from ${connectionService.title}", Toast.LENGTH_SHORT).show()
                },
                callback_fail = {
                    Log.e(TAG, "Disconnected from ${connectionService.title} failed")
                    Toast.makeText(this, "Disconnect failed", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            connectionService.signIn(
                callback_success = {
                    drawerMenuItem.title = "Disconnect from ${connectionService.title}"
                    registerStorage(connectionService.source.toString(), connectionService.getCloudStorage())
                    Log.d(TAG, "Connected to ${connectionService.title}")
                    Toast.makeText(this, "Connected to ${connectionService.title}", Toast.LENGTH_SHORT).show()
                },
                callback_fail = {
                    Log.e(TAG, "Connection to ${connectionService.title} failed")
                    Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun navigateToFolder(content: IStorageContent) {
        if (content.type != ContentType.DIRECTORY) {
            return
        }

        val storageService = StorageRepository.get(content.source.toString())
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
                return@launch
            }
            this@MainActivity.adapter.addRange(contents)
            Log.d(TAG, "Listed ${contents.size} contents from ${content.source}")
            contents.forEach { content -> Log.d(TAG, "\t${content.name} (${content.id})") }
        }
    }

    override fun onItemClicked(position: Int) {
        this.binding.toolbar.btnRemove.visibility = View.GONE
        navigateToFolder(this.adapter.getItemAt(position))
    }

    override fun onItemLongClicked(position: Int): Boolean {
        this.binding.toolbar.btnRemove.visibility = View.VISIBLE

        val content = this.adapter.getItemAt(position)

        val storageService = StorageRepository.get(content.source.toString())
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

        val storageService = StorageRepository.get(currentFolder.source.toString())
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