package com.masterplus.notex.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.databinding.ItemImageTextBinding
import com.masterplus.notex.models.ImageTextObject

class SelectImageTextAdapter: RecyclerView.Adapter<SelectImageTextAdapter.ViewHolder>() {

    private val diffUtils=object :DiffUtil.ItemCallback<ImageTextObject>(){
        override fun areItemsTheSame(oldItem: ImageTextObject, newItem: ImageTextObject): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(
            oldItem: ImageTextObject,
            newItem: ImageTextObject
        ): Boolean {
            return oldItem==newItem
        }

    }
    private val recyclerListDiffer=AsyncListDiffer(this,diffUtils)

    var items:List<ImageTextObject>
        get() = recyclerListDiffer.currentList
        set(value) = recyclerListDiffer.submitList(value)

    private var isSelectedActive:Boolean=false
    private var selectedId:Int=-1
    private var listener:SelectImageTextListener?=null



    public fun setSelection(isSelectedActive:Boolean,selectedId:Int){
        this.isSelectedActive=isSelectedActive
        this.selectedId=selectedId
    }

    public fun setListener(listener: SelectImageTextListener?){
        this.listener=listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view=ItemImageTextBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=items[position]

        holder.binding.imageTextImage
            .setImageDrawable(ContextCompat.getDrawable(holder.itemView.context,item.drawableId))
        holder.binding.imageTextText.text=item.text

        if(isSelectedActive){
            holder.itemView.isSelected = item.id==selectedId
        }

    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    inner class ViewHolder(val binding: ItemImageTextBinding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val item=items[bindingAdapterPosition]
                listener?.getSelectedItem(item)
                selectedId=item.id
                notifyDataSetChanged()
            }
        }
    }

    interface SelectImageTextListener{
        fun getSelectedItem(item:ImageTextObject)
    }
}