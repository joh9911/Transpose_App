package com.myFile.transpose.view.fragment

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
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.ChannelVideoRecyclerViewAdapter
import com.myFile.transpose.databinding.FragmentChannelBinding
import com.myFile.transpose.view.dialog.DialogFragmentPopupAddPlaylist
import com.myFile.transpose.model.model.ChannelDataModel

import com.myFile.transpose.model.model.VideoDataModel
import com.myFile.transpose.viewModel.ChannelViewModel
import com.myFile.transpose.viewModel.ChannelViewModelFactory
import com.myFile.transpose.viewModel.SharedViewModel

class ChannelFragment: Fragment() {
    var fbinding: FragmentChannelBinding? = null
    val binding get() = fbinding!!
    lateinit var channelVideoRecyclerViewAdapter: ChannelVideoRecyclerViewAdapter
    lateinit var activity: Activity

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var channelViewModel: ChannelViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentChannelBinding.inflate(inflater, container, false)
        val view = binding.root
        initViewModel()
        initRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
    }

    private fun initViewModel(){
        val application = requireActivity().application as MyApplication
        val viewModelFactory = ChannelViewModelFactory(application)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        channelViewModel = ViewModelProvider(this, viewModelFactory)[ChannelViewModel::class.java]
    }

    private fun initObserver(){
        sharedViewModel.channelData.observe(viewLifecycleOwner){
            channelViewModel.fetchChannelVideoData(it)
            addRecyclerViewHeaderView(it)
        }

        channelViewModel.channelVideoDataList.observe(viewLifecycleOwner){
            addRecyclerViewItemView(channelViewModel.channelVideoDataList.value ?: arrayListOf())
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun addRecyclerViewHeaderView(channelDataModel: ChannelDataModel?){
        val currentList = channelVideoRecyclerViewAdapter.currentList.toMutableList()
        val headerView = ChannelVideoRecyclerViewAdapter.ChannelFragmentRecyclerViewItem.HeaderTitleData(channelDataModel)
        currentList.add(headerView)
        channelVideoRecyclerViewAdapter.submitList(currentList)
    }

    private fun addRecyclerViewItemView(currentVideoList: ArrayList<VideoDataModel>){
        val currentList = channelVideoRecyclerViewAdapter.currentList.toMutableList()
        val loadingData = ChannelVideoRecyclerViewAdapter.ChannelFragmentRecyclerViewItem.LoadingData
        if (currentList.isNotEmpty() && currentList.last() == loadingData)
            currentList.removeLast()
        val existingData = channelVideoRecyclerViewAdapter.currentList.filterIsInstance<ChannelVideoRecyclerViewAdapter.ChannelFragmentRecyclerViewItem.ItemData>().map { it.videoData }.toHashSet()
        val newItems = currentVideoList.filter { it !in existingData }.map{ChannelVideoRecyclerViewAdapter.ChannelFragmentRecyclerViewItem.ItemData(it)}
        currentList.addAll(newItems)
        if (channelViewModel.nextPageToken != null)
            currentList.add(loadingData)
        channelVideoRecyclerViewAdapter.submitList(currentList)
    }

    override fun onResume() {
        super.onResume()
        Log.d("채널프레그먼트","onResume${parentFragment}")
    }

    private fun initRecyclerView(){
        binding.videoRecyclerView.layoutManager = LinearLayoutManager(context)
        channelVideoRecyclerViewAdapter = ChannelVideoRecyclerViewAdapter()
        channelVideoRecyclerViewAdapter.setItemClickListener(object: ChannelVideoRecyclerViewAdapter.OnItemClickListener{
            override fun videoClick(v: View, position: Int) {
                val currentList = channelViewModel.channelVideoDataList.value ?: return
                Log.d("현재 비디오 리스트","${currentList.size}")
                Log.d("해당 포지션 클릭","$position")
                sharedViewModel.setSingleModeVideoId(currentList[position - 1].videoId)
                activity.executeVideoPlayerFragment(SharedViewModel.PlaybackMode.SINGLE_VIDEO)
            }
            override fun optionButtonClick(v: View, position: Int) {
                val currentList = channelViewModel.channelVideoDataList.value ?: return
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.video_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.add_my_playlist -> {
                            showNoticeDialog(currentList[position - 1])
                        }
                    }
                    true
                }
                popUp.show()
            }
        })
        binding.videoRecyclerView.adapter = channelVideoRecyclerViewAdapter

        binding.videoRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount - 1

                // 스크롤이 끝에 도달했는지 확인
                if (!binding.videoRecyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
                    Log.d("스크롤 끝에","도달!")
                    val channelData = sharedViewModel.channelData.value ?: return
                    channelViewModel.nextPageToken ?: return
                    channelViewModel.fetchChannelVideoData(channelData)
                }
            }
        })
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d("채널프레그먼트","onDestroy")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        fbinding = null
    }

}