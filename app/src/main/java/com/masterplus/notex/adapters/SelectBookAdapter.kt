package com.masterplus.notex.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.databinding.ItemSelectBookBinding
import com.masterplus.notex.roomdb.views.BookCountView

class SelectBookAdapter:RecyclerView.Adapter<SelectBookAdapter.ViewHolder>() {


    private val diffUtils=object:DiffUtil.ItemCallback<BookCountView>(){
        override fun areItemsTheSame(oldItem: BookCountView, newItem: BookCountView): Boolean
            = oldItem.bookId==newItem.bookId

        override fun areContentsTheSame(oldItem: BookCountView, newItem: BookCountView): Boolean
            = oldItem == newItem
    }

    private val asyncListDiffer = AsyncListDiffer(this,diffUtils)
    var items:List<BookCountView>
        get() = asyncListDiffer.currentList
        set(value) = asyncListDiffer.submitList(value)


    var selectedItem:BookCountView? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field=value
            notifyDataSetChanged()
        }

    fun getItemPos(item:BookCountView):Int{
        return items.indexOf(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val bindingView=ItemSelectBookBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=items[position]

        holder.binding.imageVisibilitySelectBookItem.visibility=if(item.isVisibleItems) View.GONE else View.VISIBLE
        holder.binding.textUpperSelectBookItem.text = item.name
        holder.binding.textDownSelectBookItem.text = item.size.toString()

        holder.binding.root.isSelected = item==selectedItem

    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding:ItemSelectBookBinding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                if(bindingAdapterPosition!=-1){
                    selectedItem=items[bindingAdapterPosition]
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
    }


}