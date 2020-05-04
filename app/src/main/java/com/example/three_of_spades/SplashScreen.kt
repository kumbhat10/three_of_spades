package com.example.three_of_spades

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashScreen: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val delayTime = 100L  // Splash Screen time
      Handler().postDelayed({startActivity(Intent(applicationContext,StartScreen::class.java));finish()},delayTime)
//        Handler().postDelayed({;finish()},3000L)

    }

}