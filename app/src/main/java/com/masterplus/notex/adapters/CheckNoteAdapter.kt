package com.masterplus.notex.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.R
import com.masterplus.notex.databinding.ItemCheckNoteBinding
import com.masterplus.notex.roomdb.entities.ContentNote
import com.masterplus.notex.utils.getHighLightedText
import java.util.*

class CheckNoteAdapter: RecyclerView.Adapter<CheckNoteAdapter.ViewHolder>() {


    var items:MutableList<ContentNote> = mutableListOf()

    private var searchedText:String=""
    private var isEdit:Boolean=false
    private var isEnabled:Boolean=true
    private var noteColorInt:Int=Color.parseColor("#FFFFFF")
    private var listener:CheckNoteAdapterListener? = null
    private var selectedItems = mutableListOf<ContentNote>()
    private var textSize:Float=18f


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCheckNoteBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.imageIsCheckFromCNoteIt.isVisible = !isEdit && item.isCheck
        holder.binding.imageMenuFromCNoteIt.isVisible = !isEdit && !item.isCheck

        holder.binding.imageDragFromCNoteIt.isVisible=isEdit

        holder.binding.root.setCardBackgroundColor(noteColorInt)

        holder.binding.textFromCNoteIt.paintFlags = if(item.isCheck) Paint.STRIKE_THRU_TEXT_FLAG else 0
        holder.binding.textFromCNoteIt.textSize=textSize

        holder.binding.textFromCNoteIt.text=if(isEdit&&item.textSize>300) getItemAdjustedText(item.text.subSequence(0,300).toString())
            else getItemAdjustedText(item.text)

        holder.binding.imageMoreFromCNoteIt.isVisible = isEdit && item.textSize>300

        val isSelected = selectedItems.contains(item)
        holder.binding.root.isSelected=isSelected

    }

    override fun getItemCount(): Int = items.size


    private fun getItemAdjustedText(text:String): SpannableStringBuilder {
        return if(searchedText!="") getHighLightedText(text,searchedText) else SpannableStringBuilder(text)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setInit(noteColor:Int, isEdit:Boolean=false,textSize:Float, listener: CheckNoteAdapterListener?){
        this.noteColorInt=noteColor
        this.isEdit=isEdit
        this.listener=listener
        this.textSize=textSize
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTextSize(textSize:Float){
        this.textSize=textSize
        notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setColor(colorInt: Int){
        this.noteColorInt=colorInt
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setEdit(isEdit: Boolean){
        this.isEdit=isEdit
        if(!isEdit){
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setCheckSelectedItems(isCheck:Boolean){
        selectedItems.forEach {
            items[items.indexOf(it)].isCheck=isCheck
            it.isCheck=isCheck
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setEnabled(isEnabled:Boolean){
        this.isEnabled=isEnabled
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun selectAllItems(){
        selectedItems.clear()
        selectedItems.addAll(items)
        listener?.selectedItemCount(selectedItems.size)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSearchedText(searchedText:String){
        this.searchedText=searchedText
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItems(items:List<ContentNote>){
        selectedItems.clear()
        items.forEach { item->
            selectedItems.addAll(this.items.filter { it.uid==item.uid })
        }
        listener?.selectedItemCount(selectedItems.size)
        notifyDataSetChanged()
    }

    fun getSelectedItems() = selectedItems

    fun removeItem(position: Int){
        items.removeAt(position)
        notifyItemRemoved(position)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun removeItems(items:List<ContentNote>){
        this.items.removeAll(items)
        notifyDataSetChanged()
    }
    fun addItem(item:ContentNote, itemPos:Int){
        items.add(itemPos,item)
        notifyItemInserted(itemPos)
    }

    fun updateItem(newValue:String, pos: Int){
        items[pos].text=newValue
        notifyItemChanged(pos)
    }
    fun swapItems(fromPosition:Int,toPosition:Int){
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }
    fun swapItem(fromPosition: Int){
        swapItems(fromPosition,items.size-1)
    }
    fun swapItem(fromPosition:Int,toPosition:Int){
        Collections.swap(items,fromPosition,toPosition)
        notifyItemMoved(fromPosition,toPosition)
    }


    @SuppressLint("ClickableViewAccessibility")
    inner class ViewHolder(val binding:ItemCheckNoteBinding):RecyclerView.ViewHolder(binding.root),
        View.OnClickListener,View.OnLongClickListener{
        init {
            binding.root.setOnClickListener(this)
            binding.imageMenuFromCNoteIt.setOnClickListener(this)
            binding.textLinearFromCNoteIt.setOnClickListener(this)

            binding.root.setOnLongClickListener(this)

            binding.imageDragFromCNoteIt.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    listener?.onStartDrag(this)
                }
                false
            }
        }

        override fun onClick(v: View?) {
            val pos=bindingAdapterPosition
            if(isEnabled&&pos!=-1){
                val item = items[pos]
                when(v?.id){
                    binding.root.id->{
                        if(isEdit)
                            isItemContains(item, pos)
                    }
                    binding.imageMenuFromCNoteIt.id->{
                        val popMenu= PopupMenu(v.context,binding.imageMenuFromCNoteIt, Gravity.CENTER)
                        popMenu.inflate(R.menu.check_note_item_menu)
                        popMenu.setOnMenuItemClickListener {
                            listener?.selectedMenuItem(it,item,pos)
                            true
                        }
                        popMenu.show()
                    }
                    binding.textLinearFromCNoteIt.id->{
                        if(isEdit){
                            listener?.editRequestItem(item,pos)
                        }
                        else{
                            item.isCheck=!item.isCheck
                            notifyItemChanged(pos)
                        }
                    }

                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val pos=bindingAdapterPosition
            if(isEnabled&&pos!=-1){
                if(!isEdit)
                    setEdit(true)
                val item = items[pos]
                isItemContains(item, pos)
            }
            return true
        }
        @SuppressLint("NotifyDataSetChanged")
        private fun isItemContains(item: ContentNote, pos: Int){
            val isContains=selectedItems.contains(item)
            if(isContains)selectedItems.remove(item) else selectedItems.add(item)
            notifyDataSetChanged()
            listener?.selectedItemCount(selectedItems.size)
        }
    }

    interface CheckNoteAdapterListener{
        fun selectedMenuItem(menuItem: MenuItem,item: ContentNote,position: Int)
        fun editRequestItem(item: ContentNote,position: Int)
        fun selectedItemCount(count:Int)
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    }


}