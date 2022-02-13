package com.masterplus.notex.adapters

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.R
import com.masterplus.notex.databinding.ItemBookBinding
import com.masterplus.notex.roomdb.views.BookCountView

class BookAdapter:RecyclerView.Adapter<BookAdapter.ViewHolder>() {

    private val diffUtils=object:DiffUtil.ItemCallback<BookCountView>(){
        override fun areItemsTheSame(oldItem: BookCountView, newItem: BookCountView): Boolean = oldItem.bookId==newItem.bookId

        override fun areContentsTheSame(oldItem: BookCountView, newItem: BookCountView): Boolean = oldItem==newItem

    }
    private val asyncListDiffer=AsyncListDiffer(this,diffUtils)
    var items:List<BookCountView>
        get() = asyncListDiffer.currentList
        set(value) = asyncListDiffer.submitList(value)

    private var listener:BookListener? = null
    fun setListener(listener:BookListener?){
        this.listener=listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val bindingView=ItemBookBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(bindingView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=items[position]
        holder.binding.textUpperBookItem.text=item.name
        holder.binding.textDownBookItem.text = item.size.toString()
        holder.binding.imageVisibilityBookItem.visibility = if(item.isVisibleItems) View.GONE else View.VISIBLE
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding:ItemBookBinding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                listener?.clickedItem(items[bindingAdapterPosition])
            }
            binding.imageMenuBookItem.setOnClickListener {
                val popMenu=PopupMenu(binding.root.context,binding.imageMenuBookItem)
                popMenu.menuInflater.inflate(R.menu.book_item_menu,popMenu.menu)
                popMenu.menu?.let { menu->
                    menu.findItem(R.id.setVisible_book_menu_item).isVisible=binding.imageVisibilityBookItem.isVisible
                    menu.findItem(R.id.setInVisible_book_menu_item).isVisible=!binding.imageVisibilityBookItem.isVisible
                }
                popMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    listener?.menuListener(it,items[bindingAdapterPosition],bindingAdapterPosition)
                    true
                })
                popMenu.show()
            }
        }
    }
    interface BookListener{
        fun clickedItem(item: BookCountView)
        fun menuListener(menuItem: MenuItem, item: BookCountView, pos:Int)
    }
}