@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.os.Build
import java.text.SimpleDateFormat
import java.util.*

class CreateUser(username: String, userPhotoUrl: String) {
   @SuppressLint("SimpleDateFormat")
   val data = hashMapOf("n" to username, //username
       "ph" to userPhotoUrl, // photo URL
       "sc" to 4500, //start coins
       "scD" to 0, //start coins
       "w" to 0,  // total won games
       "w_daily" to 0,  // total won games Daily
       "w_bot" to 0,  // total won games against bot
       "b" to 0,  // total bided games
       "b_daily" to 0,  // total bided games Daily
       "b_bot" to 0,  // total bided games against bot
       "p" to 0, // total played games
       "p_daily" to 0, // total played games Daily
       "p_bot" to 0, // total played games against bot
       "pr" to 0,  // premium or not
       "LSD" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(), // last seen date
       "JD" to SimpleDateFormat("yyyyMMdd").format(Date()).toInt(), // Join date
       "nDRC" to 1, // consecutive days
       "claim" to 0,
       "rated" to 0,
       "phone" to "${Build.MANUFACTURER} ${Build.MODEL}",
       "phAPI" to Build.VERSION.SDK_INT
   )
}

@SuppressLint("SimpleDateFormat")
fun getChangedDate(date:Int, changeBy: Int = -1): Int{
    val dateFormat = SimpleDateFormat("yyyyMMdd")
    val dateInput = dateFormat.parse(date.toString())
    val calendar = Calendar.getInstance()
    calendar.time = dateInput
    calendar.add(Calendar.DATE, changeBy)
    return dateFormat.format(calendar.time).toInt()
}
@SuppressLint("SimpleDateFormat")
class GetFormattedDate(dateInput:Int) {
    val date = SimpleDateFormat("d").format(SimpleDateFormat("yyyyMMdd").parse(dateInput.toString())).toInt()
    val month = SimpleDateFormat("MMM").format(SimpleDateFormat("yyyyMMdd").parse(dateInput.toString())).toString()
    val dateMonth = SimpleDateFormat("d-MMM").format(SimpleDateFormat("yyyyMMdd").parse(dateInput.toString())).toString()

}