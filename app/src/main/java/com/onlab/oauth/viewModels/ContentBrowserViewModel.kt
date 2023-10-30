package com.onlab.oauth.viewModels

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onlab.oauth.adapters.ContentBrowserAdapter
import com.onlab.oauth.classes.ConnectionRepository
import com.onlab.oauth.classes.FolderHistory
import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.interfaces.IRecyclerItemClickedListener
import com.onlab.oauth.interfaces.IStorageContent
import com.onlab.oauth.interfaces.IStorageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContentBrowserViewModel: ViewModel(), IRecyclerItemClickedListener {

    val removeButtonVisibility = MutableLiveData<Int>()
    val toolbarCurrentFolderText = MutableLiveData<String>()
    val toastMessage = MutableLiveData<String>()

    val onItemClicked = MutableLiveData<Int>()
    val onItemLongClicked = MutableLiveData<Int>()
    val onItemMoreClicked = MutableLiveData<Int>()

    private val folderHistory = FolderHistory()
    private val _contentList = ContentBrowserAdapter(this)
    val contentList: ContentBrowserAdapter
        get() {
            return _contentList
        }

    companion object {
        const val tag = "ContentBrowserViewModel"

        fun createFactory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ContentBrowserViewModel() as T
                }
            }
        }
    }

    fun init() {
        listRegisteredStorageRootFolders()
    }


    fun getCurrentFolderStorageService(toastMessageOnError: String = ""): Pair<IStorageContent, IStorageService>? {
        val currentFolder = this.folderHistory.current
        if (currentFolder == null) {
            if (toastMessageOnError.isNotEmpty()) {
                toastMessage.value = toastMessageOnError
            }
            Log.d(tag, "$toastMessageOnError. Couldn't get current folder from folder history")
            return null
        }

        val storageService = ConnectionRepository.get(currentFolder.source.toString())?.getStorage()
        if (storageService == null) {
            if (toastMessageOnError.isNotEmpty()) {
                toastMessage.value = toastMessageOnError
            }
            Log.d(tag, "Couldn't get storage service. Storage service ${currentFolder.source} was null")
            return null
        }

        return Pair(currentFolder, storageService)
    }


    private fun navigateToFolder(content: IStorageContent) {
        if (content.type != ContentType.DIRECTORY) {
            return
        }

        val storageService = ConnectionRepository.get(content.source.toString())?.getStorage()
        if (storageService == null) {
            toastMessage.value = "Connection error. Try to reconnect to ${content.source}"
            Log.d(tag, "Couldn't get storage service. Storage service ${content.source} was null")
            return
        }

        toolbarCurrentFolderText.value = content.name
        folderHistory.add(content)
        contentList.clear()

        CoroutineScope(Dispatchers.Main).launch {
            val contents = storageService.listDir(content.id)
            if (contents == null) {
                Log.d(tag,"Couldn't list contents of folder ${content.name}")
                return@launch
            }
            contentList.addRange(contents)
            Log.d(tag, "Listed ${contents.size} contents from ${content.source}")
            contents.forEach { content -> Log.d(tag, "\t${content.name} (${content.id})") }
        }
    }


    fun navigateBack(): Boolean {
        if (folderHistory.size == 1) {  // load roots
            this.folderHistory.removeLast()
            listRegisteredStorageRootFolders()
            return true
        } else if (1 < folderHistory.size) {  // go to previous folder
            navigateToFolder(folderHistory.previous!!)
            return true
        }
        return false
    }


    fun listRegisteredStorageRootFolders() = CoroutineScope(Dispatchers.Main).launch {
        toolbarCurrentFolderText.value = "Roots"
        contentList.clear()

        for ((connectionKey, connectionService) in ConnectionRepository.registeredEntries) {
            val storageService = connectionService.getStorage() ?: continue

            val rootFolder = storageService.getCustomRootFolder()
            if (rootFolder == null) {
                Log.d(tag,"Couldn't not get root folder for $connectionKey")
                return@launch
            }

            val contents = storageService.listDir(rootFolder.id)
            if (contents == null) {
                Log.d(tag,"Couldn't list contents of folder ${rootFolder.name}")
                return@launch
            }
            contentList.addRange(contents)
            Log.d(tag, "Listed ${contents.size} root folders from $connectionKey")
            contents.forEach { content -> Log.d(tag, "\t${content.name} (${content.id})") }
        }
    }


    override fun onItemClicked(position: Int) {
        removeButtonVisibility.value = View.GONE
        navigateToFolder(contentList.getItemAt(position))
    }

    override fun onItemLongClicked(position: Int): Boolean {
        removeButtonVisibility.value = View.VISIBLE
        onItemLongClicked.value = position
        return true
    }

    fun onItemLongClickedHandler(position: Int) = CoroutineScope(Dispatchers.Main).launch {
        val content = contentList.getItemAt(position)

        val storageService = ConnectionRepository.get(content.source.toString())?.getStorage()
        if (storageService == null) {
            Log.d(tag, "Couldn't get storage service. Storage service ${content.source} was null")
            return@launch
        }

        if (storageService.removeContent(content.id)) {
            contentList.removeAt(position)
            removeButtonVisibility.value = View.GONE

            toastMessage.value = "Removed ${content.name}"
            Log.d(tag, "Removed content with name: ${content.name}")
        } else {
            toastMessage.value = "Couldn't remove ${content.name}"
            Log.d(tag, "Couldn't remove content with name: ${content.name}")
        }
    }

    override fun onMoreClicked(position: Int) {  // todo
        onItemMoreClicked.value = position
        Log.d(tag, "onMoreClicked at pos=$position")
    }
}