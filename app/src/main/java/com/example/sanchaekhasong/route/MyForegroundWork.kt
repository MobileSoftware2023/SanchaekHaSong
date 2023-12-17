package com.example.sanchaekhasong.route

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.sanchaekhasong.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class MyForegroundWork(
    private val context : Context,
    workerParams : WorkerParameters
): Worker(context, workerParams) {
    var progress = "Starting work . . ."
    var NOTIFICATIONN_ID = 1
    private var locationRequest : LocationRequest? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        var isStopped = false
    }
    init {
        Companion.isStopped = false
    }

    private var destinationReached = false

    override fun doWork(): Result {
        Log.d("TAG", "1")
        //setForegroundAsync(showNotification(progress))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                if (destinationReached) return
                for (location in locationResult.locations){

                    val currentLatitude = location.latitude
                    val currentLongitude = location.longitude

                    val targetLatitude = 37.5464846
                    val targetLongitude = 126.9671207
                    Log.d("esp", "도착 지점 반경 밖")

                    if (isLocationWithinThreshold(currentLatitude, currentLongitude, targetLatitude, targetLongitude)) {
                        Log.d("esp", "도착 지점 반경 안")
                        sendArrivalNotification()
                        // 필요에 따라 위치 업데이트를 중단합니다.
                        stopLocationUpdates()
                        stopForegroundWork()
                        destinationReached = true
                        break
                    }

                    val location =
                        "Latitude:"+location.latitude + "\n"+"Longitude: "+location.longitude
                    updateNotification(location)
                }
            }
        }
        startLocationUpdates()
        return Result.success()
    }

    private fun stopForegroundWork() {
        // 백그라운드 워크를 완전히 종료합니다.
        Log.d("TAG", "Stopping foreground work...")
        Companion.isStopped = true
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun isLocationWithinThreshold(currentLatitude: Double, currentLongitude: Double, targetLatitude: Double, targetLongitude: Double): Boolean {
        // Define a threshold for proximity (you can adjust this value)
        val threshold = 0.0003

        // Check if the current location is within the threshold of the target location
        val latitudeDifference = Math.abs(currentLatitude - targetLatitude)
        val longitudeDifference = Math.abs(currentLongitude - targetLongitude)

        return latitudeDifference < threshold && longitudeDifference < threshold
    }

    private fun sendArrivalNotification() {
        Log.d("esp", "도착 알람")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "100")
            .setContentTitle("도착 알림")
            .setContentText("목표 지점에 도착했습니다.")
            .setSmallIcon(R.mipmap.ic_appimg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun stopLocationUpdates() {
        // Stop location updates when the destination is reached
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    private fun showNotification(progress:String): ForegroundInfo {
        return ForegroundInfo(NOTIFICATIONN_ID, createNotification(progress))
    }

    private fun createNotification(progress:String):Notification {
        val CHANNEL_ID = "100"
        val title = "Foreground Work"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID, title,
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.mipmap.ic_appimg)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(progress: String){
        val notification = createNotification(progress)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATIONN_ID, notification)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).setMinUpdateIntervalMillis(5000).build()

        for (i in 0..99) {
            if (MyForegroundWork.isStopped) {
                break
            }
            try {
                Thread.sleep(5000)
                locationRequest?.let {
                    fusedLocationClient.requestLocationUpdates(
                        it,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}