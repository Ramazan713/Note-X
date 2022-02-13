package com.masterplus.notex

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.masterplus.notex.databinding.ActivityPopUpAddNoteBinding
import com.masterplus.notex.enums.PopUpAddNoteTypes
import com.masterplus.notex.models.copymove.CheckItemCopyMoveObject
import com.masterplus.notex.roomdb.entities.ContentNote
import com.masterplus.notex.utils.CustomGetDialogs
import com.masterplus.notex.utils.ShowMessage
import com.masterplus.notex.viewmodels.PopUpAddNoteViewModel
import com.masterplus.notex.viewmodels.items.SetCopyMoveItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PopUpAddNoteActivity : AppCompatActivity() {

    lateinit var binding:ActivityPopUpAddNoteBinding
    private var addNoteType=PopUpAddNoteTypes.NEW_TEXT_NOTE
    private val addNoteTypeList=PopUpAddNoteTypes.values().asList()
    private val viewModel:PopUpAddNoteViewModel by viewModels()
    private val viewModelSelectNoteListener: SetCopyMoveItemViewModel by viewModels()
    private var lastEditState:Boolean? = null

    private lateinit var spinnerAdapter: ArrayAdapter<String>

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var showMessage: ShowMessage

    @Inject
    lateinit var imm:InputMethodManager

    @Inject
    lateinit var customGetDialogs: CustomGetDialogs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPopUpAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setUpSpinner()
        setUpButtons()
        setUpEditTexts()
        setObservers()

        intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.let { text->
            binding.editContentPopUp.setText(text)
        }
        setEditState(true)
    }

    private fun setObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.isExecuted.collect {
                        showMessage.showShort(getString(R.string.success_text))
                        finish()
                    }
                }
                launch {
                    viewModelSelectNoteListener.liveItem.collect {
                        finish()
                    }
                }
            }
        }

    }


        private fun setEditState(isEdit:Boolean){
        if(lastEditState==null || lastEditState!=isEdit){
            lastEditState=isEdit
            if(!isEdit){
                closeKeyboard()
                binding.editContentPopUp.clearFocus()
                binding.editTitlePopUp.clearFocus()
            }else{
                binding.editContentPopUp.requestFocus()
            }
            binding.editContentPopUp.isCursorVisible=isEdit
            binding.editTitlePopUp.isCursorVisible=isEdit
            val imageId=if(isEdit) R.drawable.ic_baseline_done_24 else R.drawable.ic_baseline_edit_24
            binding.imagePopUpEdit.setImageDrawable(ContextCompat.getDrawable(this,imageId))
        }
    }

    private fun setUpEditTexts(){
        binding.editContentPopUp.setKeyBackListener(editTextBackKeyListener)
        binding.editTitlePopUp.setKeyBackListener(editTextBackKeyListener)
    }


    private fun getSpinnerItemsArray():MutableList<String>{
        val spinnerItems= mutableListOf<String>()
        addNoteTypeList.forEach {
            when(it){
                PopUpAddNoteTypes.NEW_TEXT_NOTE->spinnerItems.add(getString(R.string.new_text_note))
                PopUpAddNoteTypes.OLD_CHECK_NOTE->spinnerItems.add(getString(R.string.existing_check_note))
                PopUpAddNoteTypes.NEW_CHECK_NOTE->spinnerItems.add(getString(R.string.new_check_note))
            }
        }
        return spinnerItems
    }

    private fun setUpButtons(){

        binding.imagePopUpExit.setOnClickListener {
            finish()
        }

        binding.imagePopUpEdit.setOnClickListener {
            setEditState(if(lastEditState!=null)!lastEditState!! else true)
        }

        binding.btPopUpSave.setOnClickListener {
            val content=binding.editContentPopUp.text?.trim()
            if(content==null)
                showMessage.showShort(getString(R.string.text_field_not_empty_text))
            else{
                val contentNote=ContentNote(_text = content.toString())
                if(addNoteType==PopUpAddNoteTypes.OLD_CHECK_NOTE){
                    saveAsOldCheckNote(contentNote)
                }else{
                    val title=binding.editTitlePopUp.text.toString()
                    viewModel.saveNote(contentNote,title,addNoteType)
                }
            }
        }
    }

    private fun setUpSpinner(){
        val spinnerItemsArray=getSpinnerItemsArray()
        spinnerAdapter= ArrayAdapter(this,android.R.layout.simple_list_item_1,spinnerItemsArray)

        val spinnerSelection = sharedPreferences.getInt("popUpAddNoteSpinnerPos",PopUpAddNoteTypes.NEW_TEXT_NOTE.ordinal)
        addNoteType = addNoteTypeList[spinnerSelection]

        binding.spinnerPopUp.apply {
            adapter=spinnerAdapter
            setSelection(spinnerSelection)

            onItemSelectedListener=object :AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                    addNoteType = addNoteTypeList[position]
                    sharedPreferences.edit().putInt("popUpAddNoteSpinnerPos",position).apply()
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
    }

    private fun closeKeyboard(){
        imm.hideSoftInputFromWindow(currentFocus?.windowToken,0)
    }
    private val editTextBackKeyListener= object :(Boolean)->Unit{
        override fun invoke(isActive:Boolean): Unit {
            setEditState(isActive)
        }
    }

    private fun saveAsOldCheckNote(contentNote: ContentNote){
        val copyMove=CheckItemCopyMoveObject(true, listOf(contentNote),0L,this)
        customGetDialogs.getCopyMoveDia(copyMove)
            .show(supportFragmentManager,"")
    }

}