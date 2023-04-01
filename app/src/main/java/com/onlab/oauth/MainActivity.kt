package com.onlab.oauth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.onlab.oauth.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {


    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)

        this.update_files_list()
        set_save_click_listener()
        set_load_click_listener()
        set_delete_click_listener()

        setContentView(this.binding.root)
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
            this.write_file(
                file_name = this.binding.etSave.text.toString(),
                content = this.binding.etPlain.text.toString()
            )
            Log.d(TAG, "created file")
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
            this.binding.etPlain.setText(content)
        }
    }


    private fun set_delete_click_listener() {
        this.binding.btnDelete.setOnClickListener {
            if (this.binding.etLoad.text.isEmpty()) {
                Log.d(TAG, "must specify file name (delete)")
                return@setOnClickListener
            }
            if (this.delete_file(file_name = this.binding.etLoad.text.toString())) {
                Log.d(TAG, "deleted file")
                this.update_files_list()
            } else {
                Log.d(TAG, "could not delete file")
            }
        }
    }



    private fun write_file(file_name: String, content: String) {
        val file = File(this.filesDir, file_name)
        val outputStream = FileOutputStream(file)
        outputStream.write(content.toByteArray())
        outputStream.close()
    }


    private fun load_file(file_name: String): String {
        val file = File(this.filesDir, file_name)
        if (!file.exists()) {
            return ""
        }
        val inputStream = FileInputStream(file)
        val fileContents = inputStream.bufferedReader().use { it.readText() }
        inputStream.close()
        return fileContents
    }


    private fun delete_file(file_name: String): Boolean {
        val file = File(this.filesDir, file_name)
        if (!file.exists()) {
            return false
        }
        file.delete()
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