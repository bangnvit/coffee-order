package com.bangnv.cafeorder.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bangnv.cafeorder.R;
import com.bangnv.cafeorder.activity.MainActivity;
import com.bangnv.cafeorder.database.AppApi;
import com.bangnv.cafeorder.model.java.Response;
import com.bangnv.cafeorder.model.java.RetrofitClients;
import com.bangnv.cafeorder.model.request.FcmToken;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;

public class CustomerFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String newToken) {
        super.onNewToken(newToken);
        Log.e("onNewToken", "newToken: " + newToken );

        SharedPreferences prefs = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE);
        String apiToken = prefs.getString("token", "No token");

        updateToServerTokeFCM(apiToken);
    }

    private void updateToServerTokeFCM(String apiToken){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("TAG", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String fcmToken = task.getResult();

                    // Log and toast
                    Toast.makeText(this, fcmToken, Toast.LENGTH_SHORT).show();
                    FcmToken fcm = new FcmToken(fcmToken);


                    @SuppressLint("WrongConstant") SharedPreferences.Editor editor_FCM = getSharedPreferences("MY_PREFS_TOKEN_FCM", MODE_PRIVATE).edit();
                    editor_FCM.putString("TOKEN_FCM", fcmToken);
                    editor_FCM.apply();


                    AppApi appApi = RetrofitClients.getInstance().create(AppApi.class);

                    appApi.postFcmToken(apiToken, fcm).enqueue(new Callback<Response<FcmToken>>() {
                        @Override
                        public void onResponse(Call<Response<FcmToken>> call, retrofit2.Response<Response<FcmToken>> response) {
//                                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(Call<com.bangnv.cafeorder.model.java.Response<FcmToken>> call, Throwable t) {

                        }
                    });
                });
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d("FirebaseMessaging", "Message Notification Title: " + title);
            Log.d("FirebaseMessaging", "Message Notification Body: " + body);
            sendNotification(title, body);
        }
    }

    private void sendNotification(String messageTitle, String messageBody) {
//        Intent intent = new Intent(this, MainActivity.class);
////        Intent intent = BackableActivity.newInstanceNotification(getApplicationContext(), BackableActivity.NAVIGATOR_FNTF);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0/* Request code */, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
//
//        String channelId = getString(R.string.default_notification_channel_id);
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this, channelId)
//                        .setSmallIcon(R.drawable.ic_launcher_background)
//                        .setContentTitle(messageTitle)
//                        .setContentText(messageBody)
//                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
//                        .setContentIntent(pendingIntent)
//                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background))
//                        .setColor(getResources().getColor(R.color.colorAccent))
//                        .setLights(Color.RED, 1000, 300);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // Since android Oreo notification channel is needed.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(channelId,
//                    "Channel human readable title",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

    }
}

