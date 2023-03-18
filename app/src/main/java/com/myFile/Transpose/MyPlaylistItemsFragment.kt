package com.myFile.Transpose

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.myFile.Transpose.databinding.FragmentMyPlaylistBinding
import com.myFile.Transpose.databinding.FragmentMyPlaylistItemBinding
import com.myFile.Transpose.databinding.MainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPlaylistItemsFragment(private val playlistId: Int): Fragment() {
    lateinit var mainBinding: MainBinding
    var fbinding: FragmentMyPlaylistItemBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity
    var myMusics = listOf<Musics>()
    var myMusicItems = arrayListOf<VideoData>()
    val channelDataList = arrayListOf<ChannelData>() // 재생 프레그먼트에 넣기 위한 그냥 빈 리스트

    lateinit var db: AppDatabase
    lateinit var myPlaylistDao: MyPlaylistDao


    lateinit var myPlaylistItemRecyclerAdapter: MyPlaylistItemRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fbinding = FragmentMyPlaylistItemBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        initDb()
        initRecyclerView()
        getAllMusic()
        return view
    }
    fun initDb(){
        db = Room.databaseBuilder(
            activity,
            AppDatabase::class.java, "database-name"
        )
            .build()
        myPlaylistDao = db.myPlaylistDao()

    }
    fun getAllMusic(){
        CoroutineScope(Dispatchers.IO).launch{
            myMusics = myPlaylistDao.getMusicItemsByPlaylistId(playlistId)
            withContext(Dispatchers.Main){
                myMusicItems.clear()
                myMusics.forEach{myMusicItems.add(it.musicData)}
                myPlaylistItemRecyclerAdapter.submitList(myMusicItems.toMutableList())
            }
        }
    }

    fun initRecyclerView(){
        binding.myPlaylistItemRecyclerView.layoutManager = LinearLayoutManager(activity)
        myPlaylistItemRecyclerAdapter = MyPlaylistItemRecyclerViewAdapter()
        myPlaylistItemRecyclerAdapter.setItemClickListener(object: MyPlaylistItemRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                activity.supportFragmentManager.beginTransaction()
                    .replace(
                        activity.binding.playerFragment.id,
                        PlayerFragment(
                            myMusicItems,

                            position

                        ),
                        "playerFragment"
                    )
                    .commit()
            }

            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.my_playlist_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete_my_playlist -> {
                            CoroutineScope(Dispatchers.IO).launch{
                                myPlaylistDao.deleteMusic(myMusics[position])
                                getAllMusic()
                            }
                        }
                    }
                    true
                })
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
        fbinding = null
    }
}