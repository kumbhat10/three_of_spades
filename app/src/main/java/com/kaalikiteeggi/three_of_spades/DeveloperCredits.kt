@file:Suppress("UNUSED_PARAMETER")

package com.kaalikiteeggi.three_of_spades

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_developer_credits.*
import kotlinx.android.synthetic.main.activity_game_screen.*
import kotlin.random.Random

class DeveloperCredits : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_credits)
        findViewById<ImageView>(R.id.icon_3developer).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite_zoom))
        val vn = packageManager.getPackageInfo(packageName,0).versionName
        val vc = packageManager.getPackageInfo(packageName,0).versionCode
        findViewById<TextView>(R.id.sizeDC).text = "VC: $vc\nVN: $vn\n W: ${resources.configuration.screenWidthDp.toString()}\nH: ${resources.configuration.screenHeightDp.toString()}"
    }
    fun openWebsite(view: View){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://sites.google.com/view/kaali-ki-teeggi/") }
        startActivity(intent)
    }

//    fun exitDeveloperScreen(view: View) {
//        val parentScreen = intent.getBooleanExtra("from", true) //true means come from login screen
//        if (parentScreen) {
//            startActivity(Intent(this, StartScreen::class.java))
//        } else {
//            val userName = intent.getStringExtra("USER_NAME")//get the intent that started this activity and extract the string
//            val intent2 = Intent(this, MainHomeScreen::class.java).apply { putExtra("USER_NAME",userName) }
//            startActivity(intent2)
//        }
//    }
//    fun clockwise(view:View){
//        soundUpdate.start()
//        when(Random.nextInt(1, 3)){
//            1 ->imageViewDeveloperCredits.startAnimation(AnimationUtils
//                .loadAnimation(applicationContext, R.anim.clockwise))
//            2 ->imageViewDeveloperCredits.startAnimation(AnimationUtils
//                .loadAnimation(applicationContext, R.anim.myanimation))
//        }
//        when(Random.nextInt(1, 3)){
//            2 ->imageViewNumber3.startAnimation(AnimationUtils
//                .loadAnimation(applicationContext, R.anim.clockwise))
//            1 ->imageViewNumber3.startAnimation(AnimationUtils
//                .loadAnimation(applicationContext, R.anim.myanimation))
//        }
//    }
}
