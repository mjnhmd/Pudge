package com.pudge

import android.content.ContentValues
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*

object Message {
    val onInsertHooker = Hooker {
        XposedHelpers.findAndHookMethod(
            XposedInit.wxClassLoader!!.loadClass("com.tencent.wcdb.database.SQLiteDatabase"), "insertWithOnConflict",
            C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        val thisObject = param.thisObject
                        val table = param.args[0] as String
                        val nullColumnHack = param.args[1] as String?
                        val initialValues = param.args[2] as ContentValues?
                        val conflictAlgorithm = param.args[3] as Int
                        XposedBridge.log("mjnmjnmjn insertWithOnConflict beforeHookedMethod $initialValues")
                    }catch (e : Exception){
                        XposedBridge.log("mjnmjnmjn insertWithOnConflict beforeHookedMethod error $e")
                    }
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                    val thisObject = param.thisObject
                    val table = param.args[0] as String
                    val nullColumnHack = param.args[1] as String?
                    val initialValues = param.args[2] as ContentValues?
                    val conflictAlgorithm = param.args[3] as Int
                    val result = param.result as Long?
                    XposedBridge.log("mjnmjnmjn insertWithOnConflict afterHookedMethod $initialValues")
                    }catch (e : Exception){
                        XposedBridge.log("mjnmjnmjn insertWithOnConflict afterHookedMethod error $e")
                    }
                }
            })
    }

    val openDatabaseHooker = Hooker {
        XposedHelpers.findAndHookMethod(
            "com.tencent.wcdb.database.SQLiteDatabase",  // 被HOOK对象名
            XposedInit.wxClassLoader,  // classLoader，固定的值，不用关心
            "openDatabase",  // 被HOOK对象的函数名
            String::class.java,  // 参数0：数据库全路径
            ByteArray::class.java,  // 参数1：用户名密码（一个加密后的7位值）
            XposedInit.wxparam!!.classLoader.loadClass("com.tencent.wcdb.database.SQLiteCipherSpec"),  // 参数2：是一个SQLiteCipherSpec对象，该对象中包含了加密方式
            XposedInit.wxparam!!.classLoader.loadClass("com.tencent.wcdb.database.SQLiteDatabase\$CursorFactory"),  // 参数3：某工厂对象
            Int::class.javaPrimitiveType,  // 参数4：Flags，未知标记
            XposedInit.wxparam!!.classLoader.loadClass("com.tencent.wcdb.DatabaseErrorHandler"),  // 参数5：错误处理的句柄
            Int::class.javaPrimitiveType,  // 参数6：加密方式的某个参数PoolSize
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        // 参数0：数据库全路径
                        XposedBridge.log("mjnmjnmjn Path: " + param.args[0])
                        // 参数1：用户名密码（一个加密后的7位值）
                        XposedBridge.log(
                            "mjnmjnmjn Password: " + String(
                                (param.args[1] as ByteArray),
                                Charset.forName("UTF-8")
                            )
                        )
                        val formatter = Formatter()
                        for (b in param.args[1] as ByteArray) {
                            formatter.format("%02x", b)
                        }
                        XposedBridge.log("mjnmjnmjn Password (hex): 0x$formatter")
                        // 参数2：是一个SQLiteCipherSpec对象，该对象中包含了加密方式
                        XposedBridge.log(
                            "mjnmjnmjn CipherSpec - kdfAlgorithm: " + XposedHelpers.getIntField(
                                param.args[2], "kdfAlgorithm"
                            )
                        )
                        XposedBridge.log(
                            "mjnmjnmjn CipherSpec - kdfIteration: " + XposedHelpers.getIntField(
                                param.args[2], "kdfIteration"
                            )
                        )
                        XposedBridge.log(
                            "mjnmjnmjn CipherSpec - hmacAlgorithm: " + XposedHelpers.getIntField(
                                param.args[2], "hmacAlgorithm"
                            )
                        )
                        XposedBridge.log(
                            "mjnmjnmjn CipherSpec - Hmac Enabled: " + XposedHelpers.getBooleanField(
                                param.args[2], "hmacEnabled"
                            )
                        )
                        // 参数4：Flags，未知标记
                        XposedBridge.log("mjnmjnmjn Flags: " + param.args[4])
                        // 参数6：加密方式的某个参数PoolSize
                        XposedBridge.log("mjnmjnmjn PoolSize: " + param.args[6])
                    }catch (e : Exception){
                        XposedBridge.log("mjnmjnmjn openDatabaseHooker error $e")
                    }
                }
            })
    }

}