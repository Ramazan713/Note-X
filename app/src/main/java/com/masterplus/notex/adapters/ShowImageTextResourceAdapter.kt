package com.masterplus.notex.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.databinding.ItemImageTextBinding
import com.masterplus.notex.models.ImageTextResourceObject

class ShowImageTextResourceAdapter: RecyclerView.Adapter<ShowImageTextResourceAdapter.ViewHolder>() {

    private val diffUtils=object : DiffUtil.ItemCallback<ImageTextResourceObject>(){
        override fun areItemsTheSame(oldItem: ImageTextResourceObject, newItem: ImageTextResourceObject): Boolean {
            return oldItem==newItem
        }
        override fun areContentsTheSame(oldItem: ImageTextResourceObject, newItem: ImageTextResourceObject): Boolean {
            return oldItem==newItem
        }

    }
    private val recyclerListDiffer= AsyncListDiffer(this,diffUtils)

    var items:List<ImageTextResourceObject>
        get() = recyclerListDiffer.currentList
        set(value) = recyclerListDiffer.submitList(value)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(ItemImageTextBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=items[position]

        holder.binding.root.isClickable=false
        holder.binding.imageTextText.text=item.text
        holder.binding.imageTextImage.apply {
            setBackgroundResource(item.backgroundDrawableId)
            setImageDrawable(ContextCompat.getDrawable(holder.itemView.context,item.srcId))
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding:ItemImageTextBinding):RecyclerView.ViewHolder(binding.root){

    }
}