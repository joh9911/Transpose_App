package com.myFile.transpose.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myFile.transpose.R
import com.myFile.transpose.databinding.ChannelVideoRecyclerViewHeaderViewBinding
import com.myFile.transpose.databinding.ProgressBarItemBinding
import com.myFile.transpose.databinding.SearchResultRecyclerItemBinding
import com.myFile.transpose.model.ChannelDataModel
import com.myFile.transpose.model.VideoDataModel

class ChannelVideoRecyclerViewAdapter: ListAdapter<ChannelVideoRecyclerViewAdapter.ChannelFragmentRecyclerViewItem, RecyclerView.ViewHolder>(
    diffUtil
) {
    companion object{
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_LOADING = 2
        private const val VIEW_TYPE_ITEM = 1
    }

    sealed class ChannelFragmentRecyclerViewItem{
        object LoadingData: ChannelFragmentRecyclerViewItem()
        data class HeaderTitleData(val channelDataModel: ChannelDataModel): ChannelFragmentRecyclerViewItem()
        data class ItemData(val videoData: VideoDataModel): ChannelFragmentRecyclerViewItem()
    }


    inner class MyHeaderViewHolder(private val binding: ChannelVideoRecyclerViewHeaderViewBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(channelDataModel: ChannelDataModel){
            binding.channelTitle.text = channelDataModel.channelTitle
            binding.channelInfo.text =
                String.format(binding.channelInfo.context.getString(R.string.channel_video_count), channelDataModel.channelVideoCount.toInt())
            binding.channelDescription.text = channelDataModel.channelDescription
            Glide.with(binding.channelBanner)
                .load(channelDataModel.channelBanner)
                .into(binding.channelBanner)
        }
    }
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
                    itemClickListener.optionButtonClick(it, bindingAdapterPosition)
            }
        }
        fun bind(videoData: VideoDataModel){
            binding.channelTextView.text = videoData.channelTitle
            binding.titleTextView.text = videoData.title
            binding.videoDetailText.text = videoData.date
            Glide.with(binding.thumbnailImageView)
                .load(videoData.thumbnail)
                .into(binding.thumbnailImageView)
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ChannelVideoRecyclerViewHeaderViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MyHeaderViewHolder(binding)
            }
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
        return when (currentList[position]) {
            is ChannelFragmentRecyclerViewItem.HeaderTitleData -> VIEW_TYPE_HEADER
            is ChannelFragmentRecyclerViewItem.ItemData -> VIEW_TYPE_ITEM
            is ChannelFragmentRecyclerViewItem.LoadingData -> VIEW_TYPE_LOADING
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is MyHeaderViewHolder -> {
                val item = currentList[position] as ChannelFragmentRecyclerViewItem.HeaderTitleData
                holder.bind(item.channelDataModel)
            }
            is MyViewHolder -> {
                val item = currentList[position] as ChannelFragmentRecyclerViewItem.ItemData
                holder.bind(item.videoData)
            }
        }

    }
    // (2) 리스너 인터페이스
    interface OnItemClickListener {
        fun videoClick(v: View, position: Int)
        fun optionButtonClick(v: View, position: Int)
    }
    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    // (4) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener : OnItemClickListener

    object diffUtil : DiffUtil.ItemCallback<ChannelFragmentRecyclerViewItem>() {

        override fun areItemsTheSame(oldItem: ChannelFragmentRecyclerViewItem, newItem: ChannelFragmentRecyclerViewItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChannelFragmentRecyclerViewItem, newItem: ChannelFragmentRecyclerViewItem): Boolean {
            return oldItem == newItem
        }
    }

}