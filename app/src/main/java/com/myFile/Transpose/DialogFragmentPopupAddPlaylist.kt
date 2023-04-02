package com.myFile.Transpose

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DialogFragmentPopupAddPlaylist(val videoData: VideoData): DialogFragment() {

    var myPlaylists = listOf<MyPlaylist>()
    lateinit var db: AppDatabase
    lateinit var myPlaylistDao: MyPlaylistDao
    private lateinit var myPlaylistRecyclerViewAdapter: MyPlaylistRecyclerViewAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            db = Room.databaseBuilder(
                requireActivity(),
                AppDatabase::class.java, "database-name"
            )
                .build()
            myPlaylistDao = db.myPlaylistDao()

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
                    val r = Runnable {
                        myPlaylistDao.insertMusic(Musics(0,videoData,myPlaylists[position].uid))
                    }
                    val thread = Thread(r)
                    thread.start()
                    Toast.makeText(activity,"재생목록에 추가했습니다.",Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                }

                override fun optionButtonClick(v: View, position: Int) {
                    Log.d("옵션버튼","DialogFragmentPopupAddPlaylist")
                }
            })
            recyclerView.adapter = myPlaylistRecyclerViewAdapter
            getMyPlaylist()
            builder
                .setTitle("재생목록에 추가")
                .setView(dialogView)
                // Add action buttons

                .setNegativeButton(R.string.dialog_cancel_text,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun getMyPlaylist(){
        CoroutineScope(Dispatchers.IO).launch{
            myPlaylists = myPlaylistDao.getAll()
            myPlaylistRecyclerViewAdapter.submitList(myPlaylists.toMutableList())
            withContext(Dispatchers.Main){
                if (myPlaylists.isEmpty()){
                    dialog?.findViewById<TextView>(R.id.empty_text_view)?.visibility = View.VISIBLE
                }
            }
        }
    }

}