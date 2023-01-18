package com.example.youtube_transpose

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.youtube_transpose.databinding.HomePlaylistItemsRecyclerViewItemBinding
import com.example.youtube_transpose.databinding.HomePopular100RecyclerViewItemBinding
import io.opencensus.resource.Resource

class PlaylistItemsRecyclerViewAdapter(dataList: MutableList<VideoData>, position: Int): RecyclerView.Adapter<PlaylistItemsRecyclerViewAdapter.MyViewHolder>() {
    private val dataList = dataList
    val firstSelectedPosition = position
    var selectedPosition = position
    var lastPlayedPosition = -1

    inner class MyViewHolder(private val binding: HomePlaylistItemsRecyclerViewItemBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                itemClickListener.onClick(it, adapterPosition)
                selectedPosition = adapterPosition
                lastPlayedPosition = if (lastPlayedPosition == -1){
                    notifyItemChanged(firstSelectedPosition)
                    selectedPosition
                }
                else{
                    notifyItemChanged(lastPlayedPosition)
                    selectedPosition
                }
                notifyItemChanged(selectedPosition)
            }
        }
        fun selected(){
            binding.dataItem.setBackgroundColor(Color.parseColor("#484848"))
            binding.optionButton.setBackgroundColor(Color.parseColor("#484848"))
        }
        fun unSelected(){
            binding.dataItem.setBackgroundColor(Color.parseColor("#010101"))
            binding.optionButton.setBackgroundColor(Color.parseColor("#010101"))
        }

        fun bind(videoData: VideoData, position: Int){
            binding.channelTextView.text = videoData.channel
            binding.titleTextView.text = videoData.title
            binding.rankingTextView.text = (position + 1).toString()
            Glide.with(binding.thumbnailImageView)
                .load(videoData.thumbnail)
                .into(binding.thumbnailImageView)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = HomePlaylistItemsRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position == selectedPosition){
            Log.d("onBind","${selectedPosition}, ${lastPlayedPosition}")
            holder.selected()
        }
        else{
            holder.unSelected()
        }
        holder.bind(dataList[position], position)

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
    private lateinit var itemClickListener : OnItemClickListener


}