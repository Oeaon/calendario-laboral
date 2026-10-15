package com.calendariolaboral.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calendariolaboral.app.data.AppSettings
import com.calendariolaboral.app.ui.CalendarioRoot
import com.calendariolaboral.app.ui.theme.CalendarioTheme

class MainActivity : ComponentActivity() {
    private val app by lazy { application as CalendarioLaboralApp }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        app.monetization.gatherConsent(this)

        setContent {
            CalendarioTheme {
                val settings by app.settings.settings.collectAsStateWithLifecycle(initialValue = null as AppSettings?)
                val monetizationState by app.monetization.state.collectAsStateWithLifecycle()
                CalendarioRoot(settings, app.settings, app.monetization, monetizationState)
            }
        }
    }
}
