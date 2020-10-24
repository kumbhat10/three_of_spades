@file:Suppress("UNUSED_PARAMETER", "DEPRECATION")

package com.kaalikiteeggi.three_of_spades

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.util.Strings
import com.google.protobuf.LazyStringArrayList
import kotlinx.android.synthetic.main.activity_developer_credits.*
import kotlinx.android.synthetic.main.activity_game_screen.*
import java.net.URISyntaxException
import java.util.ArrayList
import kotlin.random.Random

class DeveloperCredits : AppCompatActivity() {
    private lateinit var text:String
    private lateinit var uid:String
    private lateinit var vn:String
    private lateinit var vc:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_credits)
        findViewById<ImageView>(R.id.icon_3developer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite_zoom))
        vn = packageManager.getPackageInfo(packageName,0).versionName
        vc = packageManager.getPackageInfo(packageName,0).versionCode.toString()
        uid = intent.getStringExtra("uid")!!.toString()    //Get roomID and display
        text = "VC: $vc\nVN: $vn\n W: ${resources.configuration.screenWidthDp.toString()}\nH: ${resources.configuration.screenHeightDp.toString()}\nUser ID: $uid"
        findViewById<TextView>(R.id.sizeDC).text = text
    }
    fun openWebsite(view: View){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://sites.google.com/view/kaali-ki-teeggi/") }
        startActivity(intent)
    }
    fun copyToClipBoard(view: View){
        Toast.makeText(applicationContext,"Text copied to clipboard",Toast.LENGTH_SHORT).show()
        val clipBoard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Text copied",text)
        clipBoard.setPrimaryClip(clip)
    }

    fun sendEmail(view: View){
        val body = "(Auto generated info for debugging purpose) " +
                "\nAndroid: ${Build.VERSION.RELEASE}" +
                "\nAPI#:      ${Build.VERSION.SDK_INT}" +
                "\nVC:         $vc " +
                "\nVN:         $vn " +
                "\nWidth:    ${resources.configuration.screenWidthDp} " +
                "\nHeight:   ${resources.configuration.screenHeightDp} " +
                "\nDevice:   ${android.os.Build.MANUFACTURER} ${Build.MODEL}" +
                "\n\n\n Dear Team, I need support for...."

        val mailTo = "mailto:kaaliteerifun@gmail.com" +
                "?cc=" + "kumbhat10@gmail.com" +
                "&subject=" + Uri.encode("User ID: $uid") +
                "&body=" + Uri.encode(body)
    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        emailIntent.setDataAndType(Uri.parse(mailTo), "text/plain");
        emailIntent.data = Uri.parse(mailTo);

        startActivity(emailIntent)


    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_right_activity,R.anim.slide_right_activity)
        finish()
    }

}
