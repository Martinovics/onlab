package com.onlab.oauth.viewModels.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.onlab.oauth.databinding.AddContentBottomSheetBinding
import com.onlab.oauth.interfaces.IAddContentBottomFragmentListener
import com.onlab.oauth.interfaces.IAddDirectoryDialogListener


class AddContentBottomFragment(private val listener: IAddContentBottomFragmentListener) : BottomSheetDialogFragment(), IAddDirectoryDialogListener {

    private val tag = "AddContentFragment"
    private var _binding: AddContentBottomSheetBinding? = null
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    private val binding: AddContentBottomSheetBinding
        get() {
            return _binding!!
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // itt kell létrehozni, különben:
        // com.onlab.oauth.viewModels.activities.MainActivity@d5f1178 is attempting to
        // register while current state is RESUMED. LifecycleOwners must call register
        // before they are STARTED.
        filePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handleFilePick(result.data)
                }
            }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addDirectoryClickListener() {
        Log.d(tag, "add directory clicked")
        val dialog = AddDirectoryDialogFragment(this)
        dialog.show(parentFragmentManager, "AddDirectoryDialogTag")
    }

    private fun uploadFileClickListener() {
        Log.d(tag, "upload file clicked")
        val mimeTypes = arrayOf("*/*")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeTypes[0]
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }

        filePickerLauncher.launch(intent)
    }

    private fun handleFilePick(data: Intent?) {
        if (data?.data != null) {
            val uri = data.data!!
            Log.d(tag, "$uri is selected")
            listener.onFileBrowserItemSelected(uri)
        } else {
            Log.d(tag, "No data received (or it's null) from the file picker")
        }
        dismiss()
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
