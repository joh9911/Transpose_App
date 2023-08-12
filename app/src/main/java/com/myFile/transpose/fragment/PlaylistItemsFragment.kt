package com.myFile.transpose.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myFile.transpose.*
import com.myFile.transpose.adapter.PlaylistItemsRecyclerViewAdapter
import com.myFile.transpose.databinding.FragmentPlaylistBinding
import com.myFile.transpose.databinding.MainBinding
import com.myFile.transpose.dialog.DialogFragmentPopupAddPlaylist
import com.myFile.transpose.model.PlaylistDataModel
import com.myFile.transpose.model.VideoDataModel
import com.myFile.transpose.viewModel.PlaylistItemsViewModel
import com.myFile.transpose.viewModel.PlaylistItemsViewModelFactory
import com.myFile.transpose.viewModel.SharedViewModel

class PlaylistItemsFragment: Fragment() {
    lateinit var activity: Activity
    lateinit var playlistItemsRecyclerViewAdapter: PlaylistItemsRecyclerViewAdapter
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentPlaylistBinding? = null
    val binding get() = fbinding!!

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var playlistItemsViewModel: PlaylistItemsViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentPlaylistBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        initViewModel()
        initRecyclerView()
        return view
    }
    private fun initViewModel(){
        val application = requireActivity().application as MyApplication
        val viewModelFactory = PlaylistItemsViewModelFactory(application.youtubeDataRepository)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        playlistItemsViewModel = ViewModelProvider(this, viewModelFactory)[PlaylistItemsViewModel::class.java]
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
    }

    private fun initObserver(){
        sharedViewModel.playlistData.observe(viewLifecycleOwner){
            addRecyclerViewHeaderView(it)
            playlistItemsViewModel.fetchPlaylistItemsData(it, resources.getStringArray(R.array.publish_date_formats))
        }
        playlistItemsViewModel.playlistItems.observe(viewLifecycleOwner){ playlistItems ->
            loadingFinishEvent()
            addRecyclerViewItemView(playlistItems)
        }
    }

    private fun addRecyclerViewHeaderView(playlistDataModel: PlaylistDataModel){
        val currentList = playlistItemsRecyclerViewAdapter.currentList.toMutableList()
        currentList.add(PlaylistItemsRecyclerViewAdapter.PlaylistItemsRecyclerViewItems.HeaderTitleData(playlistDataModel))
        playlistItemsRecyclerViewAdapter.submitList(currentList)
    }

    private fun addRecyclerViewItemView(playlistItems: List<VideoDataModel>) {
        val currentList = playlistItemsRecyclerViewAdapter.currentList.toMutableList()
        val loadingData = PlaylistItemsRecyclerViewAdapter.PlaylistItemsRecyclerViewItems.LoadingData
        if (currentList.isNotEmpty() && currentList.last() == loadingData)
            currentList.removeLast()
        currentList.addAll(playlistItems.map{PlaylistItemsRecyclerViewAdapter.PlaylistItemsRecyclerViewItems.ItemData(it)})
        if (playlistItemsViewModel.nextPageToken != null)
            currentList.add(loadingData)
        playlistItemsRecyclerViewAdapter.submitList(currentList)
    }

    private fun loadingFinishEvent(){
        binding.progressBar.visibility = View.GONE
        binding.playlistItemRecyclerView.visibility = View.VISIBLE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    private fun initRecyclerView(){
        binding.playlistItemRecyclerView.layoutManager = LinearLayoutManager(activity)
        playlistItemsRecyclerViewAdapter = PlaylistItemsRecyclerViewAdapter()
        playlistItemsRecyclerViewAdapter.setItemClickListener(object: PlaylistItemsRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val playlistData = sharedViewModel.playlistData.value
                val playlistItems = playlistItemsViewModel.playlistItems.value ?: return
                val nowPlaylistModel = NowPlaylistModel(playlistItems, position - 1, playlistData?.title)
                val videoData = playlistItems[position - 1]
                sharedViewModel.setVideoPlayerFragmentData(videoData, nowPlaylistModel)
                activity.executeVideoPlayerFragment(SharedViewModel.PlaybackMode.PLAYLIST)

            }

            override fun optionButtonClick(v: View, position: Int) {
                val playlistItems = playlistItemsViewModel.playlistItems.value
                if (playlistItems != null){
                    val popUp = PopupMenu(activity, v)
                    popUp.menuInflater.inflate(R.menu.video_pop_up_menu, popUp.menu)
                    popUp.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.add_my_playlist -> {
                                showNoticeDialog(playlistItems[position])
                            }
                        }
                        true
                    }
                    popUp.show()
                }
            }

        })
        binding.playlistItemRecyclerView.adapter = playlistItemsRecyclerViewAdapter

        binding.playlistItemRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount - 1
                val currentList = playlistItemsViewModel.playlistItems.value ?: arrayListOf()
                // 스크롤이 끝에 도달했는지 확인
                if (!binding.playlistItemRecyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
                    Log.d("스크롤 끝에","도달!")
                    if (currentList.isNotEmpty()){
                        playlistItemsViewModel.nextPageToken ?: return
                        val playlistDataModel = sharedViewModel.playlistData.value ?: return
                        playlistItemsViewModel.fetchPlaylistItemsData(playlistDataModel, resources.getStringArray(R.array.publish_date_formats))
                    }
                }
            }
        })
    }

    fun showNoticeDialog(videoData: VideoDataModel) {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogFragmentPopupAddPlaylist(videoData)
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fbinding = null
    }
}