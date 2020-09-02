package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class CreateUser(username: String, userPhotoUrl: String) {
   @SuppressLint("SimpleDateFormat")
   val data = hashMapOf("n" to username, //username
       "ph" to userPhotoUrl, // photo URL
       "sc" to 4500, //start coins
       "w" to 0,  // total won games
       "w_bot" to 0,  // total won games against bot
       "b" to 0,  // total bided games
       "b_bot" to 0,  // total bided games against bot
       "p" to 0, // total played games
       "p_bot" to 0, // total played games against bot
       "pr" to 0,  // premium or not
       "LSD" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(), // last seen date
       "JD" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(), // Join date
       "nDRC" to 1, // consecutive days
       "claim" to 0,
       "phone" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
   )
}