package com.masterplus.notex.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.databinding.ItemBillingPremiumBinding
import com.masterplus.notex.models.PremiumObject

class PremiumAdapter : RecyclerView.Adapter<PremiumAdapter.ViewHolder>() {

    private val diffUtils=object : DiffUtil.ItemCallback<PremiumObject>(){
        override fun areItemsTheSame(oldItem: PremiumObject, newItem: PremiumObject): Boolean {
            return oldItem==newItem
        }
        override fun areContentsTheSame(oldItem: PremiumObject, newItem: PremiumObject): Boolean {
            return oldItem==newItem
        }

    }
    private val recyclerListDiffer= AsyncListDiffer(this,diffUtils)
    private var listener:((item:PremiumObject)->Unit)? = null

    var items:List<PremiumObject>
        get() = recyclerListDiffer.currentList
        set(value) = recyclerListDiffer.submitList(value)

    fun setListener(listener:(item:PremiumObject)->Unit){
        this.listener=listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBillingPremiumBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=items[position]

        holder.binding.textTitlePreItem.text=item.title
        holder.binding.textDescriptionPreItem.text=item.description
        holder.binding.textPricePreItem.text=item.price
    }

    override fun getItemCount(): Int = items.size
    inner class ViewHolder(val binding:ItemBillingPremiumBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val item=items[bindingAdapterPosition]
                listener?.invoke(item)
            }
        }
    }

}