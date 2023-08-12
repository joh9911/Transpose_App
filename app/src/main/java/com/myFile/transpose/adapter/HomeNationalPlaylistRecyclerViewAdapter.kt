package com.myFile.transpose.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myFile.transpose.databinding.HomePlaylistRecyclerItemBinding
import com.myFile.transpose.databinding.NationalRecyclerViewItemBinding
import com.myFile.transpose.model.PlaylistDataModel

class HomeNationalPlaylistRecyclerViewAdapter: ListAdapter<PlaylistDataModel, HomeNationalPlaylistRecyclerViewAdapter.MyViewHolder>(
    diffUtil
) {

    inner class MyViewHolder(private val binding: NationalRecyclerViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.onClick(it, bindingAdapterPosition)
            }
        }
        fun bind(playlistDataModel: PlaylistDataModel) {
            binding.nationName.text = playlistDataModel.title
            binding.description.text = playlistDataModel.description
            binding.playlistChannelTitle.text = playlistDataModel.channelTitle
            binding.playlistDate.text = playlistDataModel.date
            Glide.with(binding.nationIcon)
                .load(playlistDataModel.thumbnail)
                .into(binding.nationIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = NationalRecyclerViewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(currentList[position])

    }

    // (2) 리스너 인터페이스
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // (4) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener: OnItemClickListener

    companion object diffUtil : DiffUtil.ItemCallback<PlaylistDataModel>() {

        override fun areItemsTheSame(oldItem: PlaylistDataModel, newItem: PlaylistDataModel): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: PlaylistDataModel, newItem: PlaylistDataModel): Boolean {
            return oldItem == newItem
        }
    }
}