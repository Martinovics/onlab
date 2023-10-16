package com.onlab.oauth.viewModels.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.onlab.oauth.databinding.AddDirectoryDialogBinding
import com.onlab.oauth.interfaces.IAddDirectoryDialogListener


class AddDirectoryDialogFragment(private val listener: IAddDirectoryDialogListener) : DialogFragment() {

    private var _binding: AddDirectoryDialogBinding? = null
    private val binding: AddDirectoryDialogBinding
        get() {
            return _binding!!
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = AddDirectoryDialogBinding.inflate(layoutInflater)

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
                .setMessage("Add new directory")
                .setPositiveButton("OK") { _, _ ->
                    listener.onAddDirectoryDialogPositiveClicked(binding.etDirectoryName.text.toString())
                }
                .setNegativeButton("Cancel") { _, _ ->
                    listener.onAddDirectoryDialogNegativeClicked()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
