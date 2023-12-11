package com.example.bike.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bike.R
import com.example.bike.databinding.FragmentChatBinding
import com.example.bike.databinding.FragmentProfileBinding

class ChatFragment : Fragment() {

    private lateinit var chatBinding: FragmentChatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chatBinding = FragmentChatBinding.inflate(inflater, container, false)

        return chatBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ChatFragment().apply { arguments = Bundle().apply {} }
    }
}