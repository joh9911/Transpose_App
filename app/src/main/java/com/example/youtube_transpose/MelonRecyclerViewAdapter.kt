package com.example.youtube_transpose

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.youtube_transpose.databinding.HomeRecyclerItemBinding

class MelonRecyclerViewAdapter: RecyclerView.Adapter<MelonRecyclerViewAdapter.MyViewHolder>() {

    var dataList = mutableListOf<MelonData>()

    inner class MyViewHolder(private val binding: HomeRecyclerItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(melonData: MelonData){
            binding.rankingTextView.text = melonData.ranking
            binding.accountTextView.text = melonData.account
            binding.titleTextView.text = melonData.title
            binding.extraInfoTextView.text = melonData.extraInfo
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = HomeRecyclerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

}