package com.pudge

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.pudge.common.Global
import com.pudge.common.Global.SETTINGS_SECRET_FRIEND
import com.pudge.common.Util.putExtra
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textview.text = "Pudge"
        layoutInflater.inflate(R.layout.view_demo, null)
        button.text = "打开微信"
        button.setOnClickListener {
            val intent = Intent()
            val cmp: ComponentName = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.component = cmp
            startActivity(intent)
        }

        pref_btn.setOnClickListener {
            sendBroadcast(Intent(Global.ACTION_UPDATE_PREF).apply {
                putExtra("key", SETTINGS_SECRET_FRIEND)
                putExtra("value", ratingBar.numStars)
            })
        }
    }
}
