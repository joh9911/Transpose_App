package com.myFile.transpose.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myFile.transpose.database.MyPlaylist
import com.myFile.transpose.R
import com.myFile.transpose.databinding.MyPlaylistRecyclerAddPlaylistViewBinding
import com.myFile.transpose.databinding.MyPlaylistRecyclerItemBinding


class MyPlaylistRecyclerViewAdapterForAddDialog: ListAdapter<MyPlaylist, MyPlaylistRecyclerViewAdapterForAddDialog.MyPlaylistItemViewHolder>(
    diffUtil
) {


    inner class MyPlaylistItemViewHolder(private val binding: MyPlaylistRecyclerItemBinding): RecyclerView.ViewHolder(binding.root){
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
        fun bind(myPlaylist: MyPlaylist){
            binding.playlistTitle.text = myPlaylist.playlistTitle
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPlaylistItemViewHolder {
        val binding = MyPlaylistRecyclerItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyPlaylistItemViewHolder(binding)
    }



    override fun onBindViewHolder(holder: MyPlaylistItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int {
        return currentList.size
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

    object diffUtil : DiffUtil.ItemCallback<MyPlaylist>() {

        override fun areItemsTheSame(oldItem: MyPlaylist, newItem: MyPlaylist): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: MyPlaylist, newItem: MyPlaylist): Boolean {
            return oldItem == newItem
        }
    }
}