package com.myFile.transpose.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.myFile.transpose.R
import com.myFile.transpose.databinding.HomePopular100RecyclerViewItemBinding
import com.myFile.transpose.model.VideoDataModel

class HomePopularSongsRecyclerViewAdapter: ListAdapter<VideoDataModel, HomePopularSongsRecyclerViewAdapter.MyViewHolder>(
    diffUtil
) {

    inner class MyViewHolder(private val binding: HomePopular100RecyclerViewItemBinding): RecyclerView.ViewHolder(binding.root) {
        init{
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.onClick(it, bindingAdapterPosition)
            }
            itemView.findViewById<ImageButton>(R.id.option_button).setOnClickListener{
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.optionButtonClick(it, bindingAdapterPosition)
            }
        }
        fun bind(videoData: VideoDataModel, position: Int){
            binding.channelTextView.text = videoData.channelTitle
            binding.titleTextView.text = videoData.title
            val requestOptions = RequestOptions().transform(RoundedCorners(8))
            Glide.with(binding.thumbnailImageView)
                .load(videoData.thumbnail)
                .apply(requestOptions)
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

    companion object diffUtil : DiffUtil.ItemCallback<VideoDataModel>() {

        override fun areItemsTheSame(oldItem: VideoDataModel, newItem: VideoDataModel): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: VideoDataModel, newItem: VideoDataModel): Boolean {
            return oldItem == newItem
        }
    }
}