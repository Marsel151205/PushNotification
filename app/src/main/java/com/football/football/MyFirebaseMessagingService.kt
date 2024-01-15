package com.football.football

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private fun isNotificationPermissionGranted(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("kirill", "From: ${remoteMessage.from}")

        if (isNotificationPermissionGranted()) {
            remoteMessage.data.isNotEmpty().let {
                Log.d("kirill", "Message data payload: ${remoteMessage.data}")
            }

            remoteMessage.notification?.let {
                Log.d("kirill", "Message Notification Body: ${it.body}")
            }
            Handler(Looper.getMainLooper()).post {
                sendNotification(remoteMessage.notification?.body ?: "")
            }
        } else {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, "Notification denied ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("FCM Message")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        subscribeToNewsTopic()
    }

    private fun subscribeToNewsTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("news")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Successfully subscribed to topic 'news'")
                } else {
                    Log.e(TAG, "Failed to subscribe to topic 'news'", task.exception)
                }
            }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}