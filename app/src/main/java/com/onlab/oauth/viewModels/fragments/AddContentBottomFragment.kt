package com.onlab.oauth.viewModels.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.onlab.oauth.databinding.AddContentBottomSheetBinding
import com.onlab.oauth.interfaces.IAddDirectoryDialogListener


class AddContentBottomFragment(private val listener: IAddDirectoryDialogListener) : BottomSheetDialogFragment(), IAddDirectoryDialogListener {

    private val tag = "AddContentFragment"
    private var _binding: AddContentBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AddContentBottomSheetBinding.inflate(inflater)

        binding.btnAddDirectory.setOnClickListener {
            addDirectoryClickListener()
        }
        binding.btnUploadFile.setOnClickListener {
            uploadFileClickListener()
        }

        return binding.root
    }

    private fun addDirectoryClickListener() {
        Toast.makeText(context, "add directory", Toast.LENGTH_SHORT).show()
        val dialog = AddDirectoryDialogFragment(this)
        dialog.show(parentFragmentManager, "AddDirectoryDialogTag")
    }

    private fun uploadFileClickListener() {
        Toast.makeText(context, "upload file", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAddDirectoryDialogPositiveClicked(directoryName: String) {
        Log.d(tag, "onAddDirectoryDialogPositiveClicked -> adding directory: $directoryName")
        listener.onAddDirectoryDialogPositiveClicked(directoryName)
        dismiss()
    }

    override fun onAddDirectoryDialogNegativeClicked() {
        Log.d(tag, "onAddDirectoryDialogNegativeClicked -> directory won't be added")
        listener.onAddDirectoryDialogNegativeClicked()
        dismiss()
    }
}
