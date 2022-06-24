@file:Suppress("UNUSED_PARAMETER")

package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kaalikiteeggi.three_of_spades.databinding.ActivityStartScreenBinding

class StartScreen : AppCompatActivity() {
	private lateinit var callBackManager: CallbackManager
	private lateinit var mAuth: FirebaseAuth
	private lateinit var mGoogleSignInClient: GoogleSignInClient
	private var googleCode = 882
	private var refUsersData = Firebase.firestore.collection("Users")
	private var newUser: Boolean = false
	private lateinit var userPhotoUrl: String
	private var update = false
	private lateinit var intentBuilder: CustomTabsIntent.Builder
	private val privacyPolicyUrl = "https://sites.google.com/view/kaali-ki-teeggi/privacy-policy"

	private lateinit var resultLauncher: ActivityResultLauncher<Intent>
	private lateinit var binding: ActivityStartScreenBinding
	@SuppressLint("ShowToast", "SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		CaocConfig.Builder.create()
			.backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
			.enabled(true) //default: true
			.showErrorDetails(true) //default: true
			.showRestartButton(true) //default: true
			.logErrorOnRestart(false) //default: true
			.trackActivities(false) //default: false
			.errorDrawable(R.drawable.bug_icon) //default: bug image
			.apply()
		binding = ActivityStartScreenBinding.inflate(layoutInflater)
		setContentView(binding.root)

		binding.icon3startscreen.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_scale_infinite_zoom))
		binding.vcStart.text = "Ver: " + packageManager.getPackageInfo(packageName, 0).versionName
		SoundManager.instance?.playUpdateSound() //soundUpdate.start()
		mAuth = FirebaseAuth.getInstance() // endregion

		// region Facebook Login
		binding.facebookLoginButton.setOnClickListener {
			it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
			SoundManager.instance?.playUpdateSound() //soundUpdate.start()
			binding.maskButtons.visibility = View.VISIBLE
			binding.loadingText3.text = getString(R.string.wait)
			binding.loadingTextSC.visibility = View.VISIBLE
		}
		callBackManager = CallbackManager.Factory.create()
		binding.facebookLoginButton.setPermissions("email", "public_profile")
		binding.facebookLoginButton.registerCallback(callBackManager, object : FacebookCallback<LoginResult> {
			override fun onCancel() {
				SoundManager.instance?.playErrorSound() //soundError.start()
				toastCenter("Facebook login cancelled")
				binding.maskButtons.visibility = View.GONE
			}
			override fun onError(error: FacebookException) {
				SoundManager.instance?.playErrorSound() //soundError.start()
				toastCenter("Facebook login Error : ${error.message}")
				binding.maskButtons.visibility = View.GONE
				}
			override fun onSuccess(result: LoginResult) {
				val credentialFacebook = FacebookAuthProvider.getCredential(result.accessToken.token)
				signInWithCredential(credentialFacebook, "facebook")
			}
		}) // endregion

		// region Google Sign In

		val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestIdToken(getString(R.string.default_web_client_id))
			.requestEmail()
			.build()

		resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->

			if (result.resultCode == Activity.RESULT_OK) {
				binding.maskButtons.visibility = View.VISIBLE
			} else {
				binding.maskButtons.visibility = View.GONE
			}
			if (result.resultCode == Activity.RESULT_OK) {
				val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
				if (account != null) {
					val credentialGoogle = GoogleAuthProvider.getCredential(account.idToken, null)
					signInWithCredential(credentialGoogle, "google")
				}
			}
		}

		mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
		binding.googleSignInButton.setOnClickListener {
			it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_press))
			SoundManager.instance?.playUpdateSound()
			resultLauncher.launch(mGoogleSignInClient.signInIntent)
			binding.maskButtons.visibility = View.VISIBLE
			binding.loadingText3.text = getString(R.string.wait)
			binding.loadingTextSC.visibility = View.VISIBLE
		} //endregion
	}

	private fun signInWithCredential(credential: AuthCredential, provider: String) {
		mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
			if (task.isSuccessful) {
				newUser = task.result?.additionalUserInfo?.isNewUser!!
				updateUI(provider)
			} else {
				binding.maskButtons.visibility = View.GONE
			}
		}.addOnFailureListener(this) { exception ->
			binding.maskButtons.visibility = View.GONE
			try {
				if ((exception as FirebaseAuthUserCollisionException).errorCode == "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") {
					SoundManager.instance?.playErrorSound() //soundError.start()
					toastCenter("Account already exists for email" + "\n \n${exception.email} \n \nPlease Sign in with Google or different email")
					LoginManager.getInstance().logOut()
				} else {
					SoundManager.instance?.playErrorSound() //soundError.start()
					toastCenter("${exception.message} \n PLease try again or use other method")
				}
			} catch (error: Exception) {
				SoundManager.instance?.playErrorSound() //soundError.start()
				toastCenter("${exception.message} \n PLease try again or use other method")
			}

		}
	}

	private fun updateUI(provider: String) {
		if (newUser) binding.loadingText3.text = getString(R.string.new_player)
		else binding.loadingText3.text = getString(R.string.fetching_player)

		val user = mAuth.currentUser
		if (user != null) {
			val userGivenName = user.displayName!!
			val uid = user.uid
			val email = if(user.email != null) user.email else "na"
			userPhotoUrl = user.photoUrl!!.toString()
			if (newUser && provider == "facebook") {
				userPhotoUrl = "$userPhotoUrl?width=1000&height=1000&return_ssl_resources=1"
				update = true
			} else if (newUser && provider == "twitter") {
				userPhotoUrl = userPhotoUrl.split("_")[0] + "_" + userPhotoUrl.split("_")[1] + ".jpeg"
				update = true
			} else if (provider == "google" && userPhotoUrl.contains("s96-c")) {
				userPhotoUrl = userPhotoUrl.replace("s96-c", "s800-c")
				update = true
			}

//			if (update) {
//				val profileUpdates = UserProfileChangeRequest.Builder()
//					.setPhotoUri(Uri.parse(userPhotoUrl)).build()
//				user.updateProfile(profileUpdates).addOnSuccessListener {}
//			}
			try {
				refUsersData.document(uid).get().addOnSuccessListener { documentSnapshot ->
					if (documentSnapshot.data != null) {
						refUsersData.document(uid).set(hashMapOf("n" to userGivenName, "ph" to userPhotoUrl, "e" to email, "uid" to uid), SetOptions.merge())
							.addOnSuccessListener { startNextActivity(userGivenName) }
							.addOnFailureListener { exception ->
								binding.maskButtons.visibility = View.GONE
								toastCenter("${exception.message} \n Failed to merge with existing player data\n" + "Please try again")
							}
					} else {
						refUsersData.document(uid).set(CreateUser(username = userGivenName, userPhotoUrl = userPhotoUrl, email = email!!, uid = uid).data)
							.addOnSuccessListener { startNextActivity(userGivenName) }
							.addOnFailureListener { exception ->
								binding.maskButtons.visibility = View.GONE
								toastCenter("${exception.message} \n Failed to create new player \nPlease try again")
							}
					}
				}
			} catch (me: Exception) {
				binding.maskButtons.visibility = View.GONE
				toastCenter(me.message.toString())
			}
		} else {
			binding.maskButtons.visibility = View.GONE
			toastCenter("Failed to get User details \nPlease try again")
		}
	}

	private fun startNextActivity(userGivenName: String?) {
		SoundManager.instance?.playSuccessSound() //soundSuccess.start()
		binding.loadingText3.visibility = View.GONE
		binding.loadingTextSC.visibility = View.GONE
		binding.signInSuccess.visibility = View.VISIBLE
		binding.signInSuccess.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left_activity))
		startActivity(Intent(this, MainHomeScreen::class.java).apply { putExtra("newUser", newUser) }
			.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
		overridePendingTransition(R.anim.slide_left_activity, R.anim.slide_left_activity)
		finishAndRemoveTask()
	}

	private fun toastCenter(message: String) {
		Snackbar.make(binding.startRoot, message, Snackbar.LENGTH_LONG).setAction("Dismiss") {}
			.setActionTextColor(getColor(R.color.borderblue)).show()
	}

	fun openPrivacyPolicy(view: View) {
		try {
			val params = CustomTabColorSchemeParams.Builder()
				.setToolbarColor(ContextCompat.getColor(this, R.color.icon_yellow)).build()
			intentBuilder = CustomTabsIntent.Builder()
			intentBuilder.setStartAnimations(this, R.anim.slide_left_activity, R.anim.slide_left_activity)
			intentBuilder.setExitAnimations(this, R.anim.slide_right_activity, R.anim.slide_right_activity)
			intentBuilder.setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, params)
			intentBuilder.setShareState(CustomTabsIntent.SHARE_STATE_ON)
			intentBuilder.build().launchUrl(this, Uri.parse(privacyPolicyUrl))
		} catch (me: Exception) {
			try {
				val intent = Intent(Intent.ACTION_VIEW).apply {
					data = Uri.parse(privacyPolicyUrl)
				}
				startActivity(intent)
			} catch (_: Exception) {
			}
		}
	}
}




