package com.myFile.transpose.dialog

import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.myFile.transpose.MyApplication
import com.myFile.transpose.R
import com.myFile.transpose.adapter.MyPlaylistRecyclerViewAdapter
import com.myFile.transpose.database.AppDatabase
import com.myFile.transpose.database.Musics
import com.myFile.transpose.database.MyPlaylist
import com.myFile.transpose.database.MyPlaylistDao
import com.myFile.transpose.model.VideoDataModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DialogFragmentPopupAddPlaylist(val videoData: VideoDataModel): DialogFragment() {

    private lateinit var viewModel: DialogFragmentPopupAddPlaylistViewModel
    private lateinit var myPlaylistRecyclerViewAdapter: MyPlaylistRecyclerViewAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            getMyPlaylist()
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;
            val dialogView = inflater.inflate(R.layout.dialog_popup_menu_add_playlist, null)
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.playlist_recycler_view)
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
            myPlaylistRecyclerViewAdapter = MyPlaylistRecyclerViewAdapter()
            myPlaylistRecyclerViewAdapter.setItemClickListener(object: MyPlaylistRecyclerViewAdapter.OnItemClickListener{
                override fun onClick(v: View, position: Int) {
                    val myPlaylists = viewModel.myPlaylists.value!!
                    addMusicItem(Musics(0,videoData,myPlaylists[position].uid))
                    Toast.makeText(activity,"재생목록에 추가했습니다.",Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                }

                override fun optionButtonClick(v: View, position: Int) {
                    Log.d("옵션버튼","DialogFragmentPopupAddPlaylist")
                }
            })
            recyclerView.adapter = myPlaylistRecyclerViewAdapter

            builder
                .setTitle("재생목록에 추가")
                .setView(dialogView)
                // Add action buttons

                .setNegativeButton(
                    R.string.dialog_cancel_text,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel =
            ViewModelProvider(this, DialogFragmentPopupAddPlaylistViewModelFactory(MyApplication().myPlaylistRepository))
                .get(DialogFragmentPopupAddPlaylistViewModel::class.java)
        initObserver()
    }
    private fun initObserver(){
        viewModel.myPlaylists.observe(this){ myPlaylists ->
            myPlaylistRecyclerViewAdapter.submitList(myPlaylists.toMutableList())
            if (myPlaylists.isEmpty())
                dialog?.findViewById<TextView>(R.id.empty_text_view)?.visibility = View.VISIBLE
        }
    }

    private fun addMusicItem(music: Musics){
        viewModel.addMusicItem(music)
    }

    private fun getMyPlaylist(){
        viewModel.getAllPlaylist()
    }

}