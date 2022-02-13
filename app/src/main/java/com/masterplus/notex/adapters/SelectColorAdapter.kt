package com.masterplus.notex.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.databinding.ItemSelectColor2Binding
import com.masterplus.notex.models.ColorItem

class SelectColorAdapter:RecyclerView.Adapter<SelectColorAdapter.ViewHolder>() {

    private val diffUtils=object :DiffUtil.ItemCallback<ColorItem>(){
        override fun areItemsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean = oldItem==newItem

        override fun areContentsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean = oldItem == newItem

    }
    private val asyncListDiffer=AsyncListDiffer(this,diffUtils)
    var items:List<ColorItem>
        get() = asyncListDiffer.currentList
        set(value) = asyncListDiffer.submitList(value)
    private var selectedColor:ColorItem?=null
    private var listener:SelectColorListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setDefaultColorAndListener(listener:SelectColorListener?, selectedColor:ColorItem?){
        this.listener=listener
        this.selectedColor=selectedColor
        notifyDataSetChanged()

    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSelectColor2Binding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=items[position]

        holder.binding.root.apply {
            setCardBackgroundColor(item.colorUIMode)
            isSelected = selectedColor==item
            setStateColorReset(position==0)
        }

    }

    override fun getItemCount(): Int = items.size


    @SuppressLint("NotifyDataSetChanged")
    inner class ViewHolder(val binding:ItemSelectColor2Binding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val item=items[bindingAdapterPosition]
                selectedColor=item
                listener?.getSelectedColor(item)
                notifyDataSetChanged()
            }
        }
    }

    interface SelectColorListener{
        fun getSelectedColor(color:ColorItem)
    }

}