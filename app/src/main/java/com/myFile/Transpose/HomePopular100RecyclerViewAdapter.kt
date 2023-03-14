package com.myFile.Transpose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myFile.Transpose.databinding.HomePopular100RecyclerViewItemBinding

class HomePopular100RecyclerViewAdapter: ListAdapter<VideoData,HomePopular100RecyclerViewAdapter.MyViewHolder>(diffUtil) {

    inner class MyViewHolder(private val binding: HomePopular100RecyclerViewItemBinding): RecyclerView.ViewHolder(binding.root) {
        init{
            itemView.setOnClickListener {
                itemClickListener.onClick(it, position)
            }
            itemView.findViewById<ImageButton>(R.id.option_button).setOnClickListener{
                itemClickListener.optionButtonClick(it, position)
            }
        }
        fun bind(videoData: VideoData, position: Int){
            binding.channelTextView.text = videoData.channel
            binding.titleTextView.text = videoData.title
            binding.rankingTextView.text = (position + 1).toString()
            Glide.with(binding.thumbnailImageView)
                .load(videoData.thumbnail)
                .centerCrop()
                .into(binding.thumbnailImageView)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = HomePopular100RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(currentList[position], position)

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

    companion object diffUtil : DiffUtil.ItemCallback<VideoData>() {

        override fun areItemsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem == newItem
        }
    }
}