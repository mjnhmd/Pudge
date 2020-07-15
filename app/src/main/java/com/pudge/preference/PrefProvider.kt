package com.pudge.preference

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.CancellationSignal
import com.pudge.common.Global.PREFERENCE_NAME_SETTINGS
import com.pudge.common.Util.getProtectedSharedPreferences

// PrefProvider shares the preferences using content provider model.
class PrefProvider : ContentProvider() {

    private var settingPref: SharedPreferences? = null

    private fun getPreferenceType(value: Any?): String {
        return when (value) {
            null -> "Null"
            is Int -> "Int"
            is Long -> "Long"
            is Float -> "Float"
            is Boolean -> "Boolean"
            is String -> "String"
            is Set<*> -> "StringSet"
            else -> "${value::class.java}"
        }
    }

    override fun onCreate(): Boolean {
        settingPref  = context?.getProtectedSharedPreferences(PREFERENCE_NAME_SETTINGS, MODE_PRIVATE)
        return true
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Wechat Magician PrefProvider: Cannot modify read-only preferences!")
    }

    override fun query(
        uri: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ): Cursor? {
        val segments = uri.pathSegments
        if (segments.size != 1) {
            return null
        }
        val preference = settingPref
        return MatrixCursor(arrayOf("key", "value", "type")).apply {
            preference?.all?.forEach { entry ->
                val type = getPreferenceType(entry.value)
                addRow(arrayOf(entry.key, entry.value, type))
            }
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
        cancellationSignal: CancellationSignal?
    ): Cursor? {
        val segments = uri.pathSegments
        if (segments.size != 1) {
            return null
        }
        val preference = settingPref
        return MatrixCursor(arrayOf("key", "value", "type")).apply {
            preference?.all?.forEach { entry ->
                val type = getPreferenceType(entry.value)
                addRow(arrayOf(entry.key, entry.value, type))
            }
        }
    }


    override fun update(
        p0: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("Wechat Magician PrefProvider: Cannot modify read-only preferences!")
    }

    override fun delete(p0: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Wechat Magician PrefProvider: Cannot modify read-only preferences!")
    }
}