package com.myFile.Transpose

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.myFile.Transpose.databinding.FragmentPlaylistBinding
import com.myFile.Transpose.databinding.MainBinding
import com.myFile.Transpose.model.PlayListVideoSearchData
import com.myFile.Transpose.model.PlaylistModel
import kotlinx.coroutines.*

class PlaylistItemsFragment(playListData: PlayListData): Fragment() {
    private val playListData = playListData

    val API_KEY = com.myFile.Transpose.BuildConfig.API_KEY
    lateinit var activity: Activity
    lateinit var playlistItemsRecyclerViewAdapter: PlaylistItemsRecyclerViewAdapter
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentPlaylistBinding? = null
    val binding get() = fbinding!!

    val playlistVideoData = arrayListOf<VideoData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentPlaylistBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        initView()
        getData()
        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    fun initView(){
        Glide.with(binding.playlistThumbnail)
            .load(playListData.thumbnail)
            .into(binding.playlistThumbnail)
        binding.playlistTitle.text = playListData.title
        binding.playlistDescription.text = playListData.description
        binding.playlistItemRecyclerView.layoutManager = LinearLayoutManager(activity)
        playlistItemsRecyclerViewAdapter = PlaylistItemsRecyclerViewAdapter()
        playlistItemsRecyclerViewAdapter.setItemClickListener(object: PlaylistItemsRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val playlistModel = PlaylistModel(playListData.title,playlistVideoData, position)
                activity.supportFragmentManager.beginTransaction()
                    .replace(activity.binding.playerFragment.id,PlayerFragment(playlistVideoData[position],playlistModel))
                    .commit()
            }

            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.video_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.add_my_playlist -> {
                            showNoticeDialog(playlistVideoData[position])
                        }
                    }
                    true
                })
                popUp.show()
            }

        })
        binding.playlistItemRecyclerView.adapter = playlistItemsRecyclerViewAdapter
    }
    fun showNoticeDialog(videoData: VideoData) {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogFragmentPopupAddPlaylist(videoData)
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }

    fun getData(){
        CoroutineScope(Dispatchers.IO + CoroutineExceptionObject.coroutineExceptionHandler).launch {
            getPlaylistItemsData(null)
        }
    }

    private suspend fun getPlaylistItemsData(nextPageToken: String?){
        val retrofit = RetrofitData.initRetrofit()
        val response = retrofit.create(RetrofitService::class.java).getPlayListVideoItems(API_KEY,"snippet",playListData.playlistId,nextPageToken,"50")
        if (response.isSuccessful){
            if (response.body()?.items?.size != 0) {
                withContext(Dispatchers.Main) {
                    playlistItemsMapper(response.body()!!)
                }
            }
        }
    }
    private fun playlistItemsMapper(playlistItemsResponse: PlayListVideoSearchData) {
        for (index in 0 until playlistItemsResponse.items.size!!) {
            Log.d("맴퍼","진행햇음")
            val thumbnail =
                playlistItemsResponse.items[index].snippet?.thumbnails?.high?.url!!
            val date =
                playlistItemsResponse.items[index].snippet?.publishedAt!!
            val channelTitle = playlistItemsResponse.items[index].snippet?.videoOwnerChannelTitle?.replace(" - Topic", "")!!
            val title = stringToHtmlSign(playlistItemsResponse.items[index].snippet?.title!!)
            val videoId = playlistItemsResponse.items[index].snippet?.resourceId?.videoId!!
            val channelId = playlistItemsResponse.items[index].snippet?.channelId!!
            playlistVideoData.add(VideoData(thumbnail, title, channelTitle, channelId, videoId, date,  false))
        }
        playlistItemsRecyclerViewAdapter.submitList(playlistVideoData.toMutableList())
        binding.playlistProgressBar.visibility = View.GONE
        binding.playlistItemRecyclerView.visibility = View.VISIBLE
    }

    private fun stringToHtmlSign(str: String): String {
        return str.replace("&amp;".toRegex(), "[&]")
            .replace("[<]".toRegex(), "&lt;")
            .replace("[>]".toRegex(), "&gt;")
            .replace("&quot;".toRegex(), "'")
            .replace("&#39;".toRegex(), "'")
    }
    override fun onDestroy() {
        super.onDestroy()
        fbinding = null
    }
}