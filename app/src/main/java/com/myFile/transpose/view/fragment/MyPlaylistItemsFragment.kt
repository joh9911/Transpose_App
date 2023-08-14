package com.myFile.transpose.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.myFile.transpose.*
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.MyPlaylistItemRecyclerViewAdapter
import com.myFile.transpose.databinding.FragmentMyPlaylistItemBinding
import com.myFile.transpose.databinding.MainBinding
import com.myFile.transpose.model.model.NowPlaylistModel
import com.myFile.transpose.viewModel.MyPlaylistItemsViewModel
import com.myFile.transpose.viewModel.MyPlaylistItemsViewModelFactory
import com.myFile.transpose.viewModel.SharedViewModel

class MyPlaylistItemsFragment(): Fragment() {

    lateinit var mainBinding: MainBinding
    var fbinding: FragmentMyPlaylistItemBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity

    private lateinit var myPlaylistItemRecyclerAdapter: MyPlaylistItemRecyclerViewAdapter
    private lateinit var fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks

    lateinit var viewModel: MyPlaylistItemsViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentMyPlaylistItemBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        initViewModel()
        initRecyclerView()
        initEmptyItemVisible()
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {

            override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                if (f is VideoPlayerFragment) {
                    binding.emptyItem.visibility = View.VISIBLE
                }
            }

            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (f is VideoPlayerFragment) {
                    binding.emptyItem.visibility = View.GONE
                }
            }
        }
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks,false)
        initObserver()
    }

    private fun initViewModel(){
        val application = requireActivity().application as MyApplication
        val viewModelFactory = MyPlaylistItemsViewModelFactory(application.myPlaylistRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MyPlaylistItemsViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    private fun initObserver(){
        sharedViewModel.myPlaylistId.observe(viewLifecycleOwner){
            viewModel.getPlaylistItemsByPlaylistId(it)
        }
        viewModel.myPlaylistItems.observe(viewLifecycleOwner){ myPlaylistItems ->
            myPlaylistItemRecyclerAdapter.submitList(myPlaylistItems.map{it.musicData}.toMutableList())
        }
    }

    /**
     * PlayerFragment에 의해 아이템이 가려지지 않도록 visible 설정
     */
    private fun initEmptyItemVisible(){
        for (fragment in activity.supportFragmentManager.fragments){
            if (fragment is VideoPlayerFragment){
                binding.emptyItem.visibility = View.VISIBLE
            }
        }
    }

    private fun initRecyclerView(){
        binding.myPlaylistItemRecyclerView.layoutManager = LinearLayoutManager(activity)
        myPlaylistItemRecyclerAdapter = MyPlaylistItemRecyclerViewAdapter()
        myPlaylistItemRecyclerAdapter.setItemClickListener(object: MyPlaylistItemRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val myPlaylistItems = viewModel.myPlaylistItems.value ?: return
                val myPlaylistTitle = sharedViewModel.myPlaylistTitle
                val items = myPlaylistItems.map{it.musicData}
                val playlistModel = NowPlaylistModel(items, position, myPlaylistTitle)
                val videoData = items[position]
                sharedViewModel.setVideoPlayerFragmentData(videoData, playlistModel)

                activity.executeVideoPlayerFragment(SharedViewModel.PlaybackMode.PLAYLIST)
            }

            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.my_playlist_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete_my_playlist -> {
                            viewModel.myPlaylistItems.value?.let { items ->
                                viewModel.deletePlaylistItem(items[position], sharedViewModel.myPlaylistId.value ?: 0)
                            }
                        }
                    }
                    true
                }
                popUp.show()
            }
        })
        binding.myPlaylistItemRecyclerView.adapter = myPlaylistItemRecyclerAdapter
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
        fbinding = null
    }
}