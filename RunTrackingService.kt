package com.devansh.stride

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import com.google.android.gms.location.*

class RunTrackingService : Service() {
    private lateinit var fused: FusedLocationProviderClient
    private var previous: Location? = null
    private var startedAt = 0L
    private var ticker: Runnable? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        fused = LocationServices.getFusedLocationProviderClient(this)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> begin()
            ACTION_STOP -> finishRun()
        }
        return START_NOT_STICKY
    }

    private fun begin() {
        val notification = buildNotification("Run in progress • 0.00 km")
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else startForeground(NOTIFICATION_ID, notification)

        startedAt = SystemClock.elapsedRealtime()
        elapsedSeconds = 0
        distanceMeters = 0.0
        previous = null

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateDistanceMeters(3f)
            .build()

        try {
            fused.requestLocationUpdates(request, callback, mainLooper)
        } catch (_: SecurityException) {
            stopSelf()
            return
        }

        ticker = object : Runnable {
            override fun run() {
                elapsedSeconds = (SystemClock.elapsedRealtime() - startedAt) / 1000
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(NOTIFICATION_ID, buildNotification("Run in progress • %.2f km".format(distanceMeters / 1000)))
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(ticker!!)
    }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                if (location.accuracy > 30f) continue
                val old = previous
                if (old != null) {
                    val segment = old.distanceTo(location)
                    // Reject obvious GPS jumps; retain only plausible movement.
                    if (segment in 1.5f..40f) distanceMeters += segment
                }
                previous = location
            }
        }
    }

    private fun finishRun() {
        try { fused.removeLocationUpdates(callback) } catch (_: Exception) {}
        ticker?.let { handler.removeCallbacks(it) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(text: String): Notification =
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Stride")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Stride").setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation).build()
        }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL_ID, "Active run", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        try { fused.removeLocationUpdates(callback) } catch (_: Exception) {}
        ticker?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.devansh.stride.START"
        const val ACTION_STOP = "com.devansh.stride.STOP"
        private const val CHANNEL_ID = "stride_run"
        private const val NOTIFICATION_ID = 4102
        @Volatile var distanceMeters: Double = 0.0
        @Volatile var elapsedSeconds: Long = 0L
    }
}
