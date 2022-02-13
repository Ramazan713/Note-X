package com.masterplus.notex.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.masterplus.notex.R
import com.masterplus.notex.databinding.ItemUltimateNoteBasicBinding
import com.masterplus.notex.databinding.ItemUltimateNoteExtendedBinding
import com.masterplus.notex.databinding.ViewAlarmCardBinding
import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.enums.NoteType
import com.masterplus.notex.enums.ReminderTypes
import com.masterplus.notex.roomdb.models.UltimateNote
import com.masterplus.notex.roomdb.views.CompletedNoteView
import com.masterplus.notex.utils.*
import javax.inject.Inject


class NotePagingAdapter @Inject constructor(private val context:Context): PagingDataAdapter<UltimateNote,RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var holderType:Int = HolderExtendedFLAG
    private var selectedItems= mutableListOf<UltimateNote>()
    private var searchedText:String=""
    private var listener:NotePagingAdapterListener? = null

    private var isLongClickEnabled:Boolean=true
    private var isLongClickActive:Boolean=false
    private var isContentsVisible:Boolean=true
    private var isTagsVisible:Boolean=true
    private var isNoteHalfSize:Boolean=false

    companion object{
        private const val HolderExtendedFLAG=1
        private const val HolderBasicFLAG=2
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UltimateNote>() {
            override fun areItemsTheSame(oldItem: UltimateNote, newItem: UltimateNote) = oldItem.noteView.noteId == newItem.noteView.noteId

            override fun areContentsTheSame(
                oldItem: UltimateNote, newItem: UltimateNote) = oldItem==newItem
                    && oldItem.contentBriefs==newItem.contentBriefs
                    && oldItem.noteView==newItem.noteView
        }
    }

    override fun getItemViewType(position: Int): Int {
        return holderType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { item->
            val contextView=holder.itemView.context
            when(holder){
                is ViewHolderExtended->{

                    setAlarmReminderViews(holder.binding.alarmViewEx,item.noteView,contextView)
                    showTagsTextAndSetVisibility(holder.binding.noteTags,item.noteView.tagText)
                    item.noteView.title.let { title->
                        holder.binding.noteTitle.text=if(searchedText!="") getHighLightedText(title,searchedText)else title
                    }
                    holder.binding.noteImageType.setImageDrawable(ContextCompat.getDrawable(contextView,getNoteTypeBackgroundDrawableId(item.noteView.typeContent)))

                    holder.binding.noteContent.let { itemContent->
                        itemContent.visibility=getContentVisibility()
                        itemContent.showShortContents(item.contentBriefs,item.noteView.typeContent,item.noteView.contentSize?:1,
                        isNoteHalfSize,searchedText)
                    }
                    holder.binding.noteDate.showTime(item.noteView.updateDate)

                    //for setting background on searching or pinning
                    setExtraImageViewBackgroundAndVisibility(holder.binding.noteImageExtra,item.noteView,contextView)
                    holder.binding.root.isSelected = selectedItems.contains(item)

                    holder.binding.root.setCardBackgroundColor(getColorUIMode(item.noteView.color))

                }
                is ViewHolderBasic->{
                    setAlarmReminderViews(holder.binding.alarmViewBs,item.noteView,contextView)
                    item.noteView.title.let { title->
                        holder.binding.noteTitleS.text=if(searchedText!="") getHighLightedText(title,searchedText)
                            else (if(title!="") title else context.getString(R.string.unnamed_text))
                    }
                    setExtraImageViewBackgroundAndVisibility(holder.binding.noteImageExtraS,item.noteView,contextView)
                    holder.binding.noteDateS.showTime(item.noteView.updateDate)
                    holder.binding.noteImageTypeS.setImageDrawable(ContextCompat.getDrawable(contextView,getNoteTypeBackgroundDrawableId(item.noteView.typeContent)))
                    holder.binding.root.isSelected = selectedItems.contains(item)
                    holder.binding.root.setCardBackgroundColor(getColorUIMode(item.noteView.color))

                }
                else -> {}
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            HolderBasicFLAG->{ViewHolderBasic(ItemUltimateNoteBasicBinding.inflate(LayoutInflater.from(parent.context),parent,false))}
            else->{ViewHolderExtended(ItemUltimateNoteExtendedBinding.inflate(LayoutInflater.from(parent.context),parent,false))}
        }
    }

    private fun getColorUIMode(color:String):Int{
        return Utils.getColorUIMode(context,color)
    }

    private fun getContentVisibility():Int{
        return if(isContentsVisible)View.VISIBLE else View.GONE
    }

    private fun getNoteTypeBackgroundDrawableId(noteType: NoteType):Int{
        return if(noteType==NoteType.Text) R.drawable.ic_baseline_note_add_24 else R.drawable.ic_baseline_check_24
    }

    private fun showTagsTextAndSetVisibility(textView:TextView,tagTexts:String?){
        if(isTagsVisible&&tagTexts!=null){
            textView.visibility=View.VISIBLE
            val restrictedLength:Int=if(isNoteHalfSize)21 else 30
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_label_24,0,0,0)
            textView.text = if(tagTexts.length>=restrictedLength)tagTexts.substring(0,restrictedLength)+"..." else tagTexts
        }else{
            textView.visibility=View.GONE
        }
    }

    private fun setAlarmReminderViews(binding:ViewAlarmCardBinding,noteView: CompletedNoteView,context: Context){
        val reminderDate=noteView.reminderDate
        val reminderType=noteView.reminderType
        val reminderDone=noteView.reminderDone
        if(reminderDone!=null&&reminderDate!=null&&reminderType!=null&&!reminderDone){
            binding.root.visibility=View.VISIBLE
            val backgroundId:Int = if(reminderType==ReminderTypes.NOT_REPEATED)R.drawable.ic_baseline_access_time_24
                else R.drawable.ic_baseline_repeat_24

            binding.imageAlarmView.setImageDrawable(ContextCompat.getDrawable(context,backgroundId))
            binding.root.setCardBackgroundColor(Utils.getColorUIMode(context,noteView.color))
            binding.textAlarmView.showTime(reminderDate)
        }else{
            binding.root.visibility=View.GONE
        }
    }

    //for setting background on searching or pinning
    private fun setExtraImageViewBackgroundAndVisibility(extraImageView:ImageView,note:CompletedNoteView,context: Context){
        extraImageView.visibility=View.VISIBLE
        if(searchedText!=""){
            when(note.kindNote){
                NoteKinds.ARCHIVE_KIND->{extraImageView.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_baseline_archive_24))}
                NoteKinds.TRASH_KIND->{extraImageView.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_baseline_delete_24))}
                NoteKinds.ALL_KIND->{extraImageView.visibility=View.GONE}
            }
        }else{
            if(note.weight!=0){
                extraImageView.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_baseline_push_pin_24))
            }else{
                extraImageView.visibility=View.GONE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public fun setInit(listener: NotePagingAdapterListener?, isContentsVisible: Boolean = true,
                       isTagsVisible: Boolean = true, isNoteHalfSize:Boolean = false){
        this.listener=listener
        this.isContentsVisible=isContentsVisible
        this.isTagsVisible=isTagsVisible
        this.isNoteHalfSize=isNoteHalfSize
        holderType = if(isContentsVisible||isTagsVisible) HolderExtendedFLAG else HolderBasicFLAG
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    public fun setIsNoteHalfSize(isHalf:Boolean){
        this.isNoteHalfSize=isHalf
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    public fun setAppearance(isContentsVisible:Boolean, isTagsVisible:Boolean){
        this.isContentsVisible=isContentsVisible
        this.isTagsVisible=isTagsVisible
        holderType=if(isContentsVisible||isTagsVisible) HolderExtendedFLAG else HolderBasicFLAG
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    public fun setSearchText(searchedText:String){
        this.searchedText=searchedText
        isLongClickEnabled = searchedText==""
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    public fun selectAll(){
        selectedItems.clear()
        selectedItems.addAll(snapshot().items)
        listener?.isLongClick(selectedItems.size,isLongClickActive)
        notifyDataSetChanged()
    }
    public fun setListener(listener: NotePagingAdapterListener){
        this.listener=listener
    }
    public fun getSelectedItems() = selectedItems
    fun getSelectedItemSize() = selectedItems.size

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItems(items:List<UltimateNote>){
        selectedItems.clear()
        selectedItems.addAll(items)
        isLongClickActive = selectedItems.isNotEmpty()
        listener?.isLongClick(selectedItems.size,isLongClickActive)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    public fun exitEdit(){
        selectedItems.clear()
        isLongClickActive=false
        notifyDataSetChanged()
    }


    private fun onClickExecutor(adapterPosition:Int){
        if(adapterPosition!=-1){
            getItem(adapterPosition)?.let { item->
                if(isLongClickActive){
                    setOnLongClickExecution(item,adapterPosition)
                }else{
                    listener?.clickedNote(item,adapterPosition)
                }
            }
        }
    }
    private fun onLongClickExecutor(adapterPosition: Int){
        if(adapterPosition!=-1&&isLongClickEnabled){
            getItem(adapterPosition)?.let { item->
                setOnLongClickExecution(item,adapterPosition)
            }
        }
    }

    private fun setOnLongClickExecution(item: UltimateNote,adapterPosition:Int){
        if(selectedItems.contains(item)){
            selectedItems.remove(item)
        }else{
            selectedItems.add(item)
        }
        val selectedItemsSize=selectedItems.size
        isLongClickActive = selectedItemsSize>0
        listener?.isLongClick(selectedItemsSize,isLongClickActive)
        notifyItemChanged(adapterPosition)
    }

    inner class ViewHolderExtended(val binding: ItemUltimateNoteExtendedBinding):RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                onClickExecutor(bindingAdapterPosition)
            }
            itemView.setOnLongClickListener {
                onLongClickExecutor(bindingAdapterPosition)
                true
            }
        }
    }

    inner class ViewHolderBasic(val binding:ItemUltimateNoteBasicBinding):RecyclerView.ViewHolder(binding.root){
        init {
            itemView.setOnClickListener {
                onClickExecutor(bindingAdapterPosition)
            }
            itemView.setOnLongClickListener {
                onLongClickExecutor(bindingAdapterPosition)
                true
            }
        }
    }



    interface NotePagingAdapterListener{
        fun clickedNote(item:UltimateNote,pos:Int)
        fun isLongClick(selectedSize:Int,isLongActive:Boolean)
    }
}