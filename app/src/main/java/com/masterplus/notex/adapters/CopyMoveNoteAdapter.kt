package com.masterplus.notex.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.R
import com.masterplus.notex.databinding.ItemCopyMoveBinding
import com.masterplus.notex.models.copymove.CopyMoveItem

class CopyMoveNoteAdapter:RecyclerView.Adapter<CopyMoveNoteAdapter.ViewHolder>() {

    private val diffUtil = object:DiffUtil.ItemCallback<CopyMoveItem>(){
        override fun areItemsTheSame(oldItem: CopyMoveItem, newItem: CopyMoveItem): Boolean = oldItem.uid==newItem.uid
        override fun areContentsTheSame(oldItem: CopyMoveItem, newItem: CopyMoveItem): Boolean = newItem==oldItem
    }
    private val asyncListDiffer = AsyncListDiffer(this,diffUtil)
    var items:List<CopyMoveItem>
        get() = asyncListDiffer.currentList
        set(value) = asyncListDiffer.submitList(value)

    var selectedItem:CopyMoveItem? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {field=value;notifyDataSetChanged()}
    private var listener:CopyMoveNoteListener? = null
    private var imageFirstId:Int = R.drawable.ic_baseline_library_books_24
    private var imageLastId:Int? = null

    fun setInit(listener: CopyMoveNoteListener,imageFirstId:Int,imageLastId:Int?){
        this.listener=listener
        this.imageFirstId=imageFirstId
        this.imageLastId=imageLastId
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CopyMoveNoteAdapter.ViewHolder {
        return ViewHolder(ItemCopyMoveBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: CopyMoveNoteAdapter.ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.titleCopMov.text=item.name
        holder.binding.imageFirstCopMov.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context,imageFirstId))
        holder.binding.countCopMov.text = item.count.toString()


        holder.binding.imageLastCopMov.let { imageLast->
            if(item.isLastImage&&imageLastId!=null){
                imageLast.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context,imageLastId!!))
                imageLast.visibility= View.VISIBLE
            }else{
                imageLast.visibility=View.INVISIBLE
            }
        }

        holder.binding.root.isSelected = selectedItem==item

    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    inner class ViewHolder(val binding:ItemCopyMoveBinding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val position=bindingAdapterPosition
                if(position!=-1){
                    val item = items[position]
                    listener?.selectedItem(item)
                    selectedItem=item
                    notifyDataSetChanged()
                }
            }
        }
    }

    interface CopyMoveNoteListener{
        fun selectedItem(item:CopyMoveItem)
    }
}