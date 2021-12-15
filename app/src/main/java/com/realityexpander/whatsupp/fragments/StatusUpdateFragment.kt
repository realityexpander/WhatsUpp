package com.realityexpander.whatsupp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.databinding.FragmentChatsBinding
import com.realityexpander.whatsupp.databinding.FragmentStatusUpdateBinding

/**
 * A simple [Fragment] subclass.
 * Use the [StatusUpdateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatusUpdateFragment : Fragment() {
    private var _bind: FragmentStatusUpdateBinding? = null
    private val bind: FragmentStatusUpdateBinding
        get() = _bind!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _bind = FragmentStatusUpdateBinding.inflate(inflater, container, false)
        return bind.root
    }

}