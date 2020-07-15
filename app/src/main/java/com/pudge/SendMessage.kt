package com.pudge

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.pudge.Caller.postSNS
import com.pudge.XposedInit.Companion.wxClasses
import com.pudge.XposedInit.Companion.wxPacakgeName
import com.pudge.common.C
import com.pudge.common.Hooker
import com.pudge.common.ReflectionUtil.findClassesFromPackage
import com.pudge.common.Util
import com.pudge.common.Util.wxLazy
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

/**
 * 右上角发消息按钮
 */
object SendMessage {
    fun provideStaticHookers(): List<Hooker>? {
        return listOf(
            onLauncherMenuHooker
        )
    }
    private val onLauncherMenuHooker = Hooker {
        XposedHelpers.findAndHookMethod(
            Limits.LauncherUI,
            "onCreateOptionsMenu",
            C.Menu,
            object : XC_MethodHook() {
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
                postSNS(activity)
            }
        }
        val sendSns = menu.add(0, 4, 0, "")
        val btnText2 = TextView(activity).apply {
            setTextColor(Color.parseColor("#FF36404A"))
            text = "朋友圈"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = context.dp2px(4)
            }
            setOnClickListener {
                postSNS(activity)
            }
        }

        sendMsg.actionView = LinearLayout(activity).apply {
            addView(btnText)
            orientation = LinearLayout.HORIZONTAL
        }

        sendSns.actionView = LinearLayout(activity).apply {
            addView(btnText2)
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


    val SnsUploadUI: Class<*> by wxLazy("SnsUploadUI") {
        findClassesFromPackage(XposedInit.wxClassLoader!!, wxClasses!!, "${XposedInit.wxPacakgeName}.plugin.sns.ui")
            .filterByField("$wxPacakgeName.plugin.sns.ui.LocationWidget")
            .filterByField("$wxPacakgeName.plugin.sns.ui.SnsUploadSayFooter")
            .firstOrNull()
    }


}