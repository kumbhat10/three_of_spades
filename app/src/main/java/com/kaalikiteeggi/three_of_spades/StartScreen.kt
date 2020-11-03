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
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
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
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.android.synthetic.main.activity_start_screen.*
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
            .errorDrawable(R.drawable.bug_icon) //default: bug image
            .apply()
//        val mTwitterAuthConfig = TwitterAuthConfig(
//            getString(R.string.twitter_consumer_key),
//            getString(R.string.twitter_consumer_secret))
//        val twitterConfig = TwitterConfig.Builder(applicationContext).twitterAuthConfig(mTwitterAuthConfig).build()
//        Twitter.initialize(twitterConfig)
        setContentView(R.layout.activity_start_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        when (intent.getIntExtra("background", 4)) {
            0 -> startBckgd.setImageResource(R.drawable.redblackburst)
            1 -> startBckgd.setImageResource(R.drawable.blueburst)
            2 -> startBckgd.setImageResource(R.drawable.greenyellowburst)
            3 -> startBckgd.setImageResource(R.drawable.navyblueburst)
            4 -> startBckgd.setImageResource(R.drawable.redorangeburst)
            5 -> startBckgd.setImageResource(R.drawable.yellowburst)
        }
        startBckgd.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise))
        findViewById<ImageView>(R.id.icon_3startscreen).startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.anim_scale_infinite_zoom))
        vcStart.text = "Ver: " + packageManager.getPackageInfo(packageName,0).versionName
        soundError = MediaPlayer.create(applicationContext,R.raw.error)
        soundUpdate = MediaPlayer.create(applicationContext,R.raw.card_played)
        soundSuccess = MediaPlayer.create(applicationContext,R.raw.success)
        toast = Toast.makeText(applicationContext,"",Toast.LENGTH_LONG)
        toast.setGravity(Gravity.BOTTOM,0,100)
        soundUpdate.start()
        mAuth = FirebaseAuth.getInstance()
        //region Twitter Login
//        twitterLoginButton.setOnClickListener{
//            soundUpdate.start()
//            maskButtons.visibility = View.VISIBLE
//            loadingText3.text = getString(R.string.wait)
//            lightStart.visibility = View.VISIBLE
//            lightStart.on()
//        }
//        twitterLoginButton.callback = object:
//            Callback<TwitterSession>() {
//            override fun success(result: Result<TwitterSession>?) {
//                if (result != null) {
//                    val credentialTwitter = TwitterAuthProvider.getCredential(result.data.authToken.token, result.data.authToken.secret)
//                    signInWithCredential(credentialTwitter,"twitter")
//                }
//            }
//            override fun failure(exception: TwitterException?) {
//                if (exception != null) {
//                    soundError.start()
//                    toastCenter("Twitter login failed \n ${exception.message}")
//                }
//            }
//        }
        // endregion
        // region Facebook Login
        val loginButton = findViewById<LoginButton>(R.id.facebookLoginButton)
        loginButton.setOnClickListener {
            soundUpdate.start()
            maskButtons.visibility = View.VISIBLE
            loadingText3.text = getString(R.string.wait)
            lightStart.visibility = View.VISIBLE
            lightStart.on()
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
                maskButtons.visibility = View.GONE
            }
            override fun onError(error: FacebookException?) {
                soundError.start()
                if (error != null) {
                    toastCenter("Facebook login Error : ${error.message}")
                }
                maskButtons.visibility = View.GONE
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
            soundUpdate.start()
            startActivityForResult(mGoogleSignInClient.signInIntent, googleCode)
            maskButtons.visibility = View.VISIBLE
            loadingText3.text = getString(R.string.wait)
            lightStart.visibility = View.VISIBLE
            lightStart.on()
        })
        //endregion
        initializeSpeechEngine()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            maskButtons.visibility = View.VISIBLE
        }else{
            maskButtons.visibility = View.GONE
        }
        if(requestCode == googleCode && resultCode == Activity.RESULT_OK){
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            if(account!= null){
                val credentialGoogle = GoogleAuthProvider.getCredential(account.idToken,null)
                signInWithCredential(credentialGoogle, "google")
            }
        }
//        twitterLoginButton.onActivityResult(requestCode, resultCode, data)
        callBackManager.onActivityResult(requestCode, resultCode, data)
    }
    private fun signInWithCredential(credential: AuthCredential, provider: String){
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                newUser = task.result?.additionalUserInfo?.isNewUser!!
                updateUI(provider)
            }else{
                maskButtons.visibility = View.GONE
            }
        }.addOnFailureListener(this) {
                exception ->
            maskButtons.visibility = View.GONE
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
        if(newUser) loadingText3.text = getString(R.string.new_player)
        else loadingText3.text = getString(R.string.fetching_player)

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
                   val setData = if(documentSnapshot.contains("p_bot")) hashMapOf("n" to userGivenName, "ph" to userPhotoUrl)
                   else hashMapOf("n" to userGivenName, "ph" to userPhotoUrl, "b_bot" to 0,"p_bot" to 0,"w_bot" to 0)

                   refUsersData.document(uid).set(setData, SetOptions.merge() )
                       .addOnSuccessListener {   startNextActivity(userGivenName)   }
                       .addOnFailureListener{exception ->
                           maskButtons.visibility = View.GONE
                           toastCenter("${exception.message} \n Failed to merge with existing player data\n" +
                                   "Please try again") }
               }
               else {
                   refUsersData.document(uid).set(CreateUser(userGivenName.toString(), userPhotoUrl).data)
                       .addOnSuccessListener {startNextActivity(userGivenName) }
                       .addOnFailureListener{exception ->
                           maskButtons.visibility = View.GONE
                           toastCenter("${exception.message} \n Failed to create new player \nPlease try again") }
               }}
           }catch (me: Exception){
               maskButtons.visibility = View.GONE
               toastCenter(me.message.toString())
           }
        }
        else{
            maskButtons.visibility = View.GONE
            toastCenter("Failed to get User details \nPlease try again")
        }
    }

    private fun startNextActivity(userGivenName: String?){
        soundSuccess.start()
        speak("Hello  ${userGivenName.toString().split(" ")[0]}")
        loadingText3.visibility = View.GONE
        lightStart.visibility = View.GONE
        signInSuccess.visibility = View.VISIBLE
        signInSuccess.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.slide_left_activity))
        Handler(Looper.getMainLooper()).postDelayed({ startActivity(Intent(this, MainHomeScreen::class.java).apply {putExtra("newUser",newUser)})
            overridePendingTransition(R.anim.slide_left_activity,R.anim.slide_left_activity)},500)
//                           toastCenter("Signed in Successfully ${String(Character.toChars(0x1F60A))}")

        Handler(Looper.getMainLooper()).postDelayed({finish()},1500)
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
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    toastCenter("Missing Language data - Text to speech")
                }
            }
        }
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




