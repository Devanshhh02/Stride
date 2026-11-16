package com.devansh.stride

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

private val Ink = Color(0xFF101713)
private val Lime = Color(0xFFB7F36B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StrideScreen() }
    }

    @Composable
    private fun StrideScreen() {
        var running by remember { mutableStateOf(false) }
        var distance by remember { mutableStateOf(0.0) }
        var seconds by remember { mutableStateOf(0L) }
        var permissionReady by remember { mutableStateOf(false) }

        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
            if (Build.VERSION.SDK_INT >= 29) add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            permissionReady = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (permissionReady) startTracking()
        }

        fun requestAndStart() {
            val missing = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missing.isEmpty()) {
                permissionReady = true
                startTracking()
            } else permissionLauncher.launch(missing.toTypedArray())
        }

        LaunchedEffect(running) {
            while (running) {
                delay(1000)
                seconds = RunTrackingService.elapsedSeconds
                distance = RunTrackingService.distanceMeters / 1000.0
            }
        }

        MaterialTheme(colorScheme = darkColorScheme(
            primary = Lime, background = Ink, surface = Color(0xFF1C2821),
            onPrimary = Ink, onBackground = Color.White, onSurface = Color.White
        )) {
            Column(
                Modifier.fillMaxSize().background(Ink).padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(28.dp))
                Text("STRIDE", style = MaterialTheme.typography.headlineMedium, color = Lime)
                Text("Your run. Your progress.", color = Color.LightGray)
                Spacer(Modifier.height(38.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2821)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text("CURRENT RUN", color = Lime, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(18.dp))
                        Text(String.format("%.2f", distance), style = MaterialTheme.typography.displayLarge)
                        Text("KILOMETRES", color = Color.LightGray)
                        Spacer(Modifier.height(22.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Metric("TIME", formatTime(seconds))
                            val pace = if (distance > 0.02) (seconds / 60.0) / distance else 0.0
                            Metric("PACE", if (pace > 0) String.format("%.1f min/km", pace) else "--")
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (!running) requestAndStart()
                        else {
                            stopTracking()
                            running = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (running) Color(0xFFEF6A63) else Lime),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(if (running) "FINISH RUN" else "START RUN", color = Ink)
                }
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { openHealthConnect() },
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) { Text("CONNECT SAMSUNG HEALTH / HEALTH CONNECT") }
                Spacer(Modifier.height(24.dp))
                Text("Run tracking continues with the screen locked while the active-run notification is shown.",
                    color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Text("Stride • Personal fitness tracker", color = Color.Gray)
            }
        }
    }

    private fun startTracking() {
        RunTrackingService.distanceMeters = 0.0
        RunTrackingService.elapsedSeconds = 0L
        val intent = Intent(this, RunTrackingService::class.java).setAction(RunTrackingService.ACTION_START)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopTracking() {
        val intent = Intent(this, RunTrackingService::class.java).setAction(RunTrackingService.ACTION_STOP)
        startService(intent)
    }

    private fun openHealthConnect() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.google.android.apps.healthdata")
            if (intent != null) startActivity(intent)
            else startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
        } catch (_: Exception) {
            startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column {
        Text(label, color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}

private fun formatTime(total: Long): String {
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
