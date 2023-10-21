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
import com.onlab.oauth.databinding.ManageFileBottomSheetBinding
import com.onlab.oauth.interfaces.IManageFileBottomFragmentListener


class ManageFileBottomFragment(private val listener: IManageFileBottomFragmentListener, private val position: Int) : BottomSheetDialogFragment() {

    private val tag = "ManageFileFragment"
    private var _binding: ManageFileBottomSheetBinding? = null
    private lateinit var manageKeyLauncher: ActivityResultLauncher<Intent>

    private val binding: ManageFileBottomSheetBinding
        get() {
            return _binding!!
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manageKeyLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handleManageKey(result.data)
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ManageFileBottomSheetBinding.inflate(inflater)

        binding.llDownload.setOnClickListener { onManageFileDownloadButtonClicked() }
        binding.llShare.setOnClickListener { onManageFileShareButtonClicked() }
        binding.llManageKey.setOnClickListener { onManageFileManageKeyButtonClicked() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun handleManageKey(data: Intent?) {
        // todo: implement
        dismiss()
    }

    private fun onManageFileDownloadButtonClicked() {
        Log.d(tag, "onManageFileDownloadButtonClicked called")
        listener.onManageFileDownloadButtonClicked(position)
        dismiss()
    }

    private fun onManageFileShareButtonClicked() {
        Log.d(tag, "onManageFileShareButtonClicked called")
        listener.onManageFileShareButtonClicked(position)
    }

    private fun onManageFileManageKeyButtonClicked() {
        Log.d(tag, "onManageFileManageKeyButtonClicked called")
        listener.onManageFileManageKeyButtonClicked(position)
    }
}
