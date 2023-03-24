package com.onlab.oauth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.onlab.oauth.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {


    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.binding.root)
    }










}