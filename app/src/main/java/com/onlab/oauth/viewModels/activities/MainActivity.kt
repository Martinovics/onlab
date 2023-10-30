package com.onlab.oauth.viewModels.activities

import android.annotation.SuppressLint
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
import com.onlab.oauth.viewModels.DrawerMenuViewModel
import com.onlab.oauth.viewModels.fragments.AddContentBottomFragment
import com.onlab.oauth.viewModels.fragments.ManageFileBottomFragment
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.*
import androidx.activity.viewModels
import com.onlab.oauth.viewModels.AddContentViewModel
import com.onlab.oauth.viewModels.ContentBrowserViewModel
import com.onlab.oauth.viewModels.UploadDownloadViewModel


@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity(),
    IManageFileBottomFragmentListener  // todo kiszervezni
{

    private var TAG = this::class.java.simpleName
    private lateinit var _binding: ActivityMainBinding
    private lateinit var _contentList: ContentBrowserAdapter
    private val folderHistory = FolderHistory()

    private val drawerMenuViewModel: DrawerMenuViewModel by viewModels { DrawerMenuViewModel.createFactory() }
    private val contentBrowserViewModel: ContentBrowserViewModel by viewModels { ContentBrowserViewModel.createFactory() }
    private val addContentViewModel: AddContentViewModel by viewModels { AddContentViewModel.createFactory() }
    private val uploadDownloadViewModel: UploadDownloadViewModel by viewModels { UploadDownloadViewModel.createFactory() }


    val binding: ActivityMainBinding  // todo remove
        get() {
            return _binding
        }

    val contentList: ContentBrowserAdapter  // todo remove
        get() {
            return _contentList
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)

        // order matters
        initToolbar()
        initRecycleView()
        initConnections()
        initDrawerViewModel()
        initContentBrowserViewModel()
        initAddContentViewModel()
        initUploadDownloadViewModel()
    }

    private fun initDrawerViewModel() {
        drawerMenuViewModel.connectionTitles.observe(this) { titles ->
            titles.forEach { (itemId, title) ->
                binding.drawerMenu.menu.findItem(itemId)?.title = title
            }
        }

        drawerMenuViewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        drawerMenuViewModel.closeDrawer.observe(this) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.drawerMenu.setNavigationItemSelectedListener { menuItem ->
            drawerMenuViewModel.onMenuItemSelected(menuItem.itemId)
            true
        }

        drawerMenuViewModel.init()
    }

    private fun initContentBrowserViewModel() {
        contentBrowserViewModel.removeButtonVisibility.observe(this) { visibility ->
            binding.toolbar.btnRemove.visibility = visibility
        }
        contentBrowserViewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        contentBrowserViewModel.toolbarCurrentFolderText.observe(this) { text ->
            binding.toolbar.tvCurrentFolder.text = text
        }

        contentBrowserViewModel.onItemLongClicked.observe(this) { position ->
            binding.toolbar.btnRemove.setOnClickListener {
                contentBrowserViewModel.onItemLongClickedHandler(position)
            }
        }
        contentBrowserViewModel.onItemMoreClicked.observe(this) { position ->
            val bottomSheet = ManageFileBottomFragment(this, position)
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        contentBrowserViewModel.init()
    }


    private fun initAddContentViewModel() {
        _binding.btnAddContent.setOnClickListener {
            val bottomSheet = AddContentBottomFragment(addContentViewModel)
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        addContentViewModel.onContentAdded.observe(this) { content ->
            contentBrowserViewModel.contentList.add(content)
        }

        addContentViewModel.onAddPositiveClicked.observe(this) { directoryName ->
            val folderStoragePair = contentBrowserViewModel.getCurrentFolderStorageService("Couldn't add folder")
            if (folderStoragePair != null) {
                val (currentFolder, storageService) = folderStoragePair
                addContentViewModel.createFolder(currentFolder.id, directoryName, storageService)
            }
        }

        addContentViewModel.onItemSelected.observe(this) { uri ->
            val folderStoragePair = contentBrowserViewModel.getCurrentFolderStorageService("Couldn't upload file")
            if (folderStoragePair != null) {
                val (currentFolder, storageService) = folderStoragePair
                uploadDownloadViewModel.uploadFile(uri, currentFolder, storageService, contentResolver)
            }
        }

        addContentViewModel.init()
    }


    private fun initUploadDownloadViewModel() {
        uploadDownloadViewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        uploadDownloadViewModel.onContentAdded.observe(this) { content ->
            contentBrowserViewModel.contentList.add(content)
        }

        uploadDownloadViewModel.init()
    }


    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerMenuViewModel.closeDrawer()
            return
        }

        if (contentBrowserViewModel.navigateBack()) {
            return
        }

        onBackPressedDispatcher.onBackPressed()
    }

    private fun initToolbar() {
        setContentView(_binding.root)
        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.title = ""
        _binding.toolbar.btnHamburgerMenu.setOnClickListener {
            _binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        _binding.toolbar.btnRemove.setOnClickListener {
            _binding.toolbar.btnRemove.visibility = View.GONE
        }
    }

    private fun initConnections() {
        // register connection services here ==========
        ConnectionRepository.register(ContentSource.GOOGLE_DRIVE.toString(), GoogleConnectionService(this))
    }

    private fun initRecycleView() {
        _contentList = contentBrowserViewModel.contentList
        _binding.rvContents.adapter = contentList
        _binding.rvContents.layoutManager = LinearLayoutManager(this)
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