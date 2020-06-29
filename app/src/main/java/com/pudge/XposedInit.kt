package com.pudge

import android.app.Application
import android.content.Context
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
                        wxClassLoader = (param.args[0] as Context).classLoader
                        hook()
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
        Message.repeatHooker.hook()
    }

    companion object {
        private const val PACKAGE_NAME = "com.pudge"
        var wxparam: LoadPackageParam? = null
        var wxClassLoader: ClassLoader? = null
        var wxPacakgeName: String? = null
    }
}