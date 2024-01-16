package com.myFile.transpose.view.adapter

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.myFile.transpose.R
import com.myFile.transpose.databinding.VideoItemBinding
import com.myFile.transpose.data.model.VideoDataModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class MyAudioFileItemsRecyclerViewAdapter: ListAdapter<VideoDataModel, MyAudioFileItemsRecyclerViewAdapter.MyViewHolder>(
    diffUtil
) {

    inner class MyViewHolder(private val binding: VideoItemBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.onClick(it, bindingAdapterPosition)
            }
            itemView.findViewById<ImageButton>(R.id.option_button).setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.optionButtonClick(it, bindingAdapterPosition)
            }
        }

        private fun getAlbumArtUri(albumId: Long): Uri {
            return ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId
            )
        }

        private fun loadAlbumArt(context: Context, albumId: Long) {
            val albumArtUri = getAlbumArtUri(albumId)
            Glide.with(context)
                .load(albumArtUri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.color.placeholder_blur_blue) // 앨범 아트가 없을 경우 표시될 플레이스홀더
                .error(R.color.placeholder_blur_blue) // 로딩 실패 시 표시될 이미지
                .into(binding.thumbnailImageView)
        }


        fun bind(videoData: VideoDataModel, position: Int){
            binding.channelTextView.text = videoData.channelTitle
            binding.titleTextView.text = videoData.title
            binding.videoDetailText.text = videoData.date
            try{
                loadAlbumArt(itemView.context, videoData.thumbnail.toLong())
            }catch (e: Exception){
                Log.d("로그 확인","bind ${e.message}")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = VideoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
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