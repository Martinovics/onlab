package com.onlab.oauth.interfaces

import android.net.Uri


interface IAddContentBottomFragmentListener : IAddDirectoryDialogListener {
    fun onFileBrowserItemSelected(uri: Uri)
}