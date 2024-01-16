package com.myFile.transpose.view.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

import com.myFile.transpose.*
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.databinding.FragmentHomeBinding
import com.myFile.transpose.view.dialog.DialogFragmentPopupAddPlaylist
import com.myFile.transpose.data.model.VideoDataModel
import com.myFile.transpose.viewModel.HomeFragmentViewModelFactory
import com.myFile.transpose.viewModel.HomeViewModel
import com.myFile.transpose.viewModel.SharedViewModel



class HomeFragment: Fragment() {

    var fbinding: FragmentHomeBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity


    private lateinit var callback: OnBackPressedCallback

    private lateinit var viewModel: HomeViewModel
    private lateinit var sharedViewModel: SharedViewModel

    lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentHomeBinding.inflate(inflater, container, false)
        Log.d("홈프레그먼트의","온크레이트")
        initNavController()
        initViewModel()

        return binding.root
    }

    private fun initNavController(){
        val navHostFragment = childFragmentManager.findFragmentById(R.id.home_nav_host) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initViewModel(){
        val application = requireActivity().application as MyApplication
        val viewModelFactory = HomeFragmentViewModelFactory(application)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[HomeViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }


    fun showNoticeDialog(videoData: VideoDataModel) {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogFragmentPopupAddPlaylist(videoData)
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity


    }

    override fun onStart() {
        super.onStart()
        Log.d("홈 프레그멈ㄴ트", "온스타트")
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                when (navController.currentDestination?.id) {
                    R.id.playlistFragment -> {
                        isEnabled = false
                        activity.onBackPressed()
                    }
                    R.id.playlistItemsFragment -> {
                        navController.navigate(R.id.action_playlistItemsFragment_to_playlistFragment)
                    }
                    R.id.searchResultFragment -> {
                        when (sharedViewModel.fromFragmentIdInHomeNavFragment){
                            R.id.playlistFragment -> {
                                navController.navigate(R.id.playlistFragment)
                            }
                            R.id.playlistItemsFragment -> {
                                navController.navigate(R.id.playlistItemsFragment)
                            }
                            else -> {
                                navController.navigate(R.id.playlistFragment)
                            }
                        }
                    }
                    R.id.suggestionKeywordFragment -> {
                        navController.navigateUp()
                    }
                }
            }
        }
        activity.onBackPressedDispatcher.addCallback(this, callback)
    }


    override fun onDetach() {
        super.onDetach()
        Log.d("홈프레그먼트","onDetach")
//        callback.remove()
    }

    override fun onDestroy() {
        Log.d("홈프레그먼트","onDestroy")
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("홈프레그먼트","onDestroyView")
        fbinding = null
    }

    override fun onStop() {
        Log.d("홈프레그먼트","onStop")
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        Log.d("홈프레그먼트","온리즘")
    }
}