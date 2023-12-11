package com.example.bike.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bike.databinding.FragmentMeetingBinding
import com.example.bike.databinding.FragmentSearchMeetingBinding

class MeetingFragment : Fragment() {

    private lateinit var meetingBinding: FragmentMeetingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        meetingBinding = FragmentMeetingBinding.inflate(inflater, container, false)

        return meetingBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ChatFragment().apply { arguments = Bundle().apply {} }
    }
}