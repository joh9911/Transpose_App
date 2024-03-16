package com.myFile.transpose.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myFile.transpose.database.MyPlaylist
import com.myFile.transpose.R
import com.myFile.transpose.databinding.MyPlaylistRecyclerAddPlaylistViewBinding
import com.myFile.transpose.databinding.MyPlaylistRecyclerItemBinding
import com.myFile.transpose.databinding.MyPlaylistRecyclerMusicStorageViewBinding
import com.myFile.transpose.databinding.MyPlaylistRecyclerVideoStorageViewBinding


class MyPlaylistRecyclerViewAdapter: ListAdapter<MyPlaylistRecyclerViewAdapter.MyPlaylistItem, RecyclerView.ViewHolder>(
    diffUtil
) {

    companion object{
        private const val ADD_PLAYLIST_VIEW = 0
        private const val MUSIC_STORAGE_VIEW = 1
        private const val VIDEO_STORAGE_VIEW = 2
        private const val PLAYLIST_VIEW_ITEM = 3
    }

    sealed class MyPlaylistItem {
        object AddPlaylist : MyPlaylistItem()
        object MusicStorage : MyPlaylistItem()
        object VideoStorage: MyPlaylistItem()
        data class MyPlaylistData(val myPlaylist: MyPlaylist) : MyPlaylistItem()
    }

    inner class AddPlaylistViewHolder(private val binding: MyPlaylistRecyclerAddPlaylistViewBinding): RecyclerView.ViewHolder(binding.root){
        init{
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION){
                    itemClickListener.addPlaylistClick(it, bindingAdapterPosition)
                }
            }
        }
    }

    inner class MusicStorageViewHolder(private val binding: MyPlaylistRecyclerMusicStorageViewBinding): RecyclerView.ViewHolder(binding.root){
        init {
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION){
                    itemClickListener.musicStorageClick(it, bindingAdapterPosition)
                }
            }
        }
    }
    inner class VideoStorageViewHolder(private val binding: MyPlaylistRecyclerVideoStorageViewBinding): RecyclerView.ViewHolder(binding.root){
        init {
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION){
                    itemClickListener.videoStorageClick(it, bindingAdapterPosition)
                }
            }
        }
    }
    inner class MyPlaylistItemViewHolder(private val binding: MyPlaylistRecyclerItemBinding): RecyclerView.ViewHolder(binding.root){
        init {
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.onClick(it, bindingAdapterPosition)
            }
            itemView.findViewById<ImageButton>(R.id.option_button).setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.optionButtonClick(it, bindingAdapterPosition)
            }
        }
        fun bind(myPlaylist: MyPlaylist){
            binding.playlistTitle.text = myPlaylist.playlistTitle
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            ADD_PLAYLIST_VIEW -> {
                val binding = MyPlaylistRecyclerAddPlaylistViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AddPlaylistViewHolder(binding)
            }
            MUSIC_STORAGE_VIEW -> {
                val binding = MyPlaylistRecyclerMusicStorageViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MusicStorageViewHolder(binding)
            }
            VIDEO_STORAGE_VIEW -> {
                val binding = MyPlaylistRecyclerVideoStorageViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                VideoStorageViewHolder(binding)
            }
            else -> {
                val binding = MyPlaylistRecyclerItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MyPlaylistItemViewHolder(binding)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MyPlaylistItem.AddPlaylist -> ADD_PLAYLIST_VIEW
            is MyPlaylistItem.MusicStorage -> MUSIC_STORAGE_VIEW
            is MyPlaylistItem.VideoStorage -> VIDEO_STORAGE_VIEW
            is MyPlaylistItem.MyPlaylistData -> PLAYLIST_VIEW_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = getItem(position)) {
            is MyPlaylistItem.MyPlaylistData -> (holder as MyPlaylistItemViewHolder).bind(item.myPlaylist)
            is MyPlaylistItem.AddPlaylist -> {}
            is MyPlaylistItem.MusicStorage -> {}
            is MyPlaylistItem.VideoStorage -> {}
            // 나머지 타입들은 별도의 처리가 필요하지 않을 수도 있습니다.
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    // (2) 리스너 인터페이스
    interface OnItemClickListener {

        fun addPlaylistClick(v: View, position: Int)

        fun musicStorageClick(v: View, position: Int)

        fun videoStorageClick(v: View, position: Int)

        fun onClick(v: View, position: Int)
        fun optionButtonClick(v: View, position: Int)
    }
    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // (4) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener : OnItemClickListener

    object diffUtil : DiffUtil.ItemCallback<MyPlaylistItem>() {
        override fun areItemsTheSame(oldItem: MyPlaylistItem, newItem: MyPlaylistItem): Boolean {
            return when {
                oldItem is MyPlaylistItem.AddPlaylist && newItem is MyPlaylistItem.AddPlaylist -> true
                oldItem is MyPlaylistItem.MusicStorage && newItem is MyPlaylistItem.MusicStorage -> true
                oldItem is MyPlaylistItem.VideoStorage && newItem is MyPlaylistItem.VideoStorage -> true
                oldItem is MyPlaylistItem.MyPlaylistData && newItem is MyPlaylistItem.MyPlaylistData -> oldItem.myPlaylist.uid == newItem.myPlaylist.uid
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: MyPlaylistItem, newItem: MyPlaylistItem): Boolean {
            return oldItem == newItem
        }
    }
}