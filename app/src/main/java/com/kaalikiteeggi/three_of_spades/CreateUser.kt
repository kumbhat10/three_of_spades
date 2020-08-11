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
       "b" to 0,  // total bided games
       "p" to 0, // total played games
       "pr" to 0,  // premium or not
       "LSD" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(), // last seen date
       "JD" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(), // Join date
       "nDRC" to 1, // consecutive days
       "claim" to 0
   )
}