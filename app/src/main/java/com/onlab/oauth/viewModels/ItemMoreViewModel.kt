package com.onlab.oauth.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onlab.oauth.interfaces.IManageFileBottomFragmentListener
import com.onlab.oauth.interfaces.IStorageContent


class ItemMoreViewModel: ViewModel(), IManageFileBottomFragmentListener {
    val toastMessage = MutableLiveData<String>()
    val onDownloadFile = MutableLiveData<Int>()

    companion object {
        const val tag = "ItemMoreViewModel"

        fun createFactory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ItemMoreViewModel() as T
                }
            }
        }
    }

    override fun onManageFileShareButtonClicked(position: Int) {
        Log.d(tag, "onManageFileShareButtonClicked pos=$position")
    }

    override fun onManageFileDownloadButtonClicked(position: Int) {
        Log.d(tag, "onManageFileDownloadButtonClicked pos=$position")
        onDownloadFile.value = position
    }

    override fun onManageFileManageKeyButtonClicked(position: Int) {
        Log.d(tag, "onManageFileManageKeyButtonClicked pos=$position")
    }
}