package com.myFile.transpose.view.fragment

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myFile.transpose.*
import com.myFile.transpose.view.Activity.Activity
import com.myFile.transpose.view.adapter.MyPlaylistRecyclerViewAdapter
import com.myFile.transpose.database.MyPlaylist
import com.myFile.transpose.databinding.FragmentMyPlaylistBinding
import com.myFile.transpose.others.constants.Actions.TAG
import com.myFile.transpose.view.dialog.DialogCreatePlaylist
import com.myFile.transpose.viewModel.MyPlaylistsViewModel
import com.myFile.transpose.viewModel.MyPlaylistsViewModelFactory
import com.myFile.transpose.viewModel.SharedViewModel

class MyPlaylistsFragment: Fragment() {
    var fbinding: FragmentMyPlaylistBinding? = null
    val binding get() = fbinding!!
    lateinit var activity: Activity

    private lateinit var myPlaylistRecyclerViewAdapter: MyPlaylistRecyclerViewAdapter

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var myPlaylistViewModel: MyPlaylistsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fbinding = FragmentMyPlaylistBinding.inflate(inflater, container, false)
        initViewModel()
        initPlaylistRecyclerView()
        binding.myPlaylistConstraintLayout.setOnClickListener {

        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        sharedViewModel.fromChildFragmentInNavFragment.value = findNavController().currentDestination?.id
        myPlaylistViewModel.getAllPlaylist()
    }
    private fun initViewModel(){
        val application = requireActivity().application as MyApplication
        val viewModelFactory = MyPlaylistsViewModelFactory(application)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        myPlaylistViewModel = ViewModelProvider(this, viewModelFactory)[MyPlaylistsViewModel::class.java]
    }

    private fun initObserver(){
        myPlaylistViewModel.myPlaylists.observe(viewLifecycleOwner){ myPlaylists ->
            val items = mutableListOf<MyPlaylistRecyclerViewAdapter.MyPlaylistItem>()
            items.add(MyPlaylistRecyclerViewAdapter.MyPlaylistItem.AddPlaylist)
            items.add(MyPlaylistRecyclerViewAdapter.MyPlaylistItem.MusicStorage)
            items.add(MyPlaylistRecyclerViewAdapter.MyPlaylistItem.VideoStorage)
            items.addAll(myPlaylists.map { MyPlaylistRecyclerViewAdapter.MyPlaylistItem.MyPlaylistData(it) })
            myPlaylistRecyclerViewAdapter.submitList(items)
        }

    }

    private fun deleteMyPlaylistByPosition(position: Int){
        val myPlaylist = myPlaylistViewModel.myPlaylists.value?.get(position)!!
        myPlaylistViewModel.deleteMyPlaylist(myPlaylist)
    }

    private fun addMyPlaylist(myPlaylist: MyPlaylist){
        myPlaylistViewModel.addMyPlaylist(myPlaylist)
    }


    private fun initPlaylistRecyclerView(){
        binding.playlistRecyclerView.layoutManager = LinearLayoutManager(activity)
        myPlaylistRecyclerViewAdapter = MyPlaylistRecyclerViewAdapter()
        myPlaylistRecyclerViewAdapter.setItemClickListener(object: MyPlaylistRecyclerViewAdapter.OnItemClickListener{
            override fun addPlaylistClick(v: View, position: Int) {
                showNoticeDialog()
            }

            override fun musicStorageClick(v: View, position: Int) {
                Log.d(TAG,"${activity.hasReadExternalStoragePermission()}")
                if (activity.hasReadExternalStoragePermission().not()){
                    activity.requestReadExternalStoragePermission()
                }
                else{
                    findNavController().navigate(R.id.myAudioFileItemsFragment)

                }
                Log.d(TAG,"스토리지 눌림")
            }

            override fun videoStorageClick(v: View, position: Int) {
                if (activity.hasReadExternalStoragePermission().not()){
                    activity.requestReadExternalStoragePermission()
                }
                else{
                    findNavController().navigate(R.id.myVideoFileItemsFragment)
                }
            }

            override fun onClick(v: View, position: Int) {
                val myPlaylists = myPlaylistViewModel.myPlaylists.value ?: return
                sharedViewModel.setMyPlaylistId(myPlaylists[position - 3].uid)
                sharedViewModel.myPlaylistTitle = myPlaylists[position - 3].playlistTitle
                findNavController().navigate(R.id.myPlaylistItemsFragment)
            }

            override fun optionButtonClick(v: View, position: Int) {
                Log.d(TAG,"${myPlaylistRecyclerViewAdapter.currentList[position - 3]}")
                val popUp = PopupMenu(activity, v)
                popUp.menuInflater.inflate(R.menu.my_playlist_pop_up_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete_my_playlist -> {
                            deleteMyPlaylistByPosition(position - 3)
                        }
                    }
                    true
                }
                popUp.show()
            }
        })
        binding.playlistRecyclerView.adapter = myPlaylistRecyclerViewAdapter
        binding.playlistRecyclerView.addItemDecoration(CustomItemDecoration(dpToPx(requireContext(), 56)))
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


            if (totalItemCount >= 7 && position == totalItemCount - 1) {  // 마지막 아이템인 경우
                outRect.bottom = space
            }
        }
    }



    private fun showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        val dialog = DialogCreatePlaylist()

        dialog.setListener(object: DialogCreatePlaylist.NoticeDialogListener {
            override fun onDialogPositiveClick(dialog: DialogFragment, text: Editable?) {
                addMyPlaylist(MyPlaylist(0, "$text"))
            }

            override fun onDialogNegativeClick(dialog: DialogFragment) {
                dialog.dismiss()}
        })
        dialog.show(activity.supportFragmentManager, "NoticeDialogFragment")
    }






    override fun onDestroyView() {
        super.onDestroyView()
        fbinding = null
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onDetach() {
        super.onDetach()

    }
}