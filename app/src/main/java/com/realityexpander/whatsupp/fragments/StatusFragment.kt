package com.realityexpander.whatsupp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.databinding.FragmentStatusBinding


/**
 * A simple [Fragment] subclass.
 * Use the [StatusFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatusFragment : Fragment() {

    private var _bind: FragmentStatusBinding? = null
    private val bind: FragmentStatusBinding
        get() = _bind!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _bind = FragmentStatusBinding.inflate(inflater, container, false)
        return bind.root
    }

}