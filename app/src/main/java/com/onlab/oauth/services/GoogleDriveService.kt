package com.onlab.oauth.services

import android.util.Log
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.onlab.oauth.enums.ContentType
import com.onlab.oauth.enums.ContentSource
import com.onlab.oauth.interfaces.IStorageService
import com.onlab.oauth.interfaces.IStorageContent
import com.onlab.oauth.models.GoogleDriveContent
import com.onlab.oauth.models.StorageContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


class GoogleDriveService(private val drive: Drive) : IStorageService {

    companion object {
        private const val TAG: String = "DriveService"
        private val rootFolderPath = listOf("Secrets")  // "root" implicitly handled
    }

    override suspend fun listDir(directoryID: String): List<IStorageContent>? {
        return withContext(Dispatchers.IO) {
            val contents = mutableListOf<IStorageContent>()
            val files = fetchDirectoryById(directoryID) ?: return@withContext null
            for (file in files) {
                val content: IStorageContent = GoogleDriveContent(file.name, file.id, file.mimeType)
                contents.add(content)
                Log.d(TAG, "Name: ${content.name} | ID: ${content.id} | Mime: ${content.type} | Source: ${content.source}")
            }
            contents
        }
    }

    override suspend fun getCustomRootFolder(): IStorageContent? {
        val rootFolder = StorageContent("Root", "root", ContentType.DIRECTORY, ContentSource.GOOGLE_DRIVE)

        if (rootFolderPath.isEmpty()) {
            return rootFolder
        }

        return withContext(Dispatchers.IO) {
            var currentFolder: IStorageContent? = rootFolder

            for (folderName in rootFolderPath) {
                if (currentFolder == null) {
                    return@withContext null
                }

                val currentFolderContents = listDir(currentFolder.id)
                val nextFolder = currentFolderContents?.find { it.name == folderName }
                if (nextFolder == null) {
                    currentFolder = createDir(currentFolder.id, folderName)
                } else {
                    currentFolder = nextFolder
                }
            }
            currentFolder
        }
    }

    override suspend fun createDir(parentDirectoryId: String, directoryName: String): IStorageContent? {
        val fileMetadata = File().apply {
            name = directoryName
            mimeType = "application/vnd.google-apps.folder"
            parents = Collections.singletonList(parentDirectoryId)
        }
        return withContext(Dispatchers.IO) {
            try {
                val directory = drive.files().create(fileMetadata)
                    .setFields("id, name, mimeType")
                    .execute()
                directory?.let { GoogleDriveContent(it.name, it.id, it.mimeType) }
            } catch (ex: GoogleJsonResponseException) {
                null
            }
        }
    }

    override suspend fun removeContent(contentId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                drive.files().delete(contentId).execute()
                true
            } catch (ex: GoogleJsonResponseException) {
                false
            }
        }
    }

    private fun fetchDirectoryById(directoryID: String): List<File>? {
        return try {
            val query = "'$directoryID' in parents and trashed=false"  // safe: it's not and sql querry
            val result = drive.files().list().setQ(query).execute()
            result.files ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching files from Drive: ${e.localizedMessage}", e)
            null
        }
    }
}
