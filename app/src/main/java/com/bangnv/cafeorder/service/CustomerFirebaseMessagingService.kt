package com.bangnv.cafeorder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.OrderHistoryActivity
import com.bangnv.cafeorder.activity.OrderHistoryDetailActivity
import com.bangnv.cafeorder.activity.SplashActivity
import com.bangnv.cafeorder.activity.admin.AdminOrderDetailActivity
import com.bangnv.cafeorder.activity.admin.AdminReportListActivity
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.model.responseapi.ChildOrderDataResponse
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

class CustomerFirebaseMessagingService : FirebaseMessagingService(){
    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        Log.e("onNewToken", "newToken: $newToken")

        val prefs: SharedPreferences = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE)
        val apiToken = prefs.getString("token", "No token")

//        updateToServerTokeFCM(apiToken)
    }

    private fun updateToServerTokeFCM(apiToken: String?) {

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            val title = remoteMessage.notification?.title
            val body = remoteMessage.notification?.body
            val dataJsonString = Gson().toJson(remoteMessage.data)
            // Parse JSON to object
            val dataObject = Gson().fromJson(dataJsonString, ChildOrderDataResponse::class.java)

            // Get the value of typeFor
            val typeFor = dataObject.typeFor

            Log.d("FirebaseMessaging", "Message Notification: Title = $title")
            Log.d("FirebaseMessaging", "Message Notification: Body = $body")
            Log.d("FirebaseMessaging", "Message Notification: data(Gson) = $dataJsonString")
            Log.d("FirebaseMessaging", "Message Notification: typeFor = $typeFor")
            Log.d("FirebaseMessaging", "Message Notification: orderId = ${dataObject.orderId}")
            Log.d("FirebaseMessaging", "Message Notification: email = ${dataObject.email}")
            sendNotification(title, body, typeFor, dataObject.orderId)
        }
    }

    private fun sendNotification(
        messageTitle: String?,
        messageBody: String?,
        typeFor: String?,
        orderId: String?
    ) {
        lateinit var intent : Intent
        when (typeFor) {
            "1" -> {
                val bundle = Bundle()
                bundle.putSerializable(Constant.KEY_INTENT_ADMIN_ORDER_OBJECT, orderId?.toLong())
                intent = Intent(this, AdminOrderDetailActivity::class.java)
                intent.putExtras(bundle)
            }
            "2" -> {
                val bundle = Bundle()
                bundle.putSerializable(Constant.KEY_INTENT_ORDER_OBJECT, orderId?.toLong())
                intent = Intent(this, OrderHistoryDetailActivity::class.java)
                intent.putExtras(bundle)
            }
            else -> intent = Intent(this, SplashActivity::class.java) // Tự vào Main đúng theo User.type
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */,
            intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_notification_large))

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}