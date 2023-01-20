package com.example.youtube_transpose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.youtube_transpose.databinding.SearchResultRecyclerItemBinding

class SearchResultFragmentRecyclerViewAdapter(dataList: MutableList<VideoData>): RecyclerView.Adapter<SearchResultFragmentRecyclerViewAdapter.MyViewHolder>() {
    private val dataList = dataList

    inner class MyViewHolder(private val binding: SearchResultRecyclerItemBinding): RecyclerView.ViewHolder(binding.root) {
        init{
            binding.channelImageView.setOnClickListener {
                itemClickListener.channelClick(it, adapterPosition)
            }
            binding.thumbnailImageView.setOnClickListener {
                itemClickListener.videoClick(it, adapterPosition)
            }
            binding.videoTitleChannelTitleLinearLayout.setOnClickListener {
                itemClickListener.videoClick(it, adapterPosition)
            }
        }
        fun bind(videoData: VideoData){
            binding.channelTextView.text = videoData.channel
            binding.titleTextView.text = videoData.title
            Glide.with(binding.thumbnailImageView)
                .load(videoData.thumbnail)
                .into(binding.thumbnailImageView)
            Glide.with(binding.channelImageView)
                .load(videoData.channelThumbnail)
                .into(binding.channelImageView)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SearchResultRecyclerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(dataList[position])
//        holder.itemView.setOnClickListener {
//            itemClickListener.channelClick(it, position)
//        }
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

}