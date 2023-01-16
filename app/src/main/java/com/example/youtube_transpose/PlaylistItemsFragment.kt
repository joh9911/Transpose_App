package com.example.youtube_transpose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.youtube_transpose.databinding.FragmentPlayerBinding
import com.example.youtube_transpose.databinding.FragmentPlaylistBinding
import com.example.youtube_transpose.databinding.FragmentSearchResultBinding
import com.example.youtube_transpose.databinding.MainBinding

class PlaylistItemsFragment: Fragment() {
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentPlaylistBinding? = null
    val binding get() = fbinding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentPlaylistBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }
}