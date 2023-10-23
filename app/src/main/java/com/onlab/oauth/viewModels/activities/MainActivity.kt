package com.onlab.oauth.viewModels.activities

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlab.oauth.adapters.ContentBrowserAdapter
import com.onlab.oauth.classes.ConnectionRepository
import com.onlab.oauth.classes.EncryptionService
import com.onlab.oauth.classes.FolderHistory
import com.onlab.oauth.databinding.ActivityMainBinding
import com.onlab.oauth.enums.ContentSource
import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.interfaces.*
import com.onlab.oauth.services.GoogleConnectionService
import com.onlab.oauth.viewModels.fragments.AddContentBottomFragment
import com.onlab.oauth.viewModels.fragments.ManageFileBottomFragment
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(),
    IRecyclerItemClickedListener,
    IAddContentBottomFragmentListener,
    IManageFileBottomFragmentListener
{

    private var TAG = this::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var contentList: ContentBrowserAdapter
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
        listRegisteredStorageRootFolders()
    }

    private fun listRegisteredStorageRootFolders() = CoroutineScope(Dispatchers.Main).launch {
        this@MainActivity.binding.toolbar.tvCurrentFolder.text = "Roots"
        this@MainActivity.contentList.clear()

        for ((connectionKey, connectionService) in ConnectionRepository.registeredEntries) {
            val storageService = connectionService.getStorage() ?: continue

            val rootFolder = storageService.getCustomRootFolder()
            if (rootFolder == null) {
                Log.d(TAG,"Couldn't not get root folder for $connectionKey")
                return@launch
            }

            val contents = storageService.listDir(rootFolder.id)
            if (contents == null) {
                Log.d(TAG,"Couldn't list contents of folder ${rootFolder.name}")
                return@launch
            }
            this@MainActivity.contentList.addRange(contents)
            Log.d(TAG, "Listed ${contents.size} root folders from $connectionKey")
            contents.forEach { content -> Log.d(TAG, "\t${content.name} (${content.id})") }
        }
    }

    private fun initAddContentBottomFragment() {
        this.binding.btnAddContent.setOnClickListener {
            val bottomSheet = AddContentBottomFragment(this)
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
    }

    private fun initRecycleView() {
        this.contentList = ContentBrowserAdapter(this)
        this.binding.rvContents.adapter = this.contentList
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
                    // todo list root folders
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

        val storageService = ConnectionRepository.get(content.source.toString())?.getStorage()
        if (storageService == null) {
            Toast.makeText(this, "Connection error. Try to reconnect to ${content.source}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Couldn't get storage service. Storage service ${content.source} was null")
            return
        }

        this.binding.toolbar.tvCurrentFolder.text = content.name
        this.folderHistory.add(content)
        this.contentList.clear()

        CoroutineScope(Dispatchers.Main).launch {
            val contents = storageService.listDir(content.id)
            if (contents == null) {
                Log.d(TAG,"Couldn't list contents of folder ${content.name}")
                return@launch
            }
            this@MainActivity.contentList.addRange(contents)
            Log.d(TAG, "Listed ${contents.size} contents from ${content.source}")
            contents.forEach { content -> Log.d(TAG, "\t${content.name} (${content.id})") }
        }
    }

    override fun onItemClicked(position: Int) {
        this.binding.toolbar.btnRemove.visibility = View.GONE
        navigateToFolder(this.contentList.getItemAt(position))
    }

    override fun onItemLongClicked(position: Int): Boolean {
        this.binding.toolbar.btnRemove.visibility = View.VISIBLE

        val content = this.contentList.getItemAt(position)

        val storageService = ConnectionRepository.get(content.source.toString())?.getStorage()
        if (storageService == null) {
            Log.d(TAG, "Couldn't get storage service. Storage service ${content.source} was null")
            return true
        }

        this.binding.toolbar.btnRemove.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if (storageService.removeContent(content.id)) {
                    this@MainActivity.contentList.removeAt(position)
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

    override fun onMoreClicked(position: Int) {
        Log.d(TAG, "onMoreClicked at pos=$position")
        val bottomSheet = ManageFileBottomFragment(this, position)
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }

    override fun onAddDirectoryDialogPositiveClicked(directoryName: String) {
        val currentFolder = this.folderHistory.current
        if (currentFolder == null) {
            Toast.makeText(this, "Couldn't create folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Couldn't create folder. No parent directory found.")
            return
        }

        val storageService = ConnectionRepository.get(currentFolder.source.toString())?.getStorage()
        if (storageService == null) {
            Toast.makeText(this, "Couldn't create folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Couldn't get storage service. Storage service ${currentFolder.source} was null")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val newDirectory = storageService.createDir(currentFolder.id, directoryName)
            if (newDirectory != null) {
                this@MainActivity.contentList.add(newDirectory)
                Log.d(TAG, "Created folder with name ${newDirectory.name}")
            } else {
                Log.d(TAG, "Couldn't create folder. Storage service returned null.")
            }
        }
    }

    override fun onAddDirectoryDialogNegativeClicked() {
    }

    private fun getStorageService(errorMessage: String): IStorageService? {
        val currentFolder = this.folderHistory.current
        if (currentFolder == null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "$errorMessage. Couldn't get current folder from folder history")
            return null
        }

        val storageService = ConnectionRepository.get(currentFolder.source.toString())?.getStorage()
        if (storageService == null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Couldn't get storage service. Storage service ${currentFolder.source} was null")
            return null
        }

        return storageService
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onFileBrowserItemSelected(uri: Uri) {
        val storageService = getStorageService("File upload failed") ?: return
        val parentFolder = folderHistory.current!!
        val mimeType = this.contentResolver.getType(uri)
        val fileName = this.getFileNameFromUri(uri)
        Log.d(TAG, fileName)

        Toast.makeText(this, "Uploading file", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Uploading file to ${parentFolder.name} folder (path=${uri.path} mime=$mimeType)")

        CoroutineScope(Dispatchers.Main).launch {
            val inputStream = this@MainActivity.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.d(TAG, "Couldn't upload file. InputStream was null")
                return@launch
            }

            // encrypt
            val keyAlias = UUID.randomUUID().toString()
            val secretService = EncryptionService(keyAlias)  // mert még nincs folder id-nk
            val (isEncrypted, encryptedBytes) = secretService.encrypt(inputStream)

            if (!isEncrypted) {
                Toast.makeText(this@MainActivity, "Encryption failed", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Encryption failed (name=$fileName)")
            }

            val uploadInputStream = ByteArrayInputStream(encryptedBytes)
            val newFile = storageService.uploadFile(uploadInputStream, mimeType.toString(), parentFolder.id, fileName, keyAlias)
            if (newFile != null) {
                this@MainActivity.contentList.add(newFile)
                Log.d(TAG, "Uploaded file with name $fileName")
            } else {
                secretService.deleteAlias()
                Log.d(TAG, "Couldn't upload file with name $fileName. Storage service returned null.")
            }

            withContext(Dispatchers.IO) {
                inputStream.close()
            }
        }

    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileNameWithExtension: String? = null

        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                fileNameWithExtension = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }

        if (fileNameWithExtension == null) {
            fileNameWithExtension = uri.path?.split("/")?.last()
        }
        if (fileNameWithExtension == null) {
            fileNameWithExtension = "Untitled"
        }

        return fileNameWithExtension!!
    }

    override fun onManageFileShareButtonClicked(position: Int) {
        Log.d(TAG, "onManageFileShareButtonClicked pos=$position")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onManageFileDownloadButtonClicked(position: Int) {
        Log.d(TAG, "onManageFileDownloadButtonClicked pos=$position")

        val storageService = getStorageService("Couldn't download file") ?: return
        val content = this.contentList.getItemAt(position)

        Toast.makeText(this, "Downloading file", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Downloading file (name=${content.name})")

        CoroutineScope(Dispatchers.Main).launch {
            val inputStream = storageService.downloadFile(content.id)
            if (inputStream == null) {
                Toast.makeText(this@MainActivity, "Downloading file failed", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Downloading file failed (name=${content.name})")
                return@launch
            }

            // decrypt
            val secretService = EncryptionService(content.keyAlias)
            val (isDecrypted, decryptedBytes) = secretService.decrypt(inputStream)

            if (!isDecrypted) {
                Toast.makeText(this@MainActivity, "Decryption failed - Missing key", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Decryption failed (name=${content.name}) - missing key ${content.id}")
            }

            val isSaved = saveFileToAppDir(decryptedBytes, content.name)
            if (isSaved) {
                Toast.makeText(this@MainActivity, "Downloaded file", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Downloaded file: ${content.name}. Saved to default app folder")
            } else {
                Toast.makeText(this@MainActivity, "Saving file failed", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Saved file: ${content.name}. Saved to default app folder")
            }
        }
    }

    private suspend fun saveFileToAppDir(data: ByteArray, fileName: String): Boolean = withContext(Dispatchers.IO) {
        var success = true
        val file = File(filesDir, fileName)

        try {
            file.outputStream().use { outputStream ->
                outputStream.write(data)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            success = false
        }

        return@withContext success
    }

    override fun onManageFileManageKeyButtonClicked(position: Int) {
        Log.d(TAG, "onManageFileManageKeyButtonClicked pos=$position")
    }
}