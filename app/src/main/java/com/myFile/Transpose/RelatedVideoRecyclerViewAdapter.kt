package com.myFile.Transpose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myFile.Transpose.databinding.ProgressBarItemBinding
import com.myFile.Transpose.databinding.RelatedVideoRecyclerViewHeaderViewBinding
import com.myFile.Transpose.databinding.RelatedVideoRecyclerViewHeaderViewShimmerBinding
import com.myFile.Transpose.databinding.SearchResultRecyclerItemBinding
import com.myFile.Transpose.model.ChannelSearchData
import com.myFile.Transpose.model.HeaderViewData
import com.myFile.Transpose.model.VideoDetailData

class RelatedVideoRecyclerViewAdapter: ListAdapter<VideoData, RecyclerView.ViewHolder>(diffUtil) {
    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_ITEM= 1


    override fun getItemCount(): Int {
        return currentList.size + 1
    }
    lateinit var headerView: RelatedVideoRecyclerViewHeaderViewBinding

    fun setHeaderViewTitle(videoTitle: String){
        headerView.fragmentVideoTitle.text = videoTitle
    }


    fun setHeaderViewData(headerViewData: HeaderViewData,context: Activity){
        setHeaderViewAfterGettingData(context)
        headerView.fragmentVideoTitle.text = headerViewData.fragmentVideoTitle
        headerView.videoTime.text = headerViewData.videoTime
        headerView.videoViewCount.text = headerViewData.videoViewCount
        headerView.channelTextView.text = headerViewData.channelTitle
        headerView.channelSubscriptionCount.text = headerViewData.channelSubscriptionCount

        Glide.with(headerView.channelImageView)
            .load(headerViewData.channelThumbnail)
            .into(headerView.channelImageView)

    }
    fun setHeaderViewColorBeforeGettingData(context: Activity){
        headerView.videoTime.text = ""
        headerView.videoViewCount.text = ""
        headerView.channelTextView.text = ""
        headerView.channelSubscriptionCount.text = ""
        headerView.videoTime.setBackgroundColor(context.resources.getColor(R.color.before_getting_data_color))
        headerView.videoViewCount.setBackgroundColor(context.resources.getColor(R.color.before_getting_data_color))
        headerView.channelTextView.setBackgroundColor(context.resources.getColor(R.color.before_getting_data_color))
        headerView.channelSubscriptionCount.setBackgroundColor(context.resources.getColor(R.color.before_getting_data_color))

        Glide.with(headerView.channelImageView)
            .load(R.color.before_getting_data_color)
            .into(headerView.channelImageView)
    }

    fun setHeaderViewAfterGettingData(context: Activity){
        headerView.videoTime.setBackgroundColor(context.resources.getColor(R.color.white))
        headerView.videoViewCount.setBackgroundColor(context.resources.getColor(R.color.white))
        headerView.channelTextView.setBackgroundColor(context.resources.getColor(R.color.white))
        headerView.channelSubscriptionCount.setBackgroundColor(context.resources.getColor(R.color.white))
    }


    inner class HeaderViewHolder(binding: RelatedVideoRecyclerViewHeaderViewBinding):
    RecyclerView.ViewHolder(binding.root){
        init {
            headerView = binding
            binding.channelLinearLayout.setOnClickListener {
                itemClickListener.channelClick(it, 0)
            }
        }
    }


    inner class MyViewHolder(private val binding: SearchResultRecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.dataItem.setOnClickListener{
                itemClickListener.videoClick(it, bindingAdapterPosition - 1)
            }
            binding.optionButton.setOnClickListener {
                itemClickListener.optionButtonClick(it,bindingAdapterPosition - 1)
            }
        }

        fun bind(videoData: VideoData) {
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
                val binding = RelatedVideoRecyclerViewHeaderViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_HEADER
            else -> VIEW_TYPE_ITEM
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM){
            val itemHolder = holder as MyViewHolder
            itemHolder.bind(currentList[position - 1])
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
    private lateinit var itemClickListener: OnItemClickListener

    companion object diffUtil : DiffUtil.ItemCallback<VideoData>() {

        override fun areItemsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem == newItem
        }
    }
}


