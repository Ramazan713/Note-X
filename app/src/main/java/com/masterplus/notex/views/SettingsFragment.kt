package com.masterplus.notex.views

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.masterplus.notex.MainActivity
import com.masterplus.notex.R
import com.masterplus.notex.utils.CustomAlerts
import com.masterplus.notex.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment @Inject constructor(private val sharedPreferences: SharedPreferences,
                                           private val customAlerts: CustomAlerts
)
    : PreferenceFragmentCompat() {
    private lateinit var sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener


    private lateinit var fontSizeTexts: Array<String>
    private lateinit var fontSizeValues: IntArray


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findPreference<Preference>("manageSubscription")?.let { preference ->
            preference.isVisible=MainActivity.isPremiumActive
        }
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        fontSizeTexts = resources.getStringArray(R.array.font_size_texts)
        fontSizeValues = resources.getIntArray(R.array.font_size_values)

        findPreference<Preference>("font_size_text")?.let { fontSizePref->
            fontSizePref.setOnPreferenceClickListener {
                customAlerts.showSelectFontSizeAlert(requireContext(),sharedPreferences){pos, textSize ->
                    fontSizePref.summary=fontSizeTexts[pos]
                }
                true
            }
        }

        findPreference<Preference>("rateApp")?.let { pref->
            pref.setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.masterplus.notex")
                intent.setPackage("com.android.vending")
                startActivity(intent)
                true
            }
        }

        findPreference<Preference>("shareApp")?.let { pref->
            pref.setOnPreferenceClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                val shareMessage = "https://play.google.com/store/apps/details?id=com.masterplus.notex"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "Choose One"))
                true
            }
        }

        findPreference<Preference>("manageSubscription")?.let { pref->
            pref.setOnPreferenceClickListener {
                val url="https://play.google.com/store/account/subscriptions?sku=premium_monthly&package=${context?.packageName}"

                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
                startActivity(browserIntent)
                true
            }
        }

        sharedPreferenceChangeListener=SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
            when(key){
                "setSelectLang"->{
                    findPreference<ListPreference>("setSelectLang")?.let { langListPref->
                        val vl=langListPref.value
                        if(vl=="tr"){
                            langListPref.summary=getString(R.string.lang_turkish)
                        }else{
                            langListPref.summary=getString(R.string.lang_english)
                        }
                        val intent= Intent(requireActivity().intent)
                        requireActivity().finish()
                        startActivity(intent)
                    }
                }


            }

        }

    }
    override fun onResume() {
        super.onResume()

        findPreference<ListPreference>("setSelectLang")?.let { langListPref->
            val vl=langListPref.value
            if(vl=="en"){
                langListPref.summary=getString(R.string.lang_english)
            }else{
                langListPref.summary=getString(R.string.lang_turkish)
            }
        }
        findPreference<Preference>("font_size_text")?.let { fontSizePref->
            val selectedText=fontSizeTexts[fontSizeValues.indexOf(Utils.getFontSize(sharedPreferences))]
            fontSizePref.summary=selectedText
        }

        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        setToolbarTitle(getString(R.string.settings_text))
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setToolbarTitle("")
    }
    private fun setToolbarTitle(title:String){
        Utils.setToolbarTitle(requireActivity(),title)
    }
}