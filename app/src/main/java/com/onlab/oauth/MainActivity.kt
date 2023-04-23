package com.onlab.oauth

import android.os.Build
import android.os.Bundle
import android.security.keystore.UserNotAuthenticatedException
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.onlab.oauth.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream




@RequiresApi(Build.VERSION_CODES.R)
class MainActivity : AppCompatActivity() {

    companion object {
        const val AUTH_REMEMBER_SECONDS = 10
    }

    private val TAG = "MainActivity"
    private val secrets = Secrets(AUTH_REMEMBER_SECONDS)
    private lateinit var binding: ActivityMainBinding
    private lateinit var localAuth: LocalAuth




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        this.localAuth = LocalAuth(this)

        this.update_files_list()
        this.set_save_click_listener()
        this.set_load_click_listener()
        this.set_delete_click_listener()
        this.set_local_auth_click_listener()
    }


    private fun set_save_click_listener() {
        this.binding.btnSave.setOnClickListener {
            if (this.binding.etPlain.text.isEmpty()) {
                Log.d(TAG, "cant save empty file")
                return@setOnClickListener
            }
            if (this.binding.etSave.text.isEmpty()) {
                Log.d(TAG, "must specify file name (save)")
                return@setOnClickListener
            }

            val result = this.write_file(
                file_name = this.binding.etSave.text.toString(),
                content = this.binding.etPlain.text.toString()
            )
            if (!result) {
                Log.d(TAG, "could not write file")
                return@setOnClickListener
            }

            Log.d(TAG, "created file")
            this.binding.etPlain.text.clear()
            this.binding.etSave.text.clear()
            this.update_files_list()
        }
    }


    private fun set_load_click_listener() {
        this.binding.btnLoad.setOnClickListener {
            if (this.binding.etLoad.text.isEmpty()) {
                Log.d(TAG, "must specify file name (load)")
                return@setOnClickListener
            }

            val content = load_file(file_name = this.binding.etLoad.text.toString())
            if (content.isEmpty()) {
                Log.d(TAG, "could not load file")
                return@setOnClickListener
            }

            Log.d(TAG, "loaded file")
            this.binding.etPlain.setText(content)
            this.binding.etLoad.text.clear()
        }
    }


    private fun set_delete_click_listener() {
        this.binding.btnDelete.setOnClickListener {
            if (this.binding.etLoad.text.isEmpty()) {
                Log.d(TAG, "must specify file name (delete)")
                return@setOnClickListener
            }
            if (!this.delete_file(file_name = this.binding.etLoad.text.toString())) {
                Log.d(TAG, "could not delete file")
                return@setOnClickListener
            }

            Log.d(TAG, "deleted file")
            this.binding.etLoad.text.clear()
            this.update_files_list()
        }
    }


    private fun set_local_auth_click_listener() {
        this.binding.btnLocalAuth.setOnClickListener {
            Log.d(TAG, "auth btn clicked")
            this.localAuth.showAuthWindow()
        }
    }


    private fun write_file(file_name: String, content: String): Boolean {
        val file = File(this.filesDir, file_name)
        try {
            this.secrets.write_file(file_name, content.encodeToByteArray(), FileOutputStream(file))
            return true
        } catch (ex: UserNotAuthenticatedException) {
            Log.d(TAG, "User must authenticate. The key's validity timed out.")
            this.localAuth.showAuthWindow()
            return false
        }
    }


    private fun load_file(file_name: String): String {
        val file = File(this.filesDir, file_name)
        if (!file.exists()) {
            return ""
        }

        try {
            return this.secrets.read_file(file_name, FileInputStream(file)).decodeToString()
        } catch (ex: UserNotAuthenticatedException) {
            Log.d(TAG, "User must authenticate. The key's validity timed out.")
            this.localAuth.showAuthWindow()
            return ""
        }
    }


    private fun delete_file(file_name: String): Boolean {
        val file = File(this.filesDir, file_name)
        if (!file.exists()) {
            return false
        }
        if (!this.localAuth.isAuthenticationStillValid(AUTH_REMEMBER_SECONDS)) {
            Log.d(TAG, "User must authenticate. The key's validity timed out.")
            this.localAuth.showAuthWindow()
            return false
        }

        file.delete()
        this.secrets.delete_key(file_name)
        return true
    }


    private fun get_files_list(): List<String> {
        val fileNames = mutableListOf<String>()
        val files = this.filesDir.listFiles()
        files?.forEach { file ->
            fileNames.add(file.name)
        }
        return fileNames
    }


    private fun update_files_list() {
        val files = get_files_list()
        if (files.isEmpty()) {
            this.binding.tvFilesList.text = ""
        } else {
            this.binding.tvFilesList.text = files.joinToString(separator = "\n")
        }
    }


}