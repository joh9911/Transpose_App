package com.myFile.transpose.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myFile.transpose.databinding.*
import com.myFile.transpose.model.HeaderViewData
import com.myFile.transpose.adapter.model.PlayerFragmentMainItem
import com.myFile.transpose.retrofit.CommentData
import com.myFile.transpose.retrofit.VideoData

class PlayerFragmentMainRecyclerViewAdapter: ListAdapter<PlayerFragmentMainItem, RecyclerView.ViewHolder>(diffUtil) {

    companion object{
        private const val VIEW_TYPE_TITLE_HEADER = 0
        private const val VIEW_TYPE_REST_HEADER = 1
        private const val VIEW_TYPE_ITEM = 2
        private const val VIEW_TYPE_LOADING_HEADER = 3
    }


    inner class HeaderViewTitleViewHolder(private val binding: PlayerFragmentMainRecyclerViewHeaderTitleViewBinding)
        :RecyclerView.ViewHolder(binding.root){
        fun bind(videoData: VideoData){
            binding.fragmentVideoTitle.text = videoData.title
        }
    }

    inner class HeaderViewRestViewHolder(
        private val binding: PlayerFragmentMainRecyclerViewHeaderRestViewBinding):
    RecyclerView.ViewHolder(binding.root){
        init {
            binding.channelLinearLayout.setOnClickListener {
                itemClickListener.channelClick(it, 0)
            }
            binding.minusButton.setOnClickListener {
                itemClickListener.pitchMinusButtonClick(it)
            }
            binding.initButton.setOnClickListener {
                itemClickListener.pitchInitButtonClick(it)
            }
            binding.plusButton.setOnClickListener {
                itemClickListener.pitchPlusButtonClick(it)
            }
            binding.tempoInitButton.setOnClickListener {
                itemClickListener.tempoInitButtonClick(it)
            }
            binding.tempoMinusButton.setOnClickListener {
                itemClickListener.tempoMinusButtonClick(it)
            }
            binding.tempoPlusButton.setOnClickListener {
                itemClickListener.tempoPlusButtonClick(it)
            }

        }
        fun bind(headerViewData: HeaderViewData){
            binding.channelTextView.text = headerViewData.channelTitle
            binding.channelSubscriptionCount.text = headerViewData.channelSubscriptionCount
            binding.videoTime.text = headerViewData.videoTime
            binding.videoViewCount.text = headerViewData.videoViewCount
            Glide.with(binding.channelImageView)
                .load(headerViewData.channelThumbnail)
                .into(binding.channelImageView)
        }
    }
    inner class LoadingHeaderViewHolder(binding: PlayerFragmentMainRecyclerViewLoadingHeaderViewBinding):
        RecyclerView.ViewHolder(binding.root){

    }

    inner class MyViewHolder(private val binding: CommentThreadRecyclerViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
//        init {
//            binding.dataItem.setOnClickListener{
//                itemClickListener.videoClick(it, bindingAdapterPosition - 1)
//            }
//            binding.optionButton.setOnClickListener {
//                itemClickListener.optionButtonClick(it,bindingAdapterPosition - 1)
//            }
            fun bind(commentData: CommentData) {
                binding.authorName.text = commentData.authorName
                binding.commentTime.text = commentData.commentTime
                binding.commentText.text = commentData.commentText
                Glide.with(binding.authorImage)
                    .load(commentData.authorImage)
                    .circleCrop()
                    .into(binding.authorImage)
            }
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = CommentThreadRecyclerViewItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MyViewHolder(binding)
            }
            VIEW_TYPE_TITLE_HEADER -> {
                val binding = PlayerFragmentMainRecyclerViewHeaderTitleViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewTitleViewHolder(binding)
            }
            VIEW_TYPE_REST_HEADER -> {
                val binding = PlayerFragmentMainRecyclerViewHeaderRestViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewRestViewHolder(binding)
            }

            else -> {
                val binding = PlayerFragmentMainRecyclerViewLoadingHeaderViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LoadingHeaderViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PlayerFragmentMainItem.HeaderTitleData -> VIEW_TYPE_TITLE_HEADER
            is PlayerFragmentMainItem.HeaderRestData -> VIEW_TYPE_REST_HEADER
            is PlayerFragmentMainItem.ContentData -> VIEW_TYPE_ITEM
            is PlayerFragmentMainItem.LoadingHeader -> VIEW_TYPE_LOADING_HEADER
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is MyViewHolder -> {
                val item = getItem(position) as PlayerFragmentMainItem.ContentData
                holder.bind(item.commentData)
            }
            is HeaderViewTitleViewHolder -> {
                val item = getItem(position) as PlayerFragmentMainItem.HeaderTitleData
                holder.bind(item.videoData)
            }
            is HeaderViewRestViewHolder -> {
                val item = getItem(position) as PlayerFragmentMainItem.HeaderRestData
                holder.bind(item.headerViewData)
            }
        }
    }


    // (2) 리스너 인터페이스
    interface OnItemClickListener {
        fun channelClick(v: View, position: Int)
        fun videoClick(v: View, position: Int)
        fun optionButtonClick(v: View, position: Int)
        fun pitchMinusButtonClick(v: View)
        fun pitchInitButtonClick(v: View)
        fun pitchPlusButtonClick(v: View)
        fun tempoMinusButtonClick(v: View)
        fun tempoInitButtonClick(v: View)
        fun tempoPlusButtonClick(v: View)
    }

    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // (4) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener: OnItemClickListener

    object diffUtil : DiffUtil.ItemCallback<PlayerFragmentMainItem>() {

        override fun areItemsTheSame(oldItem: PlayerFragmentMainItem, newItem: PlayerFragmentMainItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: PlayerFragmentMainItem, newItem: PlayerFragmentMainItem): Boolean {
            return oldItem == newItem
        }
    }
}


