package com.pudge.preference

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.*
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.pudge.R
import com.pudge.common.Global.ACTION_UPDATE_PREF
import com.pudge.common.Global.FOLDER_SHARED_PREFS
import com.pudge.common.Global.PUDGE_PACKAGE_NAME
import com.pudge.common.Global.SETTINGS_INTERFACE_HIDE_ICON
import com.pudge.common.Util.putExtra
import java.io.File

class PrefFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Move old shared preferences to device protected storage if it exists
            val oldPrefDir = "${activity?.applicationInfo?.dataDir}/$FOLDER_SHARED_PREFS"
            val newPrefDir = "${activity?.applicationInfo?.deviceProtectedDataDir}/$FOLDER_SHARED_PREFS"
            try {
                File(oldPrefDir).renameTo(File(newPrefDir))
            } catch (t: Throwable) {
                // Ignore this one
            }
            preferenceManager.setStorageDeviceProtected()
        }

        if (arguments != null) {
            val preferencesResId = arguments!!.getInt(ARG_PREF_RES)
            val preferencesName = arguments!!.getString(ARG_PREF_NAME)
            preferenceManager.sharedPreferencesName = preferencesName
            addPreferencesFromResource(preferencesResId)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            setBackgroundColor(ContextCompat.getColor(activity!!, R.color.card_background))
        }
    }

    override fun onStart() {
        super.onStart()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        when (key) {
            SETTINGS_INTERFACE_HIDE_ICON -> {
                // Hide/Show the icon as required.
                try {
                    val hide = preferences.getBoolean(SETTINGS_INTERFACE_HIDE_ICON, false)
                    val newState = if (hide) COMPONENT_ENABLED_STATE_DISABLED else COMPONENT_ENABLED_STATE_ENABLED
                    val className = "$PUDGE_PACKAGE_NAME.frontend.MainActivityAlias"
                    val componentName = ComponentName(PUDGE_PACKAGE_NAME, className)
                    activity!!.packageManager.setComponentEnabledSetting(componentName, newState, DONT_KILL_APP)
                } catch (t: Throwable) {
                    Log.e(TAG, "Cannot hide icon: $t")
                    Toast.makeText(activity, t.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                val value = preferences.all[key] ?: return
                activity?.sendBroadcast(Intent(ACTION_UPDATE_PREF).apply {
                    putExtra("key", key)
                    putExtra("value", value)
                })
            }
        }
    }

    companion object {
        private const val TAG = "PrefFragment"

        private const val ARG_PREF_RES = "preferencesResId"
        private const val ARG_PREF_NAME = "preferencesFileName"

        fun newInstance(preferencesResId: Int, preferencesName: String): PrefFragment {
            val fragment = PrefFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PREF_RES, preferencesResId)
                putString(ARG_PREF_NAME, preferencesName)
            }
            return fragment
        }
    }
}