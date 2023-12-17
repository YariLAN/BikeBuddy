package com.example.bike.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bike.databinding.FragmentSearchMeetingBinding

class SearchMeetingFragment : Fragment() {

    private lateinit var searchBinding: FragmentSearchMeetingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        searchBinding = FragmentSearchMeetingBinding.inflate(inflater, container, false)

        return searchBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchMeetingFragment().apply { arguments = Bundle().apply {} }
    }
}