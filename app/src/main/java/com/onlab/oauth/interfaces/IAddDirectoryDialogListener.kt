package com.onlab.oauth.interfaces

interface IAddDirectoryDialogListener {
    fun onAddDirectoryDialogPositiveClicked(directoryName: String)
    fun onAddDirectoryDialogNegativeClicked()
}