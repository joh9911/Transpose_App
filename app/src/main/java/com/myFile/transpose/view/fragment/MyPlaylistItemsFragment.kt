package com.myFile.transpose.view.fragment

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myFile.transpose.*
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.databinding.FragmentMyPlaylistItemBinding
import com.myFile.transpose.data.model.NowPlaylistModel
import com.myFile.transpose.view.adapter.MyPlaylistItemsRecyclerViewAdapter
import com.myFile.transpose.viewModel.MyPlaylistItemsViewModel
import com.myFile.transpose.viewModel.MyPlaylistItemsViewModelFactory
import com.myFile.transpose.viewModel.SharedViewModel

class MyPlaylistItemsFragment(): Fragment() {

    var fbinding: FragmentMyPlaylistItemBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity

    private lateinit var myPlaylistItemRecyclerAdapter: MyPlaylistItemsRecyclerViewAdapter

    lateinit var viewModel: MyPlaylistItemsViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentMyPlaylistItemBinding.inflate(inflater, container, false)
        val view = binding.root
        initViewModel()
        initRecyclerView()
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.fromChildFragmentInNavFragment.value = findNavController().currentDestination?.id
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
            Log.d("마이 플레이리스트","$myPlaylistItems")
            myPlaylistItemRecyclerAdapter.submitList(myPlaylistItems.map{it.musicData}.toMutableList())
        }
    }


    private fun initRecyclerView(){
        binding.myPlaylistItemRecyclerView.layoutManager = LinearLayoutManager(activity)
        myPlaylistItemRecyclerAdapter = MyPlaylistItemsRecyclerViewAdapter()
        myPlaylistItemRecyclerAdapter.setItemClickListener(object: MyPlaylistItemsRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val myPlaylistItems = viewModel.myPlaylistItems.value ?: return
                val myPlaylistTitle = sharedViewModel.myPlaylistTitle
                val items = myPlaylistItems.map{it.musicData}
                val audioEffects = myPlaylistItems.map { it.audioEffects }
                val nowPlaylistModel = NowPlaylistModel(items, position, myPlaylistTitle, audioEffects)
                activity.activatePlayerInPlaylistMode(nowPlaylistModel)
            }

            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.delete_video_from_playlist_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete_video_from_playlist -> {
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
        val paddingInPixels = dpToPx(requireContext(), 56)
        binding.myPlaylistItemRecyclerView.addItemDecoration(CustomItemDecoration(paddingInPixels))
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    inner class CustomItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View,
            parent: RecyclerView, state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val totalItemCount = parent.adapter?.itemCount ?: 0


            if (position == totalItemCount - 1) {  // 마지막 아이템인 경우
                outRect.bottom = space
            }
        }
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
        fbinding = null
    }
}