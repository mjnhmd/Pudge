package com.pudge

import android.app.Application
import android.content.Context
import android.content.Intent
import com.gh0u1l5.wechatmagician.spellbook.parser.ApkFile
import com.gh0u1l5.wechatmagician.spellbook.parser.ClassTrie
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam


/**
 * XposedInit
 *
 * @author mjn
 * @since 2020/06/12
 */
class XposedInit : IXposedHookLoadPackage {
    private val TAG = "Pudge"
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
//        XposedBridge.log("mjnmjnmjn handleLoadPackage ${lpparam.packageName}")
        if (!lpparam.packageName.equals("com.tencent.mm"))
            return
        if (!lpparam.processName.equals("com.tencent.mm"))
            return
        wxparam = lpparam
        wxClassLoader = lpparam.classLoader
        wxPacakgeName = lpparam.packageName
        XposedBridge.log("mjnmjnmjn 123tyuiopp!!!!!!!!!!!!!!!!!!!!!!!!!")
        try {
            XposedHelpers.findAndHookMethod(
                Application::class.java, "attach",
                Context::class.java, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        wxContext = param.args[0] as Context
                        wxClassLoader = wxContext!!.classLoader
                        ApkFile(lpparam.appInfo.sourceDir).use {
                            wxClasses = it.classTypes
                            hook()
                        }
                    }
                })
        } catch (t: Throwable) {
            XposedBridge.log("mjnmjnmjn Hook  error. ${t}")
            var message = ""
            t.stackTrace.forEach { message += (it.toString() + "\n") }
            XposedBridge.log("mjnmjnmjn Hook  error. $message")
        }
    }

    fun hook() {
        Alert.onResumeHooker.hook()
        Message.onInsertHooker.hook()
        Message.openDatabaseHooker.hook()
//        Message.reTransmitHooker.hook()
        Message.repeatHooker.hook()
        Limits.provideStaticHookers()?.forEach { it.hook() }
    }

    companion object {
        private const val PACKAGE_NAME = "com.pudge"
        var wxparam: LoadPackageParam? = null
        var wxClassLoader: ClassLoader? = null
        var wxPacakgeName: String? = null
        var wxClasses: ClassTrie? = null
        var wxContext: Context? = null

        fun startActivity(){
            val intent = Intent()
            intent.setPackage("com.tencent.mm")
            intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.transmit.MsgRetransmitUI")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("Retr_Msg_Type", 4);
            intent.putExtra("Retr_Msg_content", "hahahha");
            intent.getLongExtra("Retr_Msg_Id", -1L);
            wxContext!!.startActivity(intent)
        }
    }
}