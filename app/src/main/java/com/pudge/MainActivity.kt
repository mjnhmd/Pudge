package com.pudge

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.pudge.ReflectionUtil.findClassesFromPackage
import com.pudge.XposedInit.Companion.wxClassLoader
import com.pudge.XposedInit.Companion.wxClasses
import com.pudge.XposedInit.Companion.wxPacakgeName
import de.robv.android.xposed.IXposedHookLoadPackage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textview.text = "Pudge"
        layoutInflater.inflate(R.layout.view_demo, null)
        button.text = "发朋友圈"
        button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                TODO("Not yet implemented")
            }

        })
        button.setOnClickListener {
//            val SnsUploadUI = findClassesFromPackage(wxClassLoader!!, wxClasses!!, "$wxPacakgeName.plugin.sns.ui")
//                    .filterByField("$wxPacakgeName.plugin.sns.ui.LocationWidget")
//                    .filterByField("$wxPacakgeName.plugin.sns.ui.SnsUploadSayFooter")
//                    .firstOrNull()

            XposedInit.startActivity()
//            val intent = Intent()
//            intent.setClassName("com.tencent.mm", "com.plugin.sns.ui.SNSUploadUI")
//                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                .putExtra("Ksnsforward", true)
//                .putExtra("Ksnsupload_type", 9)
//                .putExtra("Kdescription", "test 朋友圈 ")
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
//            context.get()?.startActivity(intent)
        }
    }
}
