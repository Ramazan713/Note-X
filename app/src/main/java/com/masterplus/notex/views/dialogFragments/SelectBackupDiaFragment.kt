package com.masterplus.notex.views.dialogFragments

import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.masterplus.notex.R
import com.masterplus.notex.viewmodels.SelectBackupViewModel
import com.masterplus.notex.adapters.SelectBackupAdapter
import com.masterplus.notex.databinding.FragmentSelectBackupDiaBinding
import com.masterplus.notex.utils.CustomAlerts
import com.masterplus.notex.utils.ShowMessage
import com.masterplus.notex.viewmodels.items.SetSelectBackupFileViewModel
import com.masterplus.notex.views.view.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectBackupDiaFragment : CustomDialogFragment() {
    private var _binding:FragmentSelectBackupDiaBinding?=null
    private val binding get() = _binding!!

    private val viewModel: SelectBackupViewModel by viewModels()
    private val viewModelSelectBackupFile:SetSelectBackupFileViewModel by viewModels({requireParentFragment()})

    private val recyclerAdapter=SelectBackupAdapter()
    private var isRefreshAvailable=true
    private var counter:CountDownTimer?=null

    @Inject
    lateinit var showMessage: ShowMessage

    @Inject
    lateinit var customAlerts:CustomAlerts

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val recyclerState=savedInstanceState?.getParcelable<Parcelable>("recyclerState")
        binding.recyclerFromSelectBackup.layoutManager?.onRestoreInstanceState(recyclerState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.stateSelectedTextObject.value=recyclerAdapter.selectedItem
        val recyclerState=binding.recyclerFromSelectBackup.layoutManager?.onSaveInstanceState()
        outState.putParcelable("recyclerState",recyclerState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentSelectBackupDiaBinding.inflate(layoutInflater,container,false)

        viewModel.loadBackupMetaFiles()
        viewModel.checkTimeOutForRefresh()



        binding.recyclerFromSelectBackup.adapter=recyclerAdapter
        binding.recyclerFromSelectBackup.layoutManager= LinearLayoutManager(requireContext())

        binding.btCancelFromSelectBackup.setOnClickListener {
            dismiss()
        }

        binding.btApproveFromSelectBackup.setOnClickListener {
            customAlerts.showBackupOverrideExistingDataAlert(requireContext(),{a,b->
                val selectedBackupFile=recyclerAdapter.selectedItem
                if(selectedBackupFile!=null){
                    viewModelSelectBackupFile.setItem(selectedBackupFile)
                    this.dismiss()
                }
            })
        }
        binding.refreshFromSelectBackup.setOnClickListener {
            recyclerAdapter.selectedItem=null
            viewModel.loadBackupMetaFilesFromCloud()
        }
        reloadForConfigurationChange()
        setObservers()

        return binding.root
    }

    private fun setObservers(){
        viewModel.backupFiles.observe(viewLifecycleOwner, Observer {
            recyclerAdapter.items=it
            if(it.isEmpty()){
                binding.warningFromSelectBackup.text=getString(R.string.empty_backup_text)
            }
        })
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            binding.progressBarFromSelectBackup.isGone = !it
            binding.recyclerFromSelectBackup.isGone = it
            binding.warningFromSelectBackup.isGone = it
            binding.refreshFromSelectBackup.isEnabled = isRefreshAvailable && !it
        })
        viewModel.timeOutForRefreshItems.observe(viewLifecycleOwner, Observer {
            if(it>0){
                isRefreshAvailable=false
                binding.refreshFromSelectBackup.isEnabled=false
                counter=object: CountDownTimer(it,1000) {
                    override fun onTick(p0: Long) {
                        binding.textTimerFromSelectBackup.text=((p0/1000)+1).toString()
                    }
                    override fun onFinish() {
                        binding.textTimerFromSelectBackup.text=""
                        isRefreshAvailable=true
                        binding.refreshFromSelectBackup.isEnabled=true
                    }
                }.start()

            }else{
                isRefreshAvailable=true
            }

        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.isError.collect { error->
                        error?.let { showMessage.showLong(it) }
                    }
                }
            }
        }
    }


    private fun reloadForConfigurationChange(){
        viewModel.stateSelectedTextObject.value?.also { stateItem->
            recyclerAdapter.selectedItem=stateItem
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        counter?.cancel()
    }

}