package com.pudge

import android.app.Activity
import android.widget.Toast
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IActivityHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object Alert : IActivityHook{
    val onStartHooker = Hooker {
        XposedHelpers.findAndHookMethod(C.Activity, "onStart", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                XposedBridge.log("MJNMJN onActivityStarting: $activity")
                Toast.makeText(activity, "Hello Wechat!", Toast.LENGTH_LONG).show()
            }
        })
    }

    val onResumeHooker = Hooker {
        XposedHelpers.findAndHookMethod(C.Activity, "onResume", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as? Activity ?: return
                XposedBridge.log("MJNMJN onActivityResuming: $activity")
                Toast.makeText(activity, "Hello Wechat! resume", Toast.LENGTH_LONG).show()
            }
        })
    }


}