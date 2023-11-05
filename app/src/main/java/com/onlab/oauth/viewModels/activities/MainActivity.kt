package com.onlab.oauth.viewModels.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlab.oauth.adapters.ContentBrowserAdapter
import com.onlab.oauth.classes.ConnectionRepository
import com.onlab.oauth.databinding.ActivityMainBinding
import com.onlab.oauth.enums.ContentSource
import com.onlab.oauth.interfaces.*
import com.onlab.oauth.services.GoogleConnectionService
import com.onlab.oauth.viewModels.fragments.AddContentBottomFragment
import com.onlab.oauth.viewModels.fragments.ManageFileBottomFragment
import kotlinx.coroutines.*
import java.util.*
import androidx.activity.viewModels
import com.onlab.oauth.viewModels.*


@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private val drawerMenuViewModel: DrawerMenuViewModel by viewModels { DrawerMenuViewModel.createFactory() }
    private val contentBrowserViewModel: ContentBrowserViewModel by viewModels { ContentBrowserViewModel.createFactory() }
    private val addContentViewModel: AddContentViewModel by viewModels { AddContentViewModel.createFactory() }
    private val uploadDownloadViewModel: UploadDownloadViewModel by viewModels { UploadDownloadViewModel.createFactory() }
    private val itemMoreViewModel: ItemMoreViewModel by viewModels { ItemMoreViewModel.createFactory() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)

        // order matters
        initConnections()

        initToolbar()
        initRecycleView()
        initDrawerViewModel()
        initContentBrowserViewModel()
        initAddContentViewModel()
        initUploadDownloadViewModel()
        initItemMoreViewModel()
    }


    private fun initConnections() {
        // register connection services here ==========
        ConnectionRepository.register(ContentSource.GOOGLE_DRIVE.toString(), GoogleConnectionService(this))
    }


    private fun initRecycleView() {
        this.binding.rvContents.adapter = contentBrowserViewModel.contentList
        this.binding.rvContents.layoutManager = LinearLayoutManager(this)
    }


    private fun initToolbar() {
        setContentView(this.binding.root)
        setSupportActionBar(this.binding.toolbar.root)
        supportActionBar?.title = ""
        this.binding.toolbar.btnHamburgerMenu.setOnClickListener {
            this.binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        this.binding.toolbar.btnRemove.setOnClickListener {
            this.binding.toolbar.btnRemove.visibility = View.GONE
        }
    }


    private fun initDrawerViewModel() {
        drawerMenuViewModel.connectionTitles.observe(this) { titles ->
            titles.forEach { (itemId, title) ->
                this.binding.drawerMenu.menu.findItem(itemId)?.title = title
            }
        }

        drawerMenuViewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        drawerMenuViewModel.closeDrawer.observe(this) {
            this.binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        this.binding.drawerMenu.setNavigationItemSelectedListener { menuItem ->
            drawerMenuViewModel.onMenuItemSelected(menuItem.itemId)
            true
        }

        drawerMenuViewModel.init()
    }


    private fun initContentBrowserViewModel() {
        contentBrowserViewModel.removeButtonVisibility.observe(this) { visibility ->
            this.binding.toolbar.btnRemove.visibility = visibility
        }
        contentBrowserViewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        contentBrowserViewModel.toolbarCurrentFolderText.observe(this) { text ->
            this.binding.toolbar.tvCurrentFolder.text = text
        }

        contentBrowserViewModel.onItemLongClicked.observe(this) { position ->
            this.binding.toolbar.btnRemove.setOnClickListener {
                contentBrowserViewModel.onItemLongClickedHandler(position)
            }
        }
        contentBrowserViewModel.onItemMoreClicked.observe(this) { position ->
            val bottomSheet = ManageFileBottomFragment(itemMoreViewModel, position)
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        contentBrowserViewModel.init()
    }


    private fun initAddContentViewModel() {
        this.binding.btnAddContent.setOnClickListener {
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


    private fun initItemMoreViewModel() {
        itemMoreViewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        itemMoreViewModel.onDownloadFile.observe(this) { position ->
            val folderStoragePair = contentBrowserViewModel.getCurrentFolderStorageService("Couldn't add folder")
            if (folderStoragePair != null) {
                val (_, storageService) = folderStoragePair
                val content = contentBrowserViewModel.contentList.getItemAt(position)
                uploadDownloadViewModel.downloadFile(content, storageService, filesDir)
            }
        }
    }


    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this.binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerMenuViewModel.closeDrawer()
            return
        }

        if (contentBrowserViewModel.navigateBack()) {
            return
        }

        onBackPressedDispatcher.onBackPressed()
    }

    
}