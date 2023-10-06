package com.onlab.oauth.services

import android.util.Log
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.onlab.oauth.interfaces.ICloudStorage
import com.onlab.oauth.interfaces.ICloudStorageContent
import com.onlab.oauth.models.GoogleDriveContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DriveService(private val drive: Drive) : ICloudStorage {

    companion object {
        private const val TAG: String = "DriveService"
    }

    override fun listDir(directoryID: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val files = fetchDirectoryById(directoryID)

            // Váltás a Main szálra a UI módosításokhoz (jelen esetben a logoláshoz nem kell | csak példa)
            withContext(Dispatchers.Main) {
                if (files.isNotEmpty()) {
                    for (file in files) {
                        val content: ICloudStorageContent = GoogleDriveContent(file.name, file.id, file.mimeType)
                        Log.d(TAG, "Name: ${content.name} | ID: ${content.id} | Mime: ${content.type} | Source: ${content.source}")
                    }
                } else {
                    Log.i(TAG, "No files found.")
                }
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
