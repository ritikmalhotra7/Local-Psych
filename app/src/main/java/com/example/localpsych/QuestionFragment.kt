package com.example.localpsych

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.localpsych.databinding.FragmentQuestionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


class QuestionFragment : Fragment() {
    private var _binding: FragmentQuestionBinding? = null
    private val binding by lazy {
        _binding!!
    }
    private val random = UUID.randomUUID().toString()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestionBinding.inflate(inflater)
        try{
            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    SocketHandler.socket?.getOutputStream()?.apply {
                        write(random.toByteArray())
                    }?.flush()
                    val buffer = ByteArray(1024)
                    val data = SocketHandler.socket?.getInputStream()?.read(buffer)
                    withContext(Dispatchers.Main){
                        binding.fragmentQuestionTvQuestion.text = data.toString()
                    }
                }
            }
        }catch (e:Exception){
            Log.d("taget",e.toString())
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}