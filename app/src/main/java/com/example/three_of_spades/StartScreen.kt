package com.example.three_of_spades

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class StartScreen : AppCompatActivity() {
    private lateinit var soundUpdate: MediaPlayer
    private lateinit var soundError: MediaPlayer
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        soundError = MediaPlayer.create(applicationContext,R.raw.error_entry)
        soundUpdate = MediaPlayer.create(applicationContext,R.raw.player_moved)
        toast = Toast.makeText(applicationContext,"dd",Toast.LENGTH_SHORT)
//        toast.setGravity(Gravity.CENTER,0,20)
        toast.view.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.Black))
        toast.view.findViewById<TextView>(android.R.id.message).setTextColor(ContextCompat.getColor(applicationContext,R.color.font_yellow))
        toast.view.findViewById<TextView>(android.R.id.message).textSize = 16F
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)
//        gifStartScreen.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise_slow_startscreen))

        val sharedPreferences = getSharedPreferences("PREFS",Context.MODE_PRIVATE)  //init preference file in private mode
        val editText = findViewById<EditText>(R.id.textInputEditText)

        if(sharedPreferences.contains("localUsername")){ // Check if User history available
            editText.setText(sharedPreferences.getString("localUsername",""))
            soundUpdate.start() // sound to welcome user when open for first time
//            Toast.makeText(applicationContext,
//                "Welcome back "+sharedPreferences.getString("localUsername",""), Toast.LENGTH_SHORT).show()

            toastCenter("Welcome back "+sharedPreferences.getString("localUsername",""))
        }
        else{
            soundError.start()
//            Toast.makeText(applicationContext,"Please Enter your name", Toast.LENGTH_SHORT).show()
            toastCenter("Please Enter your name")
            editText.hint = "Enter Name"
        }
    }

    fun submitButton(view: View) {
        val userInput = findViewById<EditText>(R.id.textInputEditText).text.toString()//read text field
        if(userInput.isNotEmpty()) {//if player name is entered
            val sharedPreferences = getSharedPreferences("PREFS",Context.MODE_PRIVATE)  //init preference file in private mode
            val editor  = sharedPreferences.edit();
            editor.putString("localUsername",userInput) // write username to preference file
            editor.apply();
            val database = Firebase.database //init firebase instance
            val myRef = database.getReference("users/$userInput")
            myRef.setValue("")
            soundUpdate.start()
            startActivity(Intent(this, MainHomeScreen::class.java)
                .apply {putExtra("USER_NAME",userInput ) })
//            finish()  // decide later if want to finish this
        }
        else {
//            Toast.makeText(applicationContext,"No Name entered", Toast.LENGTH_SHORT).show()
            toastCenter("No Name entered")
            findViewById<EditText>(R.id.textInputEditText).hint = "Enter Name"
            soundError.start()
        }
    }
    fun developerCredits(view: View){
        soundUpdate.start()
        startActivity( Intent(this,DeveloperCredits::class.java).apply { putExtra("from",true) })
         }
    private fun toastCenter(message: String){
        toast.setText(message)
        toast.show()
    }
}




