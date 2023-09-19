package com.example.localpsych

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.localpsych.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding by lazy {
        _binding!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater)
        setViews()
        return binding.root
    }

    private fun setViews() {
        binding.apply {
            fragmentHomeBtHost.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_hostFragment) }
            fragmentHomeBtJoin.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_clientFragment) }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}