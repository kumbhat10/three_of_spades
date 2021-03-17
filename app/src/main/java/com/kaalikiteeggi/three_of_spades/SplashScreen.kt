package com.kaalikiteeggi.three_of_spades

//import com.adcolony.sdk.AdColony
//import com.adcolony.sdk.AdColonyAppOptions
//import com.facebook.ads.AdSettings
//import com.google.ads.mediation.adcolony.AdColonyMediationAdapter
//import com.google.ads.mediation.inmobi.InMobiConsent
//import com.inmobi.sdk.InMobiSdk
//import com.unity3d.ads.UnityAds
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bitvale.lightprogress.LightProgress
import com.facebook.login.LoginManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_splash_screen.*
import java.util.*

class SplashScreen : AppCompatActivity() {
	private lateinit var soundInto: MediaPlayer
	private var user: FirebaseUser? = null
	private lateinit var appUpdateManager: AppUpdateManager
	private lateinit var toast: Toast
	private var handler = Handler(Looper.getMainLooper())
	private var isAppLatest = true
	private var isTimerOver = false
	private var isNextActivityStarted = false
	private var background = 4
	private val timer = if (!BuildConfig.DEBUG) 4500L
	else 4500L
	private val requestCodeAppUpdate = 800

	override fun onCreate(savedInstanceState: Bundle?) {
		setTheme(R.style.Theme_App)
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_splash_screen)
		requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
		background = 4//Random.nextInt(0, 6)
//		when (background) {
//            0 -> splashBckgd.setImageResource(R.drawable.redblackburst)
//            1 -> splashBckgd.setImageResource(R.drawable.blueburst)
//            2 -> splashBckgd.setImageResource(R.drawable.greenyellowburst)
//            3 -> splashBckgd.setImageResource(R.drawable.navyblueburst)
//            4 -> splashBckgd.setImageResource(R.drawable.redorangeburst)
//            5 -> splashBckgd.setImageResource(R.drawable.yellowburst)
//		}
//		findViewById<LightProgress>(R.id.lightSplash).on()
		icon3.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.zoomout_once))
		splashBckgd.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.clockwise))
		updateUI()
		toast = Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT)

		Handler(Looper.getMainLooper()).post {	mobileAds() // load mobile ads for everyone
			checkAppUpdate()
			soundInto = MediaPlayer.create(applicationContext, R.raw.card_shuffle)
			soundInto.setVolume(0.1F, 0.1F)
			soundInto.start()
		}
		handler.postDelayed({
            isTimerOver = true
            if (isAppLatest) nextActivity()
        }, timer) // dummy 3500
	}
	@SuppressLint("SetTextI18n")
	private fun updateUI() {
		vcSplash.text = "Ver: " + packageManager.getPackageInfo(packageName, 0).versionName
		val mAuth = FirebaseAuth.getInstance()  // dummy - could be a problem if invalid mAuth and not null
		user = mAuth.currentUser
		if (user != null) {
			val userName = user!!.displayName.toString().split(" ")[0]
			val photoURL = user!!.photoUrl.toString()
			userNameSplash.text = userName
			userNameSplash.visibility = View.VISIBLE
			welcome2.visibility = View.VISIBLE
			profilePic2.visibility = View.VISIBLE
			Picasso.get().load(photoURL).resize(300, 300).into(profilePic2)
		}
	}

	private fun nextActivity() {
		if (user != null && !isNextActivityStarted) {
			isNextActivityStarted = true
			handler.removeCallbacksAndMessages(null)
			startActivity(Intent(this, MainHomeScreen::class.java).apply { putExtra("newUser123", false) })
			overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
			Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1000)
		} else if (!isNextActivityStarted) {
			isNextActivityStarted = true
			LoginManager.getInstance().logOut()
			startActivity(Intent(applicationContext, StartScreen::class.java).apply { putExtra("background", background) })
			overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
			finish()
		}
	}

	@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
	private fun mobileAds() {
		MobileAds.initialize(this)
		val testDeviceIds = Arrays.asList(getString(R.string.testDeviceId))
		val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
		MobileAds.setRequestConfiguration(configuration)
	}

	private fun checkAppUpdate() {
		appUpdateManager = AppUpdateManagerFactory.create(baseContext)
		val appUpdateInfoTask = appUpdateManager.appUpdateInfo
		appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
			if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
				handler.removeCallbacksAndMessages(null)
				toastCenter("New App version is available")
				appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, requestCodeAppUpdate)
			} else {
				isAppLatest = true
				if (isTimerOver && !isNextActivityStarted) nextActivity()
			}
		}.addOnFailureListener { e ->
			toastCenter("App update check error \n${e.message.toString()}")
			isAppLatest = true
			if (isTimerOver && !isNextActivityStarted) nextActivity()
		}.addOnCompleteListener {
			isAppLatest = true
			if (isTimerOver && !isNextActivityStarted) nextActivity()
		}
	}

	private fun toastCenter(message: String) {
		toast.setText(message)
		toast.show()
	}

	override fun onResume() {
		super.onResume()
		appUpdateManager = AppUpdateManagerFactory.create(baseContext)
		appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
			if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
				appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, requestCodeAppUpdate)
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == requestCodeAppUpdate) {
			if (resultCode != Activity.RESULT_OK) {
				checkAppUpdate()
				toastCenter("App update failed \n Please try again or Restart app")
			} else if (resultCode == Activity.RESULT_OK) {
				toastCenter("App was updated successfully") // dummy
				isAppLatest = true
				if (isTimerOver && !isNextActivityStarted) nextActivity()
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}
}
