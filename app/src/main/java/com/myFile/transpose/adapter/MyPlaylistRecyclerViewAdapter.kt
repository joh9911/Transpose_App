package com.myFile.transpose.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myFile.transpose.database.MyPlaylist
import com.myFile.transpose.R
import com.myFile.transpose.databinding.MyPlaylistRecyclerItemBinding

class MyPlaylistRecyclerViewAdapter: ListAdapter<MyPlaylist, MyPlaylistRecyclerViewAdapter.MyViewHolder>(
    diffUtil
) {
    inner class MyViewHolder(private val binding: MyPlaylistRecyclerItemBinding): RecyclerView.ViewHolder(binding.root){
        init {
            itemView.setOnClickListener {
                itemClickListener.onClick(it, bindingAdapterPosition)
            }
            itemView.findViewById<ImageButton>(R.id.option_button).setOnClickListener {
                itemClickListener.optionButtonClick(it, bindingAdapterPosition)
            }
        }
        fun bind(myPlaylist: MyPlaylist, position: Int){
            binding.playlistTitle.text = myPlaylist.playlistTitle
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = MyPlaylistRecyclerItemBinding.inflate(
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

    companion object diffUtil : DiffUtil.ItemCallback<MyPlaylist>() {

        override fun areItemsTheSame(oldItem: MyPlaylist, newItem: MyPlaylist): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: MyPlaylist, newItem: MyPlaylist): Boolean {
            return oldItem == newItem
        }
    }
}