package com.realityexpander.whatsupp.interfaces

import com.realityexpander.whatsupp.fragments.BaseFragment

// HomeContextI shared with the fragments
interface HostContextI {
    fun onAndroidFragmentCreated(fragment: BaseFragment)
}