package com.masterplus.notex.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.databinding.ItemSelectTextBinding
import com.masterplus.notex.roomdb.entities.BackupFile

class SelectBackupAdapter: RecyclerView.Adapter<SelectBackupAdapter.ViewHolder>() {

    private val diffUtils=object: DiffUtil.ItemCallback<BackupFile>(){
        override fun areItemsTheSame(oldItem: BackupFile, newItem: BackupFile): Boolean
                = oldItem.uid==newItem.uid

        override fun areContentsTheSame(oldItem: BackupFile, newItem: BackupFile): Boolean
                = oldItem == newItem
    }

    private val asyncListDiffer = AsyncListDiffer(this,diffUtils)
    var items:List<BackupFile>
        get() = asyncListDiffer.currentList
        set(value) = asyncListDiffer.submitList(value)

    var selectedItem: BackupFile? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field=value
            notifyDataSetChanged()
        }

    fun getItemPos(item: BackupFile):Int{
        return items.indexOf(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectBackupAdapter.ViewHolder {
        return ViewHolder(ItemSelectTextBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: SelectBackupAdapter.ViewHolder, position: Int) {
        val item=items[position]

        holder.binding.selectTextItem.text=item.name
        holder.binding.root.isSelected = item==selectedItem
    }

    override fun getItemCount(): Int = items.size




    inner class ViewHolder(val binding: ItemSelectTextBinding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                if(bindingAdapterPosition!=-1){
                    val item=items[bindingAdapterPosition]
                    selectedItem=item
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
    }


}