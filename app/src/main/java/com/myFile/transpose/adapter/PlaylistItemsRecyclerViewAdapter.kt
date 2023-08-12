package com.myFile.transpose.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myFile.transpose.databinding.HomePlaylistItemsRecyclerViewItemBinding
import com.myFile.transpose.databinding.PlaylistRecyclerViewHeaderViewBinding
import com.myFile.transpose.databinding.ProgressBarItemBinding
import com.myFile.transpose.model.ChannelDataModel
import com.myFile.transpose.model.PlaylistDataModel
import com.myFile.transpose.model.VideoDataModel

class PlaylistItemsRecyclerViewAdapter: ListAdapter<PlaylistItemsRecyclerViewAdapter.PlaylistItemsRecyclerViewItems, RecyclerView.ViewHolder>(
    diffUtil
) {

    sealed class PlaylistItemsRecyclerViewItems{
        object LoadingData: PlaylistItemsRecyclerViewItems()
        data class HeaderTitleData(val playlistDataModel: PlaylistDataModel): PlaylistItemsRecyclerViewItems()
        data class ItemData(val videoData: VideoDataModel): PlaylistItemsRecyclerViewItems()
    }

    companion object{
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_LOADING = 2
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class MyProgressViewHolder(binding: ProgressBarItemBinding): RecyclerView.ViewHolder(binding.root){
    }

    inner class MyHeaderViewHolder(private val binding: PlaylistRecyclerViewHeaderViewBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(playlistDataModel: PlaylistDataModel){
            binding.playlistTitle.text = playlistDataModel.title
            binding.playlistDescription.text = playlistDataModel.description
            Glide.with(binding.playlistThumbnail)
                .load(playlistDataModel.thumbnail)
                .into(binding.playlistThumbnail)
        }
    }

    inner class MyViewHolder(private val binding: HomePlaylistItemsRecyclerViewItemBinding): RecyclerView.ViewHolder(binding.root) {
        init{
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.onClick(it, bindingAdapterPosition)
            }
            binding.optionButton.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.optionButtonClick(it, bindingAdapterPosition)
            }

        }

        fun bind(videoData: VideoDataModel){
            binding.channelTextView.text = videoData.channelTitle
            binding.titleTextView.text = videoData.title
            Glide.with(binding.thumbnailImageView)
                .load(videoData.thumbnail)
                .into(binding.thumbnailImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            VIEW_TYPE_HEADER -> {
                val binding = PlaylistRecyclerViewHeaderViewBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
                MyHeaderViewHolder(binding)
            }
            VIEW_TYPE_ITEM -> {
                val binding = HomePlaylistItemsRecyclerViewItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
                MyViewHolder(binding)
            }
            else -> {
                val binding = ProgressBarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MyProgressViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is MyHeaderViewHolder ->{
                val item = currentList[position] as PlaylistItemsRecyclerViewItems.HeaderTitleData
                holder.bind(item.playlistDataModel)
            }
            is MyViewHolder -> {
                val item = currentList[position] as PlaylistItemsRecyclerViewItems.ItemData
                holder.bind(item.videoData)
            }
        }
    }
    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is PlaylistItemsRecyclerViewItems.HeaderTitleData -> VIEW_TYPE_HEADER
            is PlaylistItemsRecyclerViewItems.ItemData -> VIEW_TYPE_ITEM
            is PlaylistItemsRecyclerViewItems.LoadingData -> VIEW_TYPE_LOADING
        }
    }


    // (2) 리스너 인터페이스
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
        fun optionButtonClick(v: View, position: Int)
    }
    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    // (4) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener : OnItemClickListener


    object diffUtil : DiffUtil.ItemCallback<PlaylistItemsRecyclerViewItems>() {

        override fun areItemsTheSame(oldItem: PlaylistItemsRecyclerViewItems, newItem: PlaylistItemsRecyclerViewItems): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: PlaylistItemsRecyclerViewItems, newItem: PlaylistItemsRecyclerViewItems): Boolean {
            return oldItem == newItem
        }
    }

}