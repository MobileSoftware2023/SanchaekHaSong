package com.example.sanchaekhasong

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


// FirebaseInstanceIdService has been deprecated, use FirebaseMessagingService
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        saveTokenToDatabase(token)
    }

    private fun saveTokenToDatabase(token: String) {
        // Save the token to the database based on the user ID
        val userId = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
        val databaseReference = FirebaseDatabase.getInstance().getReference("${userId}/fcmToken")
        databaseReference.setValue(token)
    }

    private val TAG = "MyFirebaseMsgService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 메시지 수신 시 호출됩니다.

        // 푸시 메시지 내용을 가져옵니다.
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val point = remoteMessage.data["point"]

        // 팝업을 띄우는 등의 원하는 동작을 수행합니다.
        showNotification(title, body, point)
    }

    private fun showNotification(title: String?, message: String?, point: String?) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android Oreo 이상에서는 알림 채널을 설정해야 합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, "default")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())
        val database = FirebaseDatabase.getInstance()
        val username = FirebaseAuth.getInstance().currentUser?.email.toString().substringBeforeLast('@')
        val userData = database.getReference("$username")
        if (point != null) {
            userData.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var currentPoint = dataSnapshot.child("point").value as Long
                    currentPoint += point.toLong()!!
                    userData.child("point").setValue(currentPoint)
                    userData.removeEventListener(this)
                }
                override fun onCancelled(error: DatabaseError) {
                    val code = error.code
                    val message = error.message
                    Log.e("TAG_DB", "onCancelled by $code : $message")
                }
            })
        }

    }
}
