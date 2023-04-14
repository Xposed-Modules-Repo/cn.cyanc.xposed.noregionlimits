package cn.cyanc.xposed.noregionlimits

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.kiylx.m3preference.ui.BaseSettingsFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : BaseSettingsFragment() {
        @SuppressLint("WorldReadableFiles")
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "conf"
            context?.let {
                PreferenceManager.setDefaultValues(
                    it,
                    "conf",
                    Context.MODE_WORLD_READABLE,
                    R.xml.root_preferences,
                    true
                )
            }
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}