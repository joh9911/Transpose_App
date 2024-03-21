package com.myFile.transpose.view.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.myFile.transpose.MyApplication
import com.myFile.transpose.R
import com.myFile.transpose.databinding.FragmentHomeBinding
import com.myFile.transpose.databinding.FragmentLibraryBinding
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.SearchSuggestionKeywordRecyclerViewAdapter
import com.myFile.transpose.view.dialog.DialogPasteYoutubeLink
import com.myFile.transpose.viewModel.LibraryViewModel
import com.myFile.transpose.viewModel.LibraryViewModelFactory
import com.myFile.transpose.viewModel.SharedViewModel

class LibraryFragment: Fragment() {
    var fbinding: FragmentLibraryBinding? = null
    val binding get() = fbinding!!

    private lateinit var activity: Activity


    private var callback: OnBackPressedCallback? = null


    private lateinit var libraryViewModel: LibraryViewModel
    private lateinit var sharedViewModel: SharedViewModel

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentLibraryBinding.inflate(inflater, container, false)
        initNavController()
        initViewModel()

        return binding.root
    }

    private fun initNavController() {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.library_nav_host) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun initViewModel() {
        val application = requireActivity().application as MyApplication
        val viewModelFactory = LibraryViewModelFactory(application)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        libraryViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[LibraryViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity

    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback?.remove()
    }

    override fun onStart() {
        super.onStart()
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                when (navController.currentDestination?.id) {
                    R.id.myPlaylistsFragment -> {

                        isEnabled = false
                        activity.onBackPressed()

                    }
                    R.id.myPlaylistItemsFragment -> {
                        navController.navigate(R.id.myPlaylistsFragment)
                    }
                    R.id.searchResultFragment -> {
                        when (sharedViewModel.fromFragmentIdInLibraryNavFragment){
                            R.id.myPlaylistsFragment -> navController.navigate(R.id.myPlaylistsFragment)
                            R.id.myPlaylistItemsFragment -> navController.navigate(R.id.myPlaylistItemsFragment)
                            else -> navController.navigate(R.id.myPlaylistsFragment)
                        }
                    }
                    R.id.suggestionKeywordFragment -> {
                        navController.navigateUp()
                    }
                    R.id.myAudioFileItemsFragment -> {
                        navController.navigateUp()
                    }
                    R.id.myVideoFileItemsFragment -> {
                        Log.d("로그 확인","비디오 아이템 뒤로가기")
                        navController.navigateUp()
                    }
                    R.id.myAudioFileSearchFragment -> {
                        Log.d("로그 확인", "오디오 서치 아이템 뒤로가기")
                    }
                    R.id.myVideoFileSearchFragment -> {
                        Log.d("로그 확인","비디오 서치 아이템 뒤로가기")
                    }
                }

            }
        }
        callback?.let { activity.onBackPressedDispatcher.addCallback(this, it) }
    }
}