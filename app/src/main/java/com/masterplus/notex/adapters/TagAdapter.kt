package com.masterplus.notex.adapters

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.R
import com.masterplus.notex.databinding.ItemTagBinding

import com.masterplus.notex.roomdb.views.TagCountView

class TagAdapter: RecyclerView.Adapter<TagAdapter.ViewHolder>() {
    private val diffUtils=object: DiffUtil.ItemCallback<TagCountView>(){
        override fun areItemsTheSame(oldItem: TagCountView, newItem: TagCountView): Boolean = oldItem.tagId==newItem.tagId

        override fun areContentsTheSame(oldItem: TagCountView, newItem: TagCountView): Boolean = oldItem==newItem

    }
    private val asyncListDiffer= AsyncListDiffer(this,diffUtils)
    var items:List<TagCountView>
        get() = asyncListDiffer.currentList
        set(value) = asyncListDiffer.submitList(value)


    private var listener: TagListener? = null
    fun setListener(listener: TagListener?){
        this.listener=listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTagBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=items[position]
        holder.binding.textUpperTagItem.text=item.name
        holder.binding.textDownTagItem.text = item.size.toString()
    }

    override fun getItemCount(): Int = items.size
    inner class ViewHolder(val binding:ItemTagBinding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                listener?.clickedItem(items[bindingAdapterPosition])
            }
            binding.imageMenuTagItem.setOnClickListener {
                val popMenu= PopupMenu(binding.root.context,binding.imageMenuTagItem)
                popMenu.menuInflater.inflate(R.menu.tag_item_menu,popMenu.menu)
                popMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    listener?.menuListener(it,items[bindingAdapterPosition],bindingAdapterPosition)
                    true
                })
                popMenu.show()
            }
        }
    }

    interface TagListener{
        fun clickedItem(item: TagCountView)
        fun menuListener(menuItem: MenuItem, item: TagCountView, pos:Int)
    }
}