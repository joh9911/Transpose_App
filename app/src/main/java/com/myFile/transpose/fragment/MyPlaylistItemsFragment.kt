package com.myFile.transpose.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.myFile.transpose.*
import com.myFile.transpose.retrofit.VideoData
import com.myFile.transpose.adapter.MyPlaylistItemRecyclerViewAdapter
import com.myFile.transpose.database.AppDatabase
import com.myFile.transpose.database.Musics
import com.myFile.transpose.database.MyPlaylist
import com.myFile.transpose.database.MyPlaylistDao
import com.myFile.transpose.databinding.FragmentMyPlaylistItemBinding
import com.myFile.transpose.databinding.MainBinding
import com.myFile.transpose.model.PlayerFragmentBundle
import com.myFile.transpose.model.PlaylistModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class MyPlaylistItemsFragment(): Fragment() {

    var myPlaylistUid = 0
    lateinit var myPlaylistTitle: String

    lateinit var mainBinding: MainBinding
    var fbinding: FragmentMyPlaylistItemBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity
    var myMusics = listOf<Musics>()
    var myMusicItems = arrayListOf<VideoData>()

    lateinit var db: AppDatabase
    lateinit var myPlaylistDao: MyPlaylistDao

    lateinit var myPlaylistItemRecyclerAdapter: MyPlaylistItemRecyclerViewAdapter
    private lateinit var fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentMyPlaylistItemBinding.inflate(inflater, container, false)
        mainBinding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        getPlaylistDetailFromMyPlaylistFragment()
        initDb()
        initRecyclerView()
        getAllMusic()
        initEmptyItemVisible()
        return view
    }

    fun getPlaylistDetailFromMyPlaylistFragment(){
        myPlaylistUid = arguments?.getInt("playlistUid")!!
        myPlaylistTitle = arguments?.getString("playlistTitle")!!
    }

    fun initDb(){
        db = AppDatabase.getDatabase(activity)
        myPlaylistDao = db.myPlaylistDao()

    }
    fun getAllMusic(){
        CoroutineScope(Dispatchers.IO).launch{
            myMusics = myPlaylistDao.getMusicItemsByPlaylistId(myPlaylistUid)
            withContext(Dispatchers.Main){
                if (myMusics.isEmpty())
                    binding.emptyTextView.visibility = View.VISIBLE
                myMusicItems.clear()
                myMusics.forEach{myMusicItems.add(it.musicData)}
                myPlaylistItemRecyclerAdapter.submitList(myMusicItems.toMutableList())
            }
        }
    }

    /**
     * PlayerFragment에 의해 아이템이 가려지지 않도록 visible 설정
     */
    private fun initEmptyItemVisible(){
        for (fragment in activity.supportFragmentManager.fragments){
            if (fragment is PlayerFragment){
                binding.emptyItem.visibility = View.VISIBLE
            }
        }
    }

    private fun initRecyclerView(){
        binding.myPlaylistItemRecyclerView.layoutManager = LinearLayoutManager(activity)
        myPlaylistItemRecyclerAdapter = MyPlaylistItemRecyclerViewAdapter()
        myPlaylistItemRecyclerAdapter.setItemClickListener(object: MyPlaylistItemRecyclerViewAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {

                val playlistModel = PlaylistModel(myPlaylistTitle, myMusicItems, position)
                val videoData = myMusicItems[position]
                val playerFragmentBundle = PlayerFragmentBundle(videoData, playlistModel)

                val bundle = Bundle().apply {
                    putParcelable("playerFragment", playerFragmentBundle)
                }
                val playerFragment = PlayerFragment().apply {
                    arguments = bundle
                }

                activity.supportFragmentManager.beginTransaction()
                    .replace(
                        activity.binding.playerFragment.id,
                        playerFragment
                    )
                    .commit()
            }

            override fun optionButtonClick(v: View, position: Int) {
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.my_playlist_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete_my_playlist -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                myPlaylistDao.deleteMusic(myMusics[position])
                                getAllMusic()
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
        fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {

            override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                if (f is PlayerFragment) {
                    binding.emptyItem.visibility = View.VISIBLE
                }
            }

            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (f is PlayerFragment) {
                    binding.emptyItem.visibility = View.GONE
                }
            }
        }
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks,false)


    }
    override fun onDestroy() {
        super.onDestroy()
        fbinding = null
        activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
    }
}