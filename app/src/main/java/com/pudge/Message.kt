package com.pudge

import android.content.ContentValues
import android.telecom.Call
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method
import java.nio.charset.Charset
import java.util.*


object Message {
    val TAG = "MessageHooker"
    /**
     * 新消息插入数据库
     */
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
                        if (table == "message") {
//                        XposedBridge.log("$TAG insertWithOnConflict beforeHookedMethod $initialValues")
                        }
                    }catch (e : Exception){
                        XposedBridge.log("$TAG insertWithOnConflict beforeHookedMethod error $e")
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
                        if (table == "message") {
                            XposedBridge.log("$TAG insertWithOnConflict afterHookedMethod $initialValues")
                        }
                    }catch (e : Exception){
                        XposedBridge.log("$TAG insertWithOnConflict afterHookedMethod error $e")
                    }
                }
            })
    }

    /**
     * 打开数据库操作，获取数据库密码
     */
    val openDatabaseHooker = Hooker {
        XposedHelpers.findAndHookMethod(
            "com.tencent.wcdb.database.SQLiteDatabase",  // 被HOOK对象名
            XposedInit.wxClassLoader,  // classLoader，固定的值，不用关心
            "openDatabase",  // 被HOOK对象的函数名
            String::class.java,  // 参数0：数据库全路径
            ByteArray::class.java,  // 参数1：用户名密码（一个加密后的7位值）
            XposedInit.wxClassLoader!!.loadClass("com.tencent.wcdb.database.SQLiteCipherSpec"),  // 参数2：是一个SQLiteCipherSpec对象，该对象中包含了加密方式
            XposedInit.wxClassLoader!!.loadClass("com.tencent.wcdb.database.SQLiteDatabase\$CursorFactory"),  // 参数3：某工厂对象
            Int::class.javaPrimitiveType,  // 参数4：Flags，未知标记
            XposedInit.wxClassLoader!!.loadClass("com.tencent.wcdb.DatabaseErrorHandler"),  // 参数5：错误处理的句柄
            Int::class.javaPrimitiveType,  // 参数6：加密方式的某个参数PoolSize
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        // 参数0：数据库全路径
                        XposedBridge.log("$TAG Path: " + param.args[0])
                        // 参数1：用户名密码（一个加密后的7位值）
                        XposedBridge.log(
                            "$TAG Password: " + String(
                                (param.args[1] as ByteArray),
                                Charset.forName("UTF-8")
                            )
                        )
                        val formatter = Formatter()
                        for (b in param.args[1] as ByteArray) {
                            formatter.format("%02x", b)
                        }
                        XposedBridge.log("$TAG Password (hex): 0x$formatter")
                        // 参数2：是一个SQLiteCipherSpec对象，该对象中包含了加密方式
                        XposedBridge.log(
                            "$TAG CipherSpec - kdfAlgorithm: " + XposedHelpers.getIntField(
                                param.args[2], "kdfAlgorithm"
                            )
                        )
                        XposedBridge.log(
                            "$TAG CipherSpec - kdfIteration: " + XposedHelpers.getIntField(
                                param.args[2], "kdfIteration"
                            )
                        )
                        XposedBridge.log(
                            "$TAG CipherSpec - hmacAlgorithm: " + XposedHelpers.getIntField(
                                param.args[2], "hmacAlgorithm"
                            )
                        )
                        XposedBridge.log(
                            "$TAG CipherSpec - Hmac Enabled: " + XposedHelpers.getBooleanField(
                                param.args[2], "hmacEnabled"
                            )
                        )
                        // 参数4：Flags，未知标记
                        XposedBridge.log("$TAG Flags: " + param.args[4])
                        // 参数6：加密方式的某个参数PoolSize
                        XposedBridge.log("$TAG PoolSize: " + param.args[6])
                    }catch (e : Exception){
                        XposedBridge.log("$TAG openDatabaseHooker error $e")
                    }
                }
            })
    }

        val afClass =
            XposedHelpers.findClass("com.tencent.mm.sdk.platformtools.af", XposedInit.wxClassLoader)
        //        //获取收到消息的标记通知栏
        val `b$1Class` =
            XposedHelpers.findClass("com.tencent.mm.booter.notification.b$1", XposedInit.wxClassLoader)

    /**
     * 这个方法是只能截到收到的消息，因为是劫持通知，不能截到发出的消息
     */
    val repeatHooker = Hooker {
        XposedHelpers.findAndHookMethod(`b$1Class`, "handleMessage",
            android.os.Message::class.java, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    super.beforeHookedMethod(param)
                    try {
                        val message = param.args[0] as android.os.Message
                        val string: String? =
                            message.data.getString("notification.show.talker")
                        val string2: String? =
                            message.data.getString("notification.show.message.content")
                        val i: Int = message.data.getInt("notification.show.message.type")
                        val i2: Int = message.data.getInt("notification.show.tipsflag")
                        XposedBridge.log("$TAG 发送者id： $string, 内容： $string2, $i, $i2, $afClass")
                        Caller.replyTextMessage(string2, string)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        XposedBridge.log("$TAG  ${e.localizedMessage}")
                    }
                    XposedBridge.log("$TAG send ok")
                }
            })
    }

}