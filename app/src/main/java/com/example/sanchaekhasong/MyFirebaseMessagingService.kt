package com.example.sanchaekhasong

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMsgService"
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
    }




    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 메시지 수신 시 호출
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val point = remoteMessage.data["point"]
        showNotification(title, body, point)
    }

    private fun showNotification(title: String?, message: String?, point: String?) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, "default")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_appimg)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (title == "주간 단과대 랭킹 알림") {
            notificationManager.notify(0, notificationBuilder.build())
        }
        else if (title == "주간 개인 랭킹 알림") {
            notificationManager.notify(1, notificationBuilder.build())
        }
    }
}
