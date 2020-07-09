package com.pudge

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.pudge.ReflectionUtil.findClassesFromPackage
import com.pudge.XposedInit.Companion.wxClassLoader
import com.pudge.XposedInit.Companion.wxClasses
import com.pudge.XposedInit.Companion.wxPacakgeName
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import java.lang.reflect.Field

fun Context.dp2px(dip: Int): Int {
    val scale = resources.displayMetrics.density
    return (dip * scale + 0.5f).toInt()
}
object Limits{
    fun provideStaticHookers(): List<Hooker>? {
        return listOf(createHooker, onCreateOptionsMenuHooker, onCheckSelectLimitHooker, onSelectAllContactHooker,onLauncherMenuHooker)
    }
    val AlbumPreviewUI = XposedInit.wxClassLoader!!.loadClass("${XposedInit.wxPacakgeName}.plugin.gallery.ui.AlbumPreviewUI")
    val SelectContactUI = XposedInit.wxClassLoader!!.loadClass("${XposedInit.wxPacakgeName}.ui.contact.SelectContactUI")
    val LauncherUI = XposedInit.wxClassLoader!!.loadClass("${XposedInit.wxPacakgeName}.ui.LauncherUI")
    val createHooker = Hooker {
        findAndHookMethod(C.Activity, "onCreate", C.Bundle, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                val savedInstanceState = param.args[0] as Bundle?
                when (activity::class.java) {
                    AlbumPreviewUI -> {
                        // Bypass the limit on number of photos the user can select
                        val intent = activity.intent ?: return
                        val oldLimit = intent.getIntExtra("max_select_count", 9)
                        val newLimit = 1000
                        if (oldLimit <= 9) {
                            intent.putExtra("max_select_count", oldLimit + newLimit - 9)
                        }
                    }
                    SelectContactUI -> {
                        // Bypass the limit on number of recipients the user can forward.
                        val intent = activity.intent ?: return
                        if (intent.getIntExtra("max_limit_num", -1) == 9) {
                            intent.putExtra("max_limit_num", 0x7FFFFFFF)
                        }
                    }
                }
            }
        })
    }


    private val onCreateOptionsMenuHooker = Hooker {
        findAndHookMethod(wxClassLoader!!.loadClass("$wxPacakgeName.ui.MMActivity"), "onCreateOptionsMenu", C.Menu, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                XposedBridge.log("MJNMJNMJN  onCreateOptionsMenuHooker $activity")
                val menu = param.args[0] as? Menu ?: return
                when (activity::class.java) {
                    SelectContactUI -> {
                        dealSelectUI(activity, menu)
                    }
                    LauncherUI -> {
                        dealLauncherUI(activity, menu)
                    }
                }

            }
        })
    }

    private val onLauncherMenuHooker = Hooker {
        findAndHookMethod(LauncherUI, "onCreateOptionsMenu", C.Menu, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                XposedBridge.log("MJNMJNMJN  onLauncherMenuHooker $activity")
                val menu = param.args[0] as? Menu ?: return
                dealLauncherUI(activity, menu)
            }

        })
    }

    private fun dealLauncherUI(activity: Activity, menu: Menu) {
        val sendMsg = menu.add(0, 3, 0, "")
        sendMsg.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        val btnText = TextView(activity).apply {
            setTextColor(Color.parseColor("#FF36404A"))
            text = "Send"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = context.dp2px(4)
            }
            setOnClickListener {
                showSendDialog(activity)
            }
        }

        sendMsg.actionView = LinearLayout(activity).apply {
            addView(btnText)
            orientation = LinearLayout.HORIZONTAL
        }
    }

    private fun showSendDialog(activity: Activity){
        val et = EditText(activity)
        val dialog = AlertDialog.Builder(activity).setTitle("给指定人发送消息")
            .setView(et)
            .setPositiveButton("确定"
            ) { p0, _ ->
                val input = et.text.toString()
                Caller.transmitMsg(input)
                p0.dismiss()
            }
            .setNegativeButton("取消") { p0, _ ->
                p0.dismiss()
            }.create()
        dialog.show()
    }


    private fun dealSelectUI(activity: Activity, menu: Menu){
        val intent = activity.intent ?: return
        val checked = intent.getBooleanExtra("select_all_checked", false)

        val selectAll = menu.add(0, 2, 0, "")
        selectAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        val btnText = TextView(activity).apply {
            setTextColor(Color.WHITE)
            text = "ALL"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = context.dp2px(4)
            }
        }
        val btnCheckbox = CheckBox(activity).apply {
            isChecked = checked
            setOnCheckedChangeListener { _, checked ->
                onSelectContactUISelectAll(activity, checked)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = context.dp2px(6)
            }
        }
        selectAll.actionView = LinearLayout(activity).apply {
            addView(btnText)
            addView(btnCheckbox)
            orientation = LinearLayout.HORIZONTAL
        }
    }

    val SelectConversationUI = XposedInit.wxClassLoader!!.loadClass("${XposedInit.wxPacakgeName}.ui.transmit.SelectConversationUI")
    val SelectConversationUI_checkLimit = Util.findMethodsByExactParameters(SelectConversationUI, C.Boolean, C.Boolean).firstOrNull()

    // Hook SelectConversationUI to bypass the limit on number of recipients.
    private val onCheckSelectLimitHooker = Hooker {
        hookMethod(SelectConversationUI_checkLimit, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.result = false
            }
        })
    }

    // Hook SelectContactUI to help the "Select All" button.
    private val onSelectAllContactHooker = Hooker {
        findAndHookMethod(
                SelectContactUI, "onActivityResult",
                C.Int, C.Int, C.Intent, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val requestCode = param.args[0] as Int
                val resultCode = param.args[1] as Int
                val data = param.args[2] as Intent?

                if (requestCode == 5) {
                    val activity = param.thisObject as Activity
                    activity.setResult(resultCode, data)
                    activity.finish()
                    param.result = null
                }
            }
        })
    }
    val ContactInfo = findClassesFromPackage(wxClassLoader!!, wxClasses!!, "$wxPacakgeName.storage")
        .filterByMethod(C.String, "getCityCode")
        .filterByMethod(C.String, "getCountryCode")
        .firstOrNull()
    // Handle the logic about "select all" check box in SelectContactUI
    private fun onSelectContactUISelectAll(activity: Activity, isChecked: Boolean) {
        val intent = activity.intent ?: return
        intent.putExtra("select_all_checked", isChecked)
        intent.putExtra("already_select_contact", "")
        if (isChecked) {
            // Search for the ListView of contacts
            val listView = XposedHelpers.findFirstFieldByExactType(activity::class.java, ListView::class.java)
                    .get(activity) as ListView? ?: return
            val adapter = (listView.adapter as HeaderViewListAdapter).wrappedAdapter

            // Construct the list of user names
            var contactField: Field? = null
            var usernameField: Field? = null
            val userList = mutableListOf<String>()
            repeat(adapter.count) next@ { index ->
                val item = adapter.getItem(index)

                if (contactField == null) {
                    contactField = item::class.java.fields.firstOrNull {
                        it.type.name == ContactInfo!!.name
                    } ?: return@next
                }
                val contact = contactField?.get(item) ?: return@next

                if (usernameField == null) {
                    usernameField = contact::class.java.fields.firstOrNull {
                        it.name == "field_username"
                    } ?: return@next
                }
                val username = usernameField?.get(contact) ?: return@next
                userList.add(username as String)
            }
            intent.putExtra("already_select_contact", userList.joinToString(","))
        }
        activity.startActivityForResult(intent, 5)
    }
}