package com.pudge

import android.app.Activity
import android.content.Context
import android.content.Intent
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method
import java.util.*

object Caller {
    val TAG = "MessageHooker"
    fun replyTextMessage(strContent: String?, strChatroomId: String?) {
        XposedBridge.log("$TAG  准备回复消息内容：content:$strContent,chatroomId:$strChatroomId")
        if (strContent == null || strChatroomId == null || strContent.length == 0 || strChatroomId.length == 0) {
            return
        }
        try {
        //构造new里面的参数：l iVar = new i(aao, str, hQ, i2, mVar.cvb().fD(talkerUserName, str));
        val classiVar: Class<*> = XposedHelpers.findClassIfExists(
            "com.tencent.mm.modelmulti.i",
            XposedInit.wxClassLoader
        )
        val objectiVar: Any = XposedHelpers.newInstance(classiVar,
            arrayOf(
                String::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Any::class.java
            ),
            strChatroomId,
            strContent,
            1,
            1,
            object : HashMap<String?, String?>() {
                init {
                    put(strChatroomId, strChatroomId)
                }
            })
        val objectParamiVar = arrayOf(objectiVar, 0)

        //创建静态实例对象au.DF()，转换为com.tencent.mm.ab.o对象
        val classG: Class<*> =
            XposedHelpers.findClassIfExists("com.tencent.mm.kernel.g", XposedInit.wxClassLoader)
        XposedBridge.log("$TAG before callStaticMethod Eh")
        val objectG: Any = XposedHelpers.callStaticMethod(classG, "Eh")
        XposedBridge.log("$TAG after callStaticMethod a")
        val objectdpP: Any = XposedHelpers.getObjectField(objectG, "dpP")


        //查找au.DF().a()方法
        val classDF: Class<*> =
            XposedHelpers.findClassIfExists("com.tencent.mm.ab.o", XposedInit.wxClassLoader)
        val classI: Class<*> =
            XposedHelpers.findClassIfExists("com.tencent.mm.ab.l", XposedInit.wxClassLoader)
        val methodA: Method = XposedHelpers.findMethodExactIfExists(
            classDF,
            "a",
            classI,
            Int::class.javaPrimitiveType
        )

        //调用发消息方法
            XposedBridge.invokeOriginalMethod(methodA, objectdpP, objectParamiVar)
            XposedBridge.log("$TAG  invokeOriginalMethod()执行成功")
        } catch (e: Exception) {
            XposedBridge.log("$TAG  调用微信消息回复方法异常")
            XposedBridge.log(e)
        }
    }

    fun transmitMsg(strContent: String?) {
        XposedBridge.log("$TAG  准备转发消息内容：content:$strContent")
        if (strContent == null || strContent.isEmpty()) {
            return
        }
        try {
            val intent = Intent()
            intent.setPackage("com.tencent.mm")
            intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.transmit.MsgRetransmitUI")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("Retr_Msg_Type", 4);
            intent.putExtra("Retr_Msg_content", strContent);
            intent.getLongExtra("Retr_Msg_Id", -1L);
            XposedInit.wxContext!!.startActivity(intent)
        } catch (e: Exception) {
            XposedBridge.log("$TAG  调用微信消息回复方法异常")
            XposedBridge.log(e)
        }
    }



    fun postSNS(activity: Context){
        val intent = Intent(activity, SendMessage.SnsUploadUI)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra("Ksnsforward", true)
            .putExtra("Ksnsupload_type", 9)
            .putExtra("Kdescription", "test 朋友圈 ")
//            when {
//                snsInfo?.contentUrl != null -> {
//                    val buffer = FileUtil.readBytesFromDisk("$storage/.cache/0.thumb")
//                    intent.putExtra("Ksnsupload_type", 1)
//                        .putExtra("Ksnsupload_title", snsInfo.title)
//                        .putExtra("Ksnsupload_link", snsInfo.contentUrl)
//                        .putExtra("Ksnsupload_imgbuf", buffer)
//                }
//                snsInfo?.medias?.isEmpty() == false -> {
//                    when (snsInfo.medias[0].type) {
//                        "2" -> {
//                            intent.putStringArrayListExtra(
//                                "sns_kemdia_path_list",
//                                ArrayList((0 until snsInfo.medias.size).map {
//                                    "$storage/.cache/$it"
//                                })
//                            )
//                            intent.removeExtra("Ksnsupload_type")
//                        }
//                        "6" -> {
//                            intent.putExtra("Ksnsupload_type", 14)
//                                .putExtra("sight_md5", snsInfo.medias[0].main?.md5)
//                                .putExtra("KSightPath", "$storage/.cache/0")
//                                .putExtra("KSightThumbPath", "$storage/.cache/0.thumb")
//                        }
//                    }
//                }
//            }
        activity.startActivity(intent)
    }

}