package com.masterplus.notex.views.dialogFragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.masterplus.notex.MainActivity
import com.masterplus.notex.R
import com.masterplus.notex.managers.AuthHelper
import com.masterplus.notex.databinding.FragmentBackupBinding
import com.masterplus.notex.utils.CustomAlerts
import com.masterplus.notex.utils.CustomGetDialogs
import com.masterplus.notex.utils.ShowMessage
import com.masterplus.notex.utils.Utils
import com.masterplus.notex.viewmodels.BackupDiaViewModel
import com.masterplus.notex.viewmodels.items.SetSelectBackupFileViewModel
import com.masterplus.notex.views.view.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class BackupDiaFragment : CustomDialogFragment() {

    private var _binding:FragmentBackupBinding?=null
    private val binding get() = _binding!!
    private val viewModel:BackupDiaViewModel by viewModels()
    private val viewModelSelectBackupFile: SetSelectBackupFileViewModel by viewModels()
    private var progressBarDiaFragment:DialogFragment?=null
    private lateinit var registerSelectLocalBackup:ActivityResultLauncher<Intent>

    private var isPathVisible:Boolean=false

    @Inject
    lateinit var showMessage: ShowMessage

    @Inject
    lateinit var authHelper: AuthHelper

    @Inject
    lateinit var customAlerts: CustomAlerts

    @Inject
    lateinit var customGetDialogs: CustomGetDialogs



    private var firstBackupFile: File?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentBackupBinding.inflate(layoutInflater,container,false)

        firstBackupFile=File(requireActivity().application.externalMediaDirs.firstOrNull(),"backups").apply {
            if(!exists())
                mkdirs()
        }

        setObservers()
        setRegisters()
        setPathsVisibility(false)
        setPathClick()
        setButtonClicks()
        setPathTexts()


        return binding.root
    }

    private fun setObservers(){
        viewModel.isBackupDownloading.observe(viewLifecycleOwner, Observer {
            if(it){
                val title=getString(R.string.loading_backup_text)+"..."
                progressBarDiaFragment = customGetDialogs.getProgressBarDia(title)
                progressBarDiaFragment?.show(childFragmentManager,"")
            }else{
                if(progressBarDiaFragment?.isResumed == true)
                    progressBarDiaFragment?.dismiss()
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            binding.loadingLinearBackup.isVisible = it
            setButtonsEnabled(!it)
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.isNavigateToActivity.collect {
                        if(it){
                            val intent= Intent(requireContext(), MainActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                }
                launch {
                    viewModelSelectBackupFile.liveItem.collect {approvedBackupFile ->
                        viewModel.downloadCloudBackup(approvedBackupFile.fileId)
                    }
                }
                launch {
                    viewModel.isMessage.collect { message->
                        message?.let {
                            showMessage.showShort(message)
                        }
                    }
                }
            }
        }
    }

    private fun setRegisters(){
        registerSelectLocalBackup=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode==RESULT_OK&&it.data!=null){
                it.data!!.data?.let { it1 -> viewModel.loadLocalBackup(it1) }
            }
        }
    }

    private fun setButtonClicks(){
        binding.btLoadLocalBackup.setOnClickListener {
            customAlerts.showBackupOverrideExistingDataAlert(requireContext(),{a,b->
                val intentSelect = Intent(Intent.ACTION_OPEN_DOCUMENT).let { intent->
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.setType("*/*")
                }
                val intentChoicer=Intent.createChooser(intentSelect,"choice backup file")
                registerSelectLocalBackup.launch(intentChoicer)
            })
        }
        binding.btFormLocalBackup.setOnClickListener {
            if(isExternalStoragePermission())
                viewModel.formLocalBackup()
            else{
                requestExternalStoragePermission()
                showMessage.showShort(getString(R.string.file_read_per_req_text))
            }
        }
        binding.btLoadCloudBackup.setOnClickListener {
            if(!validateForCloud())
                return@setOnClickListener
            customGetDialogs.getSelectBackupDia()
                .show(childFragmentManager,"")
        }
        binding.btFormCloudBackup.setOnClickListener {
            if(!validateForCloud())
                return@setOnClickListener
            customAlerts.showFormNewCloudBackupAlert(requireContext()){x,y->
                viewModel.uploadCloudBackup()
            }
        }
    }

    private fun validateForCloud():Boolean{
        return when{
            !Utils.isInternetConnectionAvailable(requireContext())->{
                showMessage.showShort(getString(R.string.no_internet_text))
                false
            }
            !authHelper.isLogin()->{
                showMessage.showShort(getString(R.string.must_be_logged_text))
                false
            }
            else->true
        }
    }

    private fun setPathClick(){
        binding.textPathBackup.setOnClickListener {
            setPathsVisibility(!isPathVisible)
        }
    }

    private fun setCloudButtonsEnabled(isEnabled:Boolean){
        binding.btLoadCloudBackup.isEnabled=isEnabled
        binding.btFormCloudBackup.isEnabled=isEnabled
    }
    private fun setLocalButtonsEnabled(isEnabled:Boolean){
        binding.btFormLocalBackup.isEnabled=isEnabled
        binding.btLoadLocalBackup.isEnabled=isEnabled
    }
    private fun setButtonsEnabled(isEnabled:Boolean){
        setCloudButtonsEnabled(isEnabled)
        setLocalButtonsEnabled(isEnabled)
    }

    private fun setPathsVisibility(isVisible:Boolean){
        binding.text1PathBackup.isVisible=isVisible
        isPathVisible=isVisible
        val drawableId=if(isVisible) R.drawable.ic_baseline_keyboard_arrow_up_24 else R.drawable.ic_baseline_keyboard_arrow_down_24
        binding.textPathBackup.setCompoundDrawablesWithIntrinsicBounds(drawableId,0,0,0)
    }

    private fun setPathTexts(){
        binding.text1PathBackup.text=String.format("1-> %s",firstBackupFile?.let { if(it.exists())it.path else "" })
    }

    private fun isExternalStoragePermission():Boolean{
        return (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        == PackageManager.PERMISSION_GRANTED)
    }
    private fun requestExternalStoragePermission(){
        ActivityCompat.requestPermissions(requireActivity(),arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ,111)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}