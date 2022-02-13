package com.masterplus.notex

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.masterplus.notex.databinding.ActivityMainBinding
import com.masterplus.notex.databinding.DrawerNavMainLayoutBinding
import com.masterplus.notex.designpatterns.strategy.RootNoteArchiveItem
import com.masterplus.notex.designpatterns.strategy.RootNoteDefaultItem
import com.masterplus.notex.designpatterns.strategy.RootNoteReminderItem
import com.masterplus.notex.designpatterns.strategy.RootNoteTrashItem
import com.masterplus.notex.enums.RootNoteFrom
import com.masterplus.notex.models.ParameterRootNote
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.masterplus.notex.enums.NoteType
import com.masterplus.notex.models.LoginObject
import com.masterplus.notex.models.ParameterNote
import com.masterplus.notex.models.SharedNote
import com.masterplus.notex.utils.*
import com.masterplus.notex.viewmodels.BillingViewModel
import com.masterplus.notex.viewmodels.LoginViewModel
import com.masterplus.notex.views.dialogFragments.ProgressBarDiaFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.masterplus.notex.views.dialogFragments.PremiumDiaFr
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding
    private  var drawerBinding:DrawerNavMainLayoutBinding?=null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController:NavController
    private val billingViewModel: BillingViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    private var progressBarDiaFragment= ProgressBarDiaFragment()
    private val premiumDiaFragment=PremiumDiaFr()

    @Inject
    lateinit var showMessage: ShowMessage

    @Inject
    lateinit var customGetDialogs: CustomGetDialogs

    @Inject
    lateinit var customAlerts: CustomAlerts

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var authRegister:ActivityResultLauncher<Intent>

    companion object{
        var SAVED_NOTE_COUNT:Int=0
        const val MAX_SAVED_NOTE_COUNT=7

        val isPremiumActive:Boolean get() = BillingViewModel.mutableIsPremium
    }


    override fun attachBaseContext(newBase: Context?) {
        var newContext=newBase
        newBase?.let { context ->
            val sharedPreferencesSetting=PreferenceManager.getDefaultSharedPreferences(context)
            var defaultLang= Locale.getDefault().language
            if(defaultLang!="tr")
                defaultLang="en"

            val realLang=sharedPreferencesSetting.getString("setSelectLang",defaultLang)
            if(realLang!=null){
                val resources: Resources = context.resources
                val config=resources.configuration
                config.setLocale(Locale(realLang))
                newContext=context.createConfigurationContext(config)
                sharedPreferencesSetting.edit().putString("setSelectLang",realLang).apply()
            }
        }
        super.attachBaseContext(newContext)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerBinding=DrawerNavMainLayoutBinding.bind(binding.drawerNavView.getHeaderView(0))
        MobileAds.initialize(this) {}

        setSupportActionBar(binding.toolbar)
        navController=(supportFragmentManager.findFragmentById(R.id.frames) as NavHostFragment).navController
        appBarConfiguration= AppBarConfiguration.Builder(R.id.noteFragment,R.id.bookFragment,R.id.tagsFragment,
            R.id.settingsFragment)
            .setOpenableLayout(binding.drawerLayout).build()
        NavigationUI.setupWithNavController(binding.toolbar,navController,appBarConfiguration)

        binding.drawerNavView.setNavigationItemSelectedListener(drawerNavListener)
        binding.drawerNavView.itemIconTintList = null

        setBackgroundColor()

        binding.drawerNavView.getHeaderView(0)

        authRegister=registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.let { intent ->
                if(result.resultCode == RESULT_OK&&intent.data?.toString()?.contains("oauth2redirect?error") != true){
                    loginViewModel.logIn(intent)
                }else{
                    showMessage.showShort(getString(R.string.cancelled_text))
                }


            }
        }
        setSigningViews()

        adjustIntentActions()
        setObservers()
        checkPremiumActive()

    }

    override fun onResume() {
        super.onResume()
        billingViewModel.checkSubs()
    }

    private fun showLoadCloudBackupAlert(loginObject:LoginObject){
        if(loginObject.isNoteItemExists!=null){
            if(loginObject.isNoteItemExists)
                customAlerts.showBackupOverrideExistingDataAlert(this@MainActivity,
                    {a,b->//positive
                        loginViewModel.deleteContentTables()
                        loadTopOfCloudBackup()
                    },{a,b->//negative
                        loadTopOfCloudBackup()
                    })
            else
                loadTopOfCloudBackup()
        }else
            loadTopOfCloudBackup()

    }
    private fun loadTopOfCloudBackup(){
        loginViewModel.loadTopOfBackupFromCloud(getString(R.string.loading_backup_text)+"...")
    }

    private fun setObservers(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    loginViewModel.loginSuccess.collect {
                        it.backupFileSize?.let { size->
                            if(size>0){
                                customAlerts.showLoadBackupFromCloudFirstTime(this@MainActivity){x,y->
                                    showLoadCloudBackupAlert(it)
                                }
                            }
                        }
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                    }
                }
                launch {
                    loginViewModel.isError.collect {
                        it?.let {  showMessage.showShort(it)}
                    }
                }
            }
        }
        loginViewModel.isNavigateToActivity.observe(this, androidx.lifecycle.Observer {
            val intent = Intent(this@MainActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        })

        loginViewModel.isLoading.observe(this, androidx.lifecycle.Observer {
            if(it.isLoading){
                showProgressBarDia(it.title?:"")
            }else{
                dismissProgressBarDia()

            }
        })

    }
    private fun showProgressBarDia(title:String){
        if(!progressBarDiaFragment.isResumed){
            progressBarDiaFragment.arguments=Bundle().also { bundle->bundle.putString("title",title) }
            progressBarDiaFragment.show(supportFragmentManager,"")
        }

    }
    private fun dismissProgressBarDia(){
        try {
            progressBarDiaFragment.dismiss()
        }catch (ex:Exception){}

    }

    private fun adjustIntentActions(){

        when(intent.action){
            "com.masterplus.notex.views.DisplayTextNoteFragment"->{//from shortcuts
                if(intent.getStringExtra("extra_name")!=""){
                    navController.navigate(MainNavDirections.actionGlobalDisplayTextNoteFragment(ParameterNote()))
                    intent.putExtra("extra_name","")
                }
            }
            "com.masterplus.notex.views.DisplayCheckListNoteFragment"->{//from shortcuts
                if(intent.getStringExtra("extra_name")!=""){
                    navController.navigate(MainNavDirections.actionGlobalDisplayCheckListNoteFragment(ParameterNote()))
                    intent.putExtra("extra_name","")
                }
            }
            "$packageName/alarm" ->{//from alarm notification
                if(intent.getStringExtra("extra_name")!=""){
                    val noteType=intent.getSerializableExtra("noteType")
                    val noteId=intent.getLongExtra("noteId",0L)

                    if(noteId!=0L && noteType is NoteType){
                        val noteParameter=ParameterNote(noteId,isEmptyNote = false)
                        when(noteType){
                            NoteType.CheckList->{navController.navigate(MainNavDirections.actionGlobalDisplayCheckListNoteFragment(noteParameter))}
                            NoteType.Text->{navController.navigate(MainNavDirections.actionGlobalDisplayTextNoteFragment(noteParameter))}
                        }
                    }
                    intent.putExtra("extra_name","")
                }
            }
            Intent.ACTION_SEND-> {
                if ("text/plain" == intent.type) {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text->
                        if(text!=""){
                            val pair=UtilsShareNote.transformTextToContentNotes(text)
                            val strTitle:String?=intent.getStringExtra(Intent.EXTRA_TITLE)
                            val noteParameter=ParameterNote(null,sharedNote = SharedNote(strTitle,pair.second))
                            if(pair.first==NoteType.Text)
                                navController.navigate(MainNavDirections.actionGlobalDisplayTextNoteFragment(noteParameter))
                            else
                                navController.navigate(MainNavDirections.actionGlobalDisplayCheckListNoteFragment(noteParameter))
                            intent.putExtra(Intent.EXTRA_TEXT,"")
                        }
                    }
                }
            }
        }
    }

    private fun loadAdd(){
        binding.bannerAdd.apply {
            val adRequest = AdRequest.Builder().build()
            loadAd(adRequest)
        }
    }

    private fun checkPremiumActive(){
        billingViewModel.isPremiumLive.observe(this, androidx.lifecycle.Observer {
            if(!it)
                loadAdd()
            if(it && premiumDiaFragment.isResumed)
                premiumDiaFragment.dismiss()
            drawerBinding?.infoPremium?.isVisible = it
            binding.drawerNavView.menu.findItem(R.id.premium_drawer_menu_item).isVisible = !it
            binding.bannerAdd.isVisible = !it
        })
    }

    private fun setSigningViews(){
        drawerBinding?.let { drawerBinding->
            drawerBinding.btSignIn.also {
                it.visibility=View.VISIBLE
                it.setOnClickListener {
                    loginViewModel.authorize(authRegister)
                }
            }
            loginViewModel.liveUser.observe(this, androidx.lifecycle.Observer { user->
                val isUser= user!=null
                drawerBinding.infoEmail.text=if(isUser)user?.email?:"" else ""
                drawerBinding.infoUserName.text=if(isUser)user?.displayName?:"" else ""
                drawerBinding.btSignIn.isVisible = !isUser
                binding.drawerNavView.menu.findItem(R.id.sign_out_menu_item).isVisible = isUser
            })
        }
    }

    private fun setBackgroundColor(){
        Utils.changeToolBarColorByDefault(this)
        binding.frames.setBackgroundColor(getColor(R.color.default_background_color))
    }

    private val drawerNavListener= NavigationView.OnNavigationItemSelectedListener { it ->
        when (it.itemId) {
            R.id.tag_drawer_menu_item -> {
                navController.navigate(R.id.tagsFragment)
            }
            R.id.all_notes_drawer_menu_item -> {
                val parameterRootNote=ParameterRootNote(null,RootNoteDefaultItem(),RootNoteFrom.DEFAULT_NOTE)
                navController.navigate(MainNavDirections.actionGlobalNoteFragment(parameterRootNote))
            }
            R.id.note_book_drawer_menu_item -> {
                navController.navigate(R.id.bookFragment)
            }
            R.id.reminder_drawer_menu_item->{
                val parameterRootNote=ParameterRootNote(null,RootNoteReminderItem(),RootNoteFrom.REMINDER_FROM_NOTE)
                navController.navigate(MainNavDirections.actionGlobalNoteFragment(parameterRootNote))
            }
            R.id.archive_drawer_menu_item -> {
                val parameterRootNote=ParameterRootNote(null,RootNoteArchiveItem(),RootNoteFrom.ARCHIVE_FROM_NOTE)
                navController.navigate(MainNavDirections.actionGlobalNoteFragment(parameterRootNote))
            }
            R.id.trash_drawer_menu_item -> {
                val parameterRootNote=ParameterRootNote(null,RootNoteTrashItem(),RootNoteFrom.TRASH_FROM_NOTE)
                navController.navigate(MainNavDirections.actionGlobalNoteFragment(parameterRootNote))
            }
            R.id.settings_drawer_menu_item->{
                navController.navigate(R.id.settingsFragment)
            }
            R.id.backup_drawer_menu_item->{
                customGetDialogs.getBackupDia()
                    .show(supportFragmentManager,"")
            }
            R.id.sign_out_menu_item->{
                customAlerts.showSignOutAlert(this){x,y->
                    loginViewModel.signOut(getString(R.string.logging_out_text))
                    navController.navigate(R.id.noteFragment)
                }
            }
            R.id.premium_drawer_menu_item->{
                premiumDiaFragment.show(supportFragmentManager,"")
            }
        }


        binding.drawerLayout.closeDrawer(GravityCompat.START)
        true
    }
}