package com.onlab.oauth.services

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.onlab.oauth.interfaces.ICloudStorage
import com.onlab.oauth.interfaces.ICloudStorageContent
import com.onlab.oauth.models.GoogleDriveContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class DriveService(private val drive: Drive) : ICloudStorage {

    companion object {
        private const val TAG: String = "DriveService"
    }

    override suspend fun listDir(directoryID: String): List<ICloudStorageContent> {
        return withContext(Dispatchers.IO) {
            val contents = mutableListOf<ICloudStorageContent>()
            val files = fetchDirectoryById(directoryID)
            for (file in files) {
                val content: ICloudStorageContent = GoogleDriveContent(file.name, file.id, file.mimeType)
                contents.add(content)
                Log.d(TAG, "Name: ${content.name} | ID: ${content.id} | Mime: ${content.type} | Source: ${content.source}")
            }
            contents
        }
    }

    override suspend fun createDir(parentDirectoryId: String, directoryName: String): ICloudStorageContent? {
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

    private fun fetchDirectoryById(directoryID: String): List<File> {
        return try {
            val query = "'$directoryID' in parents and trashed=false"  // safe: it's not and sql querry
            val result = drive.files().list().setQ(query).execute()
            result.files ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching files from Drive: ${e.localizedMessage}", e)
            emptyList()
        }
    }
}
