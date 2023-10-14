package com.onlab.oauth.viewModels.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.onlab.oauth.databinding.AddContentBottomSheetBinding


class AddContentBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: AddContentBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = AddContentBottomSheetBinding.inflate(inflater)

        binding.btnAddDirectory.setOnClickListener {
            Toast.makeText(context, "Clicked!", Toast.LENGTH_SHORT).show()
        }

        binding.btnUploadFile.setOnClickListener {
            Toast.makeText(context, "Clicked!", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
