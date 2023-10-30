package com.onlab.oauth.viewModels

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onlab.oauth.classes.EncryptionService
import com.onlab.oauth.interfaces.IStorageContent
import com.onlab.oauth.interfaces.IStorageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.*

class UploadDownloadViewModel: ViewModel() {
    val toastMessage = MutableLiveData<String>()
    val onContentAdded = MutableLiveData<IStorageContent>()

    companion object {
        const val tag = "UploadDownloadViewModel"

        fun createFactory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return UploadDownloadViewModel() as T
                }
            }
        }
    }

    fun init() {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun uploadFile(uri: Uri, parentFolder: IStorageContent, storageService: IStorageService, contentResolver: ContentResolver) {
        val mimeType = contentResolver.getType(uri)
        val fileName = this.getFileNameFromUri(uri, contentResolver)
        Log.d(tag, fileName)

        toastMessage.value = "Uploading file"
        Log.d(tag, "Uploading file to ${parentFolder.name} folder (path=${uri.path} mime=$mimeType)")

        CoroutineScope(Dispatchers.Main).launch {
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.d(tag, "Couldn't upload file. InputStream was null")
                return@launch
            }

            // encrypt
            val keyAlias = UUID.randomUUID().toString()
            val secretService = EncryptionService(keyAlias)  // mert még nincs folder id-nk
            val (isEncrypted, encryptedBytes) = secretService.encrypt(inputStream)

            if (!isEncrypted) {
                toastMessage.value = "Encryption failed"
                Log.d(tag, "Encryption failed (name=$fileName)")
            }

            val uploadInputStream = ByteArrayInputStream(encryptedBytes)
            val newFile = storageService.uploadFile(uploadInputStream, mimeType.toString(), parentFolder.id, fileName, keyAlias)
            if (newFile != null) {
                onContentAdded.value = newFile!!
                toastMessage.value = "Upload successfull"
                Log.d(tag, "Uploaded file with name $fileName")
            } else {
                secretService.deleteAlias()
                Log.d(tag, "Couldn't upload file with name $fileName. Storage service returned null.")
            }

            withContext(Dispatchers.IO) {
                inputStream.close()
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri, contentResolver: ContentResolver): String {
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


    fun downloadFile() {

    }
}