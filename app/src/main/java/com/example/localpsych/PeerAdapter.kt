package com.example.localpsych

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.localpsych.databinding.PeerItemBinding

class PeerAdapter: RecyclerView.Adapter<PeerAdapter.ViewHolder>() {
    private var clickListener:((String,Int)->Unit)? = null
    private val callback = object:DiffUtil.ItemCallback<String>(){
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
    private val differ = AsyncListDiffer(this,callback)
    inner class ViewHolder(private val binding: PeerItemBinding):RecyclerView.ViewHolder(binding.root){
        fun setData(item:String,position:Int){
            binding.apply {
                item.apply {
                    peerItemTvName.text = item
                    root.setOnClickListener {
                        clickListener?.let{
                            it(item,position)
                        }
                    }
                }
            }
        }
    }

    fun setList(list:List<String>){
        differ.submitList(list)
    }

    fun setClickListener(listener:(String,Int)->Unit){
        clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(PeerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(differ.currentList.get(position),position)
    }
}