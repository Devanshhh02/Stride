package com.devansh.stride

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

/**
 * Health Connect displays this screen as the app's privacy rationale.
 * Health data is accessed only after the user grants Health Connect permissions.
 */
class HealthPermissionsRationaleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "Stride uses Health Connect to read supported activity data and, in a future sync step, save completed running sessions. You control access in Health Connect settings. GPS route tracking is collected by Stride only while a run is active."
            textSize = 18f
            setPadding(40, 40, 40, 40)
        })
    }
}
