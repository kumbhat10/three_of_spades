package com.example.three_of_spades

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_join_room_screen.*

class CreatenJoinRoomScreen : AppCompatActivity() {
    private lateinit var soundUpdate: MediaPlayer
    private lateinit var soundError: MediaPlayer
    private lateinit var soundSuccess:MediaPlayer
    private val myRefRooms    = Firebase.database.getReference("Rooms") // initialize database reference
    private val myRefGameData = Firebase.database.getReference("GameData") // initialize database reference
    private lateinit var joiningRoomListener:ValueEventListener
    private lateinit var gameStateListener:ValueEventListener
    private lateinit var roomID: String
    private lateinit var selfName: String
    private lateinit var from: String
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_create_join_room_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        soundUpdate = MediaPlayer.create(applicationContext,R.raw.player_moved)
        soundError = MediaPlayer.create(applicationContext,R.raw.error_entry)
        soundSuccess= MediaPlayer.create(applicationContext,R.raw.player_success_chime)
        toast = Toast.makeText(applicationContext,"dd",Toast.LENGTH_SHORT)
        toast.view.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.Black))
        toast.view.findViewById<TextView>(android.R.id.message).setTextColor(ContextCompat.getColor(applicationContext,R.color.font_yellow))
        toast.view.findViewById<TextView>(android.R.id.message).textSize = 16F

        roomID   = intent.getStringExtra("roomID")    //Get roomID and display
        selfName = intent.getStringExtra("selfName") //Get Username first  - selfName ,roomID available
        from     = intent.getStringExtra("from")    //check if user has joined room or created one and display Toast
        findViewById<ImageView>(R.id.imageViewShareButton).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite))
//        if(from !="p1"){Toast.makeText(applicationContext,"Room Joined Successfully", Toast.LENGTH_SHORT).show()}  // not host  //dummy disabled
//        else if(from =="p1"){Toast.makeText(applicationContext,"Room Created Successfully", Toast.LENGTH_SHORT).show()} // host
        findViewById<Button>(R.id.button_roomID).text = "Room ID $roomID"   // display the room ID
        Firebase.database.getReference("Rooms/$roomID/Players/p1").  // display the host info in joining room screen
            addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(errorDataLoad: DatabaseError) {}
                override fun onDataChange(dataLoad: DataSnapshot) {button_roomID_host.text = "Host : " +dataLoad.value.toString()  } })
    }

    override fun onStart() {
        super.onStart()
        fun updateRoomInfo(dataLoad: DataSnapshot ) {   // function to be called if roomListener detects any Room
            val from   = intent.getStringExtra("from")   //check if user has joined room or created one and display Toast
            val temp =  dataLoad.child("PlayersJoined").value.toString()
            val playerJoining = temp.toInt()
//            val p1 = dataLoad.child("Players").child("p1").value.toString()
            val p2 = dataLoad.child("Players").child("p2").value.toString()
            val p3 = dataLoad.child("Players").child("p3").value.toString()
            val p4 = dataLoad.child("Players").child("p4").value.toString()
            val p5 = dataLoad.child("Players").child("p5").value.toString()
            val p6 = dataLoad.child("Players").child("p6").value.toString()
            val p7 = dataLoad.child("Players").child("p7").value.toString()
            val p2S = (dataLoad.child("Sex").child("p2").value as Long).toInt()
            val p3S = (dataLoad.child("Sex").child("p3").value as Long).toInt()
            val p4S = (dataLoad.child("Sex").child("p4").value as Long).toInt()
            val p5S = (dataLoad.child("Sex").child("p5").value as Long).toInt()
            val p6S = (dataLoad.child("Sex").child("p6").value as Long).toInt()
            val p7S = (dataLoad.child("Sex").child("p7").value as Long).toInt()
            when{
                (playerJoining==7 && p7!="") -> {
//                        if(p7!=selfName){Toast.makeText(this@CreatenJoinRoomScreen, "$p7 has joined !",Toast.LENGTH_SHORT).show()} //show player 7 name
                    soundSuccess.start()
                    gifViewJoiningRoom2.visibility= View.VISIBLE
                    gifViewJoiningRoom1.visibility = View.INVISIBLE
                    progressBarMain.isIndeterminate = false
                    imageViewShareButton.clearAnimation()
                    imageViewShareButton.visibility = View.INVISIBLE

                    startGameButton.visibility = View.VISIBLE   // progressbar animation stop and show start button
                    startGameButton.isEnabled =true
                    waitingToJoinText.text = getString(R.string.playerJoinedConfirmation) // display Ready to start
                    waitingToJoinText.setTextColor(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4)) //set color to green
                    waitingToJoinText.typeface = Typeface.DEFAULT_BOLD // mAKE IT BOLD
                    waitingToJoinText.startAnimation(AnimationUtils
                        .loadAnimation(applicationContext, R.anim.blink_infinite_300ms))
                    startGameButton.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.blink_infinite_300ms))

                    if(from=="p1") {//Toast.makeText(this@CreatenJoinRoomScreen,"You can START the game now", Toast.LENGTH_LONG).show()} // only to HOST
                    } else {startGameButton.text = getString(R.string.hostToStart)  // to Everyone else - set HOST TO START message on button
                        startGameButton.textSize= 35F   //decrease start button font size
                        }
                }

                (playerJoining==6 && p6!="") ->{if(p6!=selfName){
//                    Toast.makeText(this@CreatenJoinRoomScreen, "$p6 has joined !",Toast.LENGTH_SHORT).show()
                    toastCenter("$p6 has joined !")
                    soundUpdate.start()}
                    progressBarMain.indeterminateDrawable.setTint(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer7))}
                (playerJoining==5 && p5!="") ->{if(p5!=selfName){
//                    Toast.makeText(this@CreatenJoinRoomScreen, "$p5 has joined !",Toast.LENGTH_SHORT).show()
                    toastCenter("$p5 has joined !")
                    soundUpdate.start()}
                    progressBarMain.indeterminateDrawable.setTint(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer6))}
                (playerJoining==4 && p4!="") ->{if(p4!=selfName){
//                    Toast.makeText(this@CreatenJoinRoomScreen, "$p4 has joined !",Toast.LENGTH_SHORT).show()
                    toastCenter("$p4 has joined !")
                    soundUpdate.start()}
                    progressBarMain.indeterminateDrawable.setTint(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer5))}
                (playerJoining==3 && p3!="") ->{if(p3!=selfName){
//                    Toast.makeText(this@CreatenJoinRoomScreen, "$p3 has joined !",Toast.LENGTH_SHORT).show()
                    toastCenter("$p3 has joined !")
                    soundUpdate.start()}
                    progressBarMain.indeterminateDrawable.setTint(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer4))}
                (playerJoining==2 && p2!="") ->{if(p2!=selfName){
//                    Toast.makeText(this@CreatenJoinRoomScreen, "$p2 has joined !",Toast.LENGTH_SHORT).show()
                    toastCenter("$p2 has joined !")
                    soundUpdate.start()}
                    progressBarMain.indeterminateDrawable.setTint(ContextCompat.getColor(applicationContext,R.color.progressBarPlayer3))}
            }
            if(p2!="" && playerJoining>=2) {// Show respective player names and their own colors and stop progress bar
                player2Text.text = p2
                player2ProgressBar.isIndeterminate = false
            if(p2S==0)player2ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_boy)
                if(p2S==1)player2ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_girl) } // this will change it from indeterminate spinner to spade photo
            if(p3!="" && playerJoining>=3) {
                player3Text.text = p3
                player3ProgressBar.isIndeterminate = false
                if(p3S==0)player3ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_boy)
                if(p3S==1)player3ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_girl) }
            if(p4!="" && playerJoining>=4) {
                player4Text.text = p4
                player4ProgressBar.isIndeterminate = false
                if(p4S==0)player4ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_boy)
                if(p4S==1)player4ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_girl) }
            if(p5!="" && playerJoining>=5) {
                player5Text.text = p5
                player5ProgressBar.isIndeterminate = false
                if(p5S==0)player5ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_boy)
                if(p5S==1)player5ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_girl) }
            if(p6!="" && playerJoining>=6) {
                player6Text.text = p6
                player6ProgressBar.isIndeterminate = false
                if(p6S==0)player6ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_boy)
                if(p6S==1)player6ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_girl) }
            if(p7!="" && playerJoining==7) {
                player7Text.text = p7
                player7ProgressBar.isIndeterminate = false
                if(p7S==0)player7ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_boy)
                if(p7S==1)player7ProgressBar.progressDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.joining_girl) }
        }  // keep updating the screen as the users join
        joiningRoomListener = object : ValueEventListener{// call updateRoomInfo function if someone joins
        override fun onDataChange(dataLoad: DataSnapshot) {
            updateRoomInfo(dataLoad)
        }
            override fun onCancelled(errorDataLoad: DatabaseError) {}}
        gameStateListener   = object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(GameState: DataSnapshot) {
                if((GameState.value as Long).toInt() == 1){
                    soundUpdate.start()
                    Handler().postDelayed(Runnable {
                    startActivity(Intent(this@CreatenJoinRoomScreen,GameScreen::class.java).apply { putExtra("selfName",selfName) }
                        .apply { putExtra("from",from) }.apply { putExtra("roomID",roomID) })
                        finish()  },0)
//                     //finish this activity before starting Game
                }
            }
        }
        myRefRooms.child(roomID).addValueEventListener(joiningRoomListener) //attach the created roomListener
        myRefGameData.child(roomID).child("GS").addValueEventListener(gameStateListener) // attach the created game data listener

//    updateGameStateToPlay(View(applicationContext)) // dummy forced write game state to 1
    }
    override fun onBackPressed() { //minimize the app and avoid destroying the activity
        moveTaskToBack(true)
    }

    override fun onStop() {// remove event listener whenever activity is stopped as it would be re-initialized when it starts again.
        super.onStop()
        myRefRooms.child(roomID).removeEventListener(joiningRoomListener)
        myRefGameData.child(roomID).child("GS").removeEventListener(gameStateListener)
    }  // remove all the listeners before stopping the activity

    fun updateGameStateToPlay(view: View){
        if(from=="p1") {
//            myRefGameData.child(roomID).child("GameState").setValue(resources.getInteger(R.integer.transitionToGameScreen))
            myRefGameData.child("$roomID/GS").setValue(resources.getInteger(R.integer.transitionToGameScreen))

        }
        else{
            soundError.start()
//            Toast.makeText(applicationContext,"Sorry $selfName - You cannot start \n Only Host can start",Toast.LENGTH_SHORT).show()
        }
    }

    fun shareRoomInfo(view: View){
        soundUpdate.start()
        val message = "Let's play 3 of Spades(Kaali Teeri) online. Join room with room ID $roomID"
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





