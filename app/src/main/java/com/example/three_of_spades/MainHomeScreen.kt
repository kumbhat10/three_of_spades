package com.example.three_of_spades

import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main_home_screen.*
import kotlin.random.Random

class MainHomeScreen : AppCompatActivity() {
    private lateinit var soundError: MediaPlayer
    private lateinit var soundUpdate: MediaPlayer

    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var toast: Toast
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        soundUpdate = MediaPlayer.create(applicationContext,R.raw.player_moved)
        soundError = MediaPlayer.create(applicationContext,R.raw.error_entry)
        toast = Toast.makeText(applicationContext,"dd",Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER,0,20)
        toast.view.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.Black))
        toast.view.findViewById<TextView>(android.R.id.message).setTextColor(ContextCompat.getColor(applicationContext,R.color.font_yellow))
        toast.view.findViewById<TextView>(android.R.id.message).textSize = 16F

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_home_screen)
        MobileAds.initialize(this) {}   // initialize mobileAdds
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build()) // load the AD manually for the first time
        mInterstitialAd.adListener = object: AdListener() {
            override fun onAdClosed() {
            mInterstitialAd.loadAd(AdRequest.Builder().build())
        }}
//        if(mInterstitialAd.isLoaded){mInterstitialAd.show()}// Show for the first time

        val userName = intent.getStringExtra("USER_NAME")//get the intent that started this activity and extract the string
        welcomeUserNameview.apply { text = userName }//capture the layouts text view and set the text to username just extracted
    }
    fun createRoom(view: View) {
        val hostName = intent.getStringExtra("USER_NAME")
        val roomID =  Random.nextInt(10000, 99999).toString()
//        val roomData = mutableMapOf("Players" to mutableMapOf<String ,String>("p1" to hostName,"p2" to "","p3" to "","p4" to "","p5" to "","p6" to "","p7" to "") ,
//            "PlayersJoined" to 1,
//            "Sex" to mutableMapOf("p1" to selfSex ,"p2" to 0,"p3" to 1,"p4" to 0,"p5" to 1,"p6" to 0,"p7" to 1))
        val cardsShuffled =  (0..97).shuffled()  // create shuffled pack of 2 decks with 6 cards removed ( 7Player x 14 = 98 cards only)
//        val gameData = mutableMapOf("CardsinHand" to mutableMapOf<String ,List<Int>>("p1" to cardsShuffled.slice(0..12),"p2" to cardsShuffled.slice(13..26)
//            ,"p3" to cardsShuffled.slice(27..40),"p4" to cardsShuffled.slice(41..54),"p5" to cardsShuffled.slice(55..68)
//            ,"p6" to cardsShuffled.slice(69..82),"p7" to cardsShuffled.slice(83..96)) ,
//        "CardsCollected" to mutableMapOf("p1" to "","p2" to "","p3" to "","p4" to "","p5" to "","p6" to "","p7" to ""),
//        "CardsonTable" to "",        "GameState" to 0, // 0 means joining state
//        "PlayersJoined" to 1, "PlayerTurn" to 1, "Score" to mutableMapOf("p1" to "","p2" to "","p3" to "","p4" to "","p5" to "","p6" to "","p7" to ""),
//        "BidValue" to mutableMapOf("p1" to "","p2" to "","p3" to "","p4" to "","p5" to "","p6" to "","p7" to ""), "Rung" to "") // 1 - spades,2-heart 3-diamonds or 4- clubs
        val gameData = CreateGameData(hostName).gameData
        val roomData = CreateGameData(hostName).roomData
        Firebase.database.getReference("Rooms").removeValue()   //dummy
        Firebase.database.getReference("GameData").removeValue() // dummy

        Firebase.database.getReference("Rooms/$roomID").setValue(roomData)
        Firebase.database.getReference("GameData/$roomID").setValue(gameData)
        Log.d("Dushyant    ","written data MHS")
        soundUpdate.start()

        startActivity(Intent(this, CreatenJoinRoomScreen::class.java).apply { putExtra("roomID",roomID) }
                                    .apply {putExtra("selfName",hostName)  }.apply {putExtra("from","p1")  })
////        finish()
    }
    fun joinRoomCreated(view: View){

        val roomID = findViewById<EditText>(R.id.roomIDInput).text.toString()//read text field
        if(roomID.isNotEmpty()) {//if room id is entered
            Firebase.database.getReference("Rooms/$roomID/PlayersJoined").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(dataLoad: DatabaseError) {}
                override fun onDataChange(dataLoad: DataSnapshot) {
                   val playersJoined = dataLoad.value.toString()
                    if( playersJoined == "null"){
                        soundError.start()
//                        Toast.makeText(applicationContext,"No such Room ID exists", Toast.LENGTH_SHORT).show()
                        toastCenter("No such Room ID exists")
                        findViewById<EditText>(R.id.roomIDInput).hint = "Wrong ID"
                        findViewById<EditText>(R.id.roomIDInput).text.clear() }
                    else if(playersJoined.toInt() >6){ // dummy developemnt to allow multiple users
                        soundError.start()
//                        Toast.makeText(applicationContext,"This Room is Full", Toast.LENGTH_SHORT).show()
                        toastCenter("This Room is Full")
                        findViewById<EditText>(R.id.roomIDInput).hint = "Room Full"
                        findViewById<EditText>(R.id.roomIDInput).text.clear() }
                    else {
                        soundUpdate.start()
                        val playerJoining = playersJoined.toInt() + 1 //dummy development
                        val userName = intent.getStringExtra("USER_NAME")//from the intent that started this activity, extract the USERNAME
                        Firebase.database.getReference("Rooms/$roomID").child("PlayersJoined").setValue(playerJoining)
                        Firebase.database.getReference("Rooms/$roomID/Players").child("p$playerJoining").setValue(userName)
                        startActivity(Intent(applicationContext, CreatenJoinRoomScreen::class.java).apply { putExtra("roomID",roomID) }
                            .apply {putExtra("selfName",userName)  } .apply {putExtra("from","p$playerJoining")  })
//                        finish() // decide later if want to kill or not
                        }}})
             }
        else{
            soundError.start()
            findViewById<EditText>(R.id.roomIDInput).hint = "Enter ID"
//            Toast.makeText(applicationContext,"Please Enter Room ID", Toast.LENGTH_SHORT).show()
            toastCenter("Please Enter Room ID")
        }
    }
    fun joinRoomWindowOpen(view: View){
        soundUpdate.start()
        findViewById<FrameLayout>(R.id.joinRoomFrame).visibility = View.VISIBLE
    }
    fun joinRoomWindowExit(view: View){
        findViewById<FrameLayout>(R.id.joinRoomFrame).visibility = View.INVISIBLE
    }
    fun developerCredits(view: View){
        soundUpdate.start()//pass username and current activity alias to be able to come back with same info
        val userName = intent.getStringExtra("USER_NAME")//get the intent that started this activity and extract the string
        startActivity(Intent(this,DeveloperCredits::class.java).
            apply{putExtra("from",false)}.apply{putExtra("USER_NAME",userName)})
    }

    fun inviteFriends(view: View){
        soundUpdate.start()
        val message = "Let's Play 3of Spades(Kaali Teeri) online. Check out this game and Install from com.example.KaalikiTiggi"
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT,message)
        intent.type = "text/plain"
        startActivity(Intent.createChooser(intent,"Share Room ID via :"))
    }
    private fun toastCenter(message: String){
        toast.setText(message)
        toast.show()
    }

}