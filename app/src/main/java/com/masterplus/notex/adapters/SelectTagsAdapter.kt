package com.masterplus.notex.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.R
import com.masterplus.notex.databinding.ItemImageTextBinding
import com.masterplus.notex.roomdb.entities.Tag

class SelectTagsAdapter:RecyclerView.Adapter<SelectTagsAdapter.ViewHolder>() {

    private val diffUtil=object:DiffUtil.ItemCallback<Tag>(){
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean = oldItem.uid==newItem.uid
        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean = oldItem==newItem
    }
    private val asyncListDiffer=AsyncListDiffer(this,diffUtil)
    var tags:List<Tag>
        get() = asyncListDiffer.currentList
        set(value) = asyncListDiffer.submitList(value)

    private var chosenTags = mutableListOf<Tag>()
    private var indeterminateTags = mutableListOf<Tag>()
    private var listener:SelectTagsAdapterListener? = null




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding=ItemImageTextBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=tags[position]
        holder.binding.imageTextText.text=item.name
        holder.binding.imageTextImage.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context,
            when{
                chosenTags.contains(item)-> R.drawable.ic_baseline_check_box_24
                indeterminateTags.contains(item)->R.drawable.ic_baseline_indeterminate_check_box_24
                else ->R.drawable.ic_baseline_check_box_outline_blank_24
            }
        ))
    }

    override fun getItemCount(): Int = tags.size

    fun getChosenTags()=chosenTags

    @SuppressLint("NotifyDataSetChanged")
    fun setChosenTags(chosenTags:List<Tag>){
        this.chosenTags=chosenTags.toMutableList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setIndeterminateTags(indeterminateTags:List<Tag>){
        this.indeterminateTags=indeterminateTags.toMutableList()
        notifyDataSetChanged()
    }
    fun setListener(listener:SelectTagsAdapterListener){
        this.listener=listener
    }

    inner class ViewHolder(val binding:ItemImageTextBinding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                if(bindingAdapterPosition!=-1){
                    val item=tags[bindingAdapterPosition]
                    if(chosenTags.contains(item)){
                        chosenTags.remove(item)
                    }else{
                        chosenTags.add(item)
                        indeterminateTags.remove(item)
                    }
                    listener?.clickedTag(item,chosenTags.contains(item),bindingAdapterPosition)
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
    }

    interface SelectTagsAdapterListener{
        fun clickedTag(tag:Tag,isSelected:Boolean,pos:Int)

    }
}