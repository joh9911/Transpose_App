package com.myFile.transpose.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myFile.transpose.retrofit.VideoData
import com.myFile.transpose.databinding.ProgressBarItemBinding
import com.myFile.transpose.databinding.SearchResultRecyclerItemBinding

class SearchResultFragmentRecyclerViewAdapter: ListAdapter<VideoData, RecyclerView.ViewHolder>(
    diffUtil
) {
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    inner class MyProgressViewHolder(binding: ProgressBarItemBinding): RecyclerView.ViewHolder(binding.root){
    }
    inner class MyViewHolder(private val binding: SearchResultRecyclerItemBinding): RecyclerView.ViewHolder(binding.root) {
        init{

            binding.dataItem.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.videoClick(it, bindingAdapterPosition)
            }
            binding.optionButton.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.optionButtonClick(it,bindingAdapterPosition)
            }

        }
        fun bind(videoData: VideoData){
            binding.channelTextView.text = videoData.channelTitle
            binding.titleTextView.text = videoData.title
            binding.videoDetailText.text = videoData.date
            Glide.with(binding.thumbnailImageView)
                .load(videoData.thumbnail)
                .into(binding.thumbnailImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = SearchResultRecyclerItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MyViewHolder(binding)
            }
            else -> {
                val binding = ProgressBarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MyProgressViewHolder(binding)
            }
        }
    }
    override fun getItemViewType(position: Int): Int {
        return when (currentList[position].title) {
            " " -> VIEW_TYPE_LOADING
            else -> VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is MyViewHolder){
            holder.bind(currentList[position])
        }


    }
    // (2) 리스너 인터페이스
    interface OnItemClickListener {
        fun channelClick(v: View, position: Int)
        fun videoClick(v: View, position: Int)
        fun optionButtonClick(v: View, position: Int)
    }
    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    // (4) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener : OnItemClickListener

    companion object diffUtil : DiffUtil.ItemCallback<VideoData>() {

        override fun areItemsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem == newItem
        }
    }

}