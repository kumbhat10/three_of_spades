package com.kaalikiteeggi.three_of_spades

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.text.SimpleDateFormat
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

	override fun onMessageReceived(remoteMessage: RemoteMessage) {

		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		Log.d(TAG, "From: ${remoteMessage.from}")
		if (remoteMessage.data.isNotEmpty()) {
			Log.d(TAG, "Message data payload: ${remoteMessage.data}")
		}
		sendNotification(remoteMessage)

		remoteMessage.notification?.let {   // Check if message contains a notification payload.
			Log.d(TAG, "Message Notification Body: ${it.body}")
		}
	}

	@SuppressLint("SimpleDateFormat")
	override fun onNewToken(p0: String) {
		super.onNewToken(p0)
		val uid = FirebaseAuth.getInstance().uid.toString()// DK -> "t6c8Bgx0OihFkTOgoAjC7o7FLE52"
		if(uid.isNotEmpty()) Firebase.firestore.collection("Users").document(uid).set(hashMapOf("zFCM_token" to p0,
			"zFCM_dt" to SimpleDateFormat("yyyyMMdd HH:mm:ss z").format(Date())), SetOptions.merge())
	}

	private fun sendNotification(remoteMessage: RemoteMessage) {
		val intent = Intent(this, MainHomeScreen::class.java)
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

		val channelId = getString(R.string.default_notification_channel_id1)
		val defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.notification)

		val notificationBuilder = NotificationCompat.Builder(this, channelId)
			.setSmallIcon(R.drawable.ic_stat_name)
			.setContentTitle(remoteMessage.notification?.title)
			.setContentText(remoteMessage.notification?.body)
			.setAutoCancel(true)
			.setSound(defaultSoundUri)
			.setContentIntent(pendingIntent)
			.setStyle(NotificationCompat.BigPictureStyle().bigPicture(ContextCompat.getDrawable(applicationContext, R.drawable.bluesquarebutton)
				?.toBitmap()))

		val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		// Since android Oreo notification channel is needed.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(channelId, "Daily Notification", NotificationManager.IMPORTANCE_DEFAULT)
			val audioAttributes =  AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
			channel.setSound(defaultSoundUri, audioAttributes)
			channel.setShowBadge(true)
			notificationManager.createNotificationChannel(channel)
		}

		notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
	}

	companion object {
		private const val TAG = "MyFirebaseMsgService"
	}
}
