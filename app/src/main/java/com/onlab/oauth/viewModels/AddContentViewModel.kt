package com.onlab.oauth.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onlab.oauth.interfaces.IAddContentBottomFragmentListener
import com.onlab.oauth.interfaces.IStorageContent
import com.onlab.oauth.interfaces.IStorageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddContentViewModel: ViewModel(), IAddContentBottomFragmentListener {

    val onContentAdded = MutableLiveData<IStorageContent>()

    val onItemSelected = MutableLiveData<Uri>()
    val onAddPositiveClicked = MutableLiveData<String>()

    companion object {
        const val tag = "ManageFileBottomVM"

        fun createFactory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AddContentViewModel() as T
                }
            }
        }
    }


    fun init() {

    }

    override fun onFileBrowserItemSelected(uri: Uri) {
        onItemSelected.value = uri
    }

    override fun onAddDirectoryDialogPositiveClicked(directoryName: String) {
        onAddPositiveClicked.value = directoryName
    }

    fun createFolder(parentDirectoryId: String, directoryName: String, storageService: IStorageService) {
        CoroutineScope(Dispatchers.Main).launch {
            val newDirectory = storageService.createDir(parentDirectoryId, directoryName)
            if (newDirectory != null) {
                //this@MainActivity.contentList.add(newDirectory)
                onContentAdded.value = newDirectory!!
                Log.d(tag, "Created folder with name ${newDirectory.name}")
            } else {
                Log.d(tag, "Couldn't create folder. Storage service returned null.")
            }
        }
    }

    override fun onAddDirectoryDialogNegativeClicked() {
    }

}