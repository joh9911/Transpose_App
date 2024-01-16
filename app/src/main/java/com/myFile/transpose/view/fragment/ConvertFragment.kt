package com.myFile.transpose.view.fragment

import android.content.Context

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.myFile.transpose.R
import com.myFile.transpose.databinding.FragmentConvertBinding

import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.viewModel.SharedViewModel

class ConvertFragment: Fragment() {
    var fbinding: FragmentConvertBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity

    lateinit var navController: NavController

    private lateinit var callback: OnBackPressedCallback

    private lateinit var sharedViewModel: SharedViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentConvertBinding.inflate(inflater, container, false)
        initViewModel()
        initNavController()
        return binding.root
    }

    private fun initViewModel(){
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

    }


    private fun initNavController(){
        val navHostFragment = childFragmentManager.findFragmentById(R.id.home_nav_host) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onStart() {
        super.onStart()
        Log.d("홈 프레그멈ㄴ트", "온스타트")
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                when (navController.currentDestination?.id) {
                    R.id.audioEditFragment -> {
                        isEnabled = false
                        activity.onBackPressed()
                    }
                    R.id.suggestionKeywordFragment -> {
                        navController.navigateUp()
                    }
                    R.id.searchResultFragment -> {
                        navController.navigate(R.id.audioEditFragment)
                    }
                }

            Log.d("백프레스", "프레그먼트 눌림")
            }
        }
        activity.onBackPressedDispatcher.addCallback(this, callback)
    }



}