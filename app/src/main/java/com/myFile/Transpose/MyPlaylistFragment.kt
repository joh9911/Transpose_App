package com.myFile.Transpose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.myFile.Transpose.databinding.FragmentMyPlaylistBinding
import com.myFile.Transpose.databinding.FragmentMyPlaylistSortBinding
import com.myFile.Transpose.databinding.MainBinding

class MyPlaylistFragment: Fragment() {
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentMyPlaylistBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentMyPlaylistBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }
}