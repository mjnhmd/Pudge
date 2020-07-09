package com.pudge

import android.app.Activity
import android.view.Menu
import android.view.View
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object Alert{
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
                activity.intent
                if (activity.localClassName.contains("MsgRetransmitUI")){
                    val intent = activity.intent
                    val msgType = intent.getIntExtra("Retr_Msg_Type", -1);
                    val msgContent = intent.getStringExtra("Retr_Msg_content");
                    val msgId = intent.getLongExtra("Retr_Msg_Id", -1L);
                    val fileName = intent.getStringExtra("Retr_File_Name");
                    val fileList = intent.getStringArrayListExtra("Retr_File_Path_List")
                    XposedBridge.log("MJNMJNMJN MsgRetransmitUI msgType =$msgType,  msgContent = $msgContent   msgId = $msgId ")
                }
                XposedBridge.log("MJNMJN onActivityResuming: ${activity.localClassName}")
                Toast.makeText(activity, "Hello Wechat! resume", Toast.LENGTH_LONG).show()
            }
        })
    }

    val onCreateOptionsMenuHooker = Hooker {
        XposedHelpers.findAndHookMethod(
            "com,tencent.mm.ui.MMActivity",
            XposedInit.wxClassLoader,
            "onCreateOptionsMenu",
            C.Menu,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as? Activity ?: return
                    val menu = param.args[0] as? Menu ?: return

                }
            })
    }


}