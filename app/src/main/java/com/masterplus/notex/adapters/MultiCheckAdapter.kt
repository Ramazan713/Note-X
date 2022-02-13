package com.masterplus.notex.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.databinding.ItemMultiCheckBinding
import com.masterplus.notex.models.CheckTextObject

class MultiCheckAdapter:RecyclerView.Adapter<MultiCheckAdapter.ViewHolder>() {

    private val diffUtils=object : DiffUtil.ItemCallback<CheckTextObject>(){
        override fun areItemsTheSame(oldItem: CheckTextObject, newItem: CheckTextObject): Boolean = oldItem.id==newItem.id

        override fun areContentsTheSame(
            oldItem: CheckTextObject,
            newItem: CheckTextObject
        ): Boolean = oldItem==newItem
    }
    private val asyncListDiffer=AsyncListDiffer(this,diffUtils)

    var items:List<CheckTextObject>
        get() = asyncListDiffer.currentList
        set(value) = asyncListDiffer.submitList(value)

    private var listener:MultiCheckAdapterListener? = null

    fun setListener(listener:MultiCheckAdapterListener){
        this.listener=listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MultiCheckAdapter.ViewHolder {
        return ViewHolder(ItemMultiCheckBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: MultiCheckAdapter.ViewHolder, position: Int) {
        val item=items[position]
        holder.binding.checkBoxMulti.text=item.text
        holder.binding.checkBoxMulti.isChecked=item.isCheck
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding:ItemMultiCheckBinding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.checkBoxMulti.setOnClickListener {
                val item=items[bindingAdapterPosition]
                item.isCheck=!item.isCheck
                listener?.clickItem(item)
                notifyItemChanged(bindingAdapterPosition)
            }
        }
    }

    interface MultiCheckAdapterListener{
        fun clickItem(checkTextObject: CheckTextObject)
    }

}