@file:Suppress("UNUSED_PARAMETER")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.*
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import java.util.*


class StartScreen : AppCompatActivity() {
    private lateinit var soundUpdate: MediaPlayer
    private lateinit var soundError: MediaPlayer
    private lateinit var soundSuccess: MediaPlayer
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var toast: Toast
    private lateinit var callBackManager: CallbackManager
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var googleCode = 882
    private var refUsersData = Firebase.firestore.collection("Users")
    private var newUser:Boolean = false
    private lateinit var userPhotoUrl:String
    private var update = false

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CaocConfig.Builder.create().backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(false) //default: true
            .trackActivities(false) //default: false
            .errorDrawable(R.drawable._s_icon_bug) //default: bug image
            .apply()
        val mTwitterAuthConfig = TwitterAuthConfig(
            getString(R.string.twitter_consumer_key),
            getString(R.string.twitter_consumer_secret))
        val twitterConfig = TwitterConfig.Builder(applicationContext).twitterAuthConfig(mTwitterAuthConfig).build()
        Twitter.initialize(twitterConfig)
        setContentView(R.layout.activity_start_screen)
        findViewById<ImageView>(R.id.icon_3startscreen).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite_zoom))
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        soundError = MediaPlayer.create(applicationContext,R.raw.error_entry)
        soundUpdate = MediaPlayer.create(applicationContext,R.raw.player_moved)
        soundSuccess = MediaPlayer.create(applicationContext,R.raw.player_success_chime)
        toast = Toast.makeText(applicationContext,"",Toast.LENGTH_LONG)
        toast.setGravity(Gravity.BOTTOM,0,100)
        toast.view.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.Black))
        toast.view.findViewById<TextView>(android.R.id.message).setTextColor(ContextCompat.getColor(applicationContext,R.color.cardsBackgroundLight))
        toast.view.findViewById<TextView>(android.R.id.message).textSize = 16F
        soundUpdate.start()
        mAuth = FirebaseAuth.getInstance()
        //region Twitter Login
        findViewById<TwitterLoginButton>(R.id.twitterLoginButton).setOnClickListener{
            findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.VISIBLE
            findViewById<TextView>(R.id.loadingText3).text = getString(R.string.signIN)
        }
        findViewById<TwitterLoginButton>(R.id.twitterLoginButton).callback = object:
            Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
                if (result != null) {
                    val credentialTwitter = TwitterAuthProvider.getCredential(result.data.authToken.token, result.data.authToken.secret)
                    signInWithCredential(credentialTwitter,"twitter")
                }
            }
            override fun failure(exception: TwitterException?) {
                if (exception != null) {
                    soundError.start()
                    toastCenter("Twitter login failed \n ${exception.message}")
                }
            }
        }
        // endregion
        // region Facebook Login
        val loginButton = findViewById<LoginButton>(R.id.facebookLoginButton)
        loginButton.setOnClickListener {
            findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.VISIBLE
            findViewById<TextView>(R.id.loadingText3).text = getString(R.string.signIN)
        }
        callBackManager = CallbackManager.Factory.create()
        loginButton.setPermissions("email","public_profile")
        loginButton.registerCallback(callBackManager, object: FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                if (result != null) {
                    val credentialFacebook = FacebookAuthProvider.getCredential(result.accessToken.token)
                    signInWithCredential(credentialFacebook, "facebook")
                }
            }
            override fun onCancel() {
                soundError.start()
                toastCenter("Facebook login cancelled")
                findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.GONE
            }
            override fun onError(error: FacebookException?) {
                soundError.start()
                if (error != null) {
                    toastCenter("Facebook login Error : ${error.message}")
                }
                findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.GONE
            }
        })
        // endregion
        // region Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
//            .requestScopes(Games.SCOPE_GAMES_LITE)
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)
        findViewById<SignInButton>(R.id.googleSignInButton).setOnClickListener(View.OnClickListener {
            startActivityForResult(mGoogleSignInClient.signInIntent, googleCode)
            findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.VISIBLE
            findViewById<TextView>(R.id.loadingText3).text = getString(R.string.signIN)
        })
        //endregion
        initializeSpeechEngine()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.VISIBLE
        }else{
            findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.GONE
        }
        if(requestCode == googleCode && resultCode == Activity.RESULT_OK){
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            if(account!= null){
                val credentialGoogle = GoogleAuthProvider.getCredential(account.idToken,null)
                signInWithCredential(credentialGoogle, "google")
            }
        }
        findViewById<TwitterLoginButton>(R.id.twitterLoginButton).onActivityResult(requestCode, resultCode, data)
        callBackManager.onActivityResult(requestCode, resultCode, data)
    }
    private fun signInWithCredential(credential: AuthCredential, provider: String){
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                newUser = task.result?.additionalUserInfo?.isNewUser!!
                updateUI(provider)
            }else{
                findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.GONE
            }
        }.addOnFailureListener(this) {
                exception ->
            findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.GONE
            if( (exception as FirebaseAuthUserCollisionException).errorCode == "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL"){
                soundError.start()
                toastCenter("Account already exists for email" + "\n \n${exception.email} \n \nPlease Sign in with Google or different email")
                LoginManager.getInstance().logOut()
            }else{
                soundError.start()
                toastCenter("${exception.message} \n \n PLease try again or use other method")
            }
        }
    }
    private fun updateUI(provider: String) {
        if(newUser) findViewById<TextView>(R.id.loadingText3).text = "Adding New User"
        else findViewById<TextView>(R.id.loadingText3).text = getString(R.string.fetching_player)

        val user = mAuth.currentUser
        if(user != null){
            val userGivenName = user.displayName
            val uid = user.uid
            userPhotoUrl = user.photoUrl.toString()
            if(newUser && provider=="facebook" ) {
                userPhotoUrl =  "$userPhotoUrl?width=1000&height=1000&return_ssl_resources=1"
                update = true
            }else if (newUser && provider=="twitter" ){
                userPhotoUrl = userPhotoUrl.split("_")[0] + "_" + userPhotoUrl.split("_")[1] + ".jpeg"
                update = true
            }else if(provider=="google" && userPhotoUrl.contains("s96-c")) {
                userPhotoUrl = userPhotoUrl.replace("s96-c","s800-c")
                update = true
            }

            if(update){
                val profileUpdates =
                    UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(userPhotoUrl)).build()
                user.updateProfile(profileUpdates)
                    .addOnSuccessListener {
                    }
            }
           try{
               refUsersData.document(uid).get().addOnSuccessListener {
                       documentSnapshot -> if(documentSnapshot.data != null) {
                   refUsersData.document(uid).set(hashMapOf("n" to userGivenName, "ph" to userPhotoUrl),SetOptions.merge() )
                       .addOnSuccessListener {   startNextActivity(userGivenName)   }
                       .addOnFailureListener{exception ->
                           findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.GONE
                           toastCenter("${exception.message} \n Failed to merge with existing player data\n" +
                                   "Please try again") }
               }
               else {
                   refUsersData.document(uid).set(CreateUser(userGivenName.toString(), userPhotoUrl).data)
                       .addOnSuccessListener {startNextActivity(userGivenName) }
                       .addOnFailureListener{exception ->
                           findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.GONE
                           toastCenter("${exception.message} \n Failed to create new player \nPlease try again") }
               }}
           }catch (me: Exception){
               findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.GONE
               toastCenter(me.message.toString())
           }
        }
        else{
            findViewById<RelativeLayout>(R.id.maskButtons).visibility = View.GONE
            toastCenter("Failed to get User details \nPlease try again")
        }
    }

    private fun startNextActivity(userGivenName: String?){
        soundSuccess.start()
        speak("Hello  ${userGivenName.toString().split(" ")[0]}")
        findViewById<ProgressBar>(R.id.progressBarLoading2).visibility = View.GONE
        findViewById<TextView>(R.id.loadingText3).visibility = View.GONE
        findViewById<AppCompatButton>(R.id.signInSuccess).visibility = View.VISIBLE
        findViewById<AppCompatButton>(R.id.signInSuccess).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.slide_left_activity))
        Handler().postDelayed({ startActivity(Intent(this, MainHomeScreen::class.java).apply {putExtra("newUser",newUser)})
            overridePendingTransition(R.anim.slide_left_activity,R.anim.slide_left_activity)},500)
//                           toastCenter("Signed in Successfully ${String(Character.toChars(0x1F60A))}")

        Handler().postDelayed({finish()},1500)
    }
    fun developerCredits(view: View){
//        soundUpdate.start()
//        startActivity( Intent(this,DeveloperCredits::class.java).apply { putExtra("from",true) })
         }
    private fun toastCenter(message: String){
        toast.setText(message)
        toast.show()

    }
    private fun speak(speechText:String, pitch:Float = 0.9f, speed:Float = 1f) {
            textToSpeech.setPitch(pitch)
            textToSpeech.setSpeechRate(speed)
            textToSpeech.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null)

    }
    private fun initializeSpeechEngine(){
        textToSpeech = TextToSpeech(applicationContext,
            TextToSpeech.OnInitListener { status ->
                if(status == TextToSpeech.SUCCESS) {
                    val result = textToSpeech.setLanguage(Locale.ENGLISH)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        toastCenter("Missing Language data - Text to speech")
                    }
                }
            })
    }

    override fun onStop() {
        toast.cancel()
        super.onStop()
    }
    override fun onDestroy() {
        toast.cancel()
        try{
            textToSpeech.shutdown()
        }catch(me:java.lang.Exception) {}
        super.onDestroy()

    }
}




