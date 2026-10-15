package com.calendariolaboral.app

import android.app.Application
import com.calendariolaboral.app.data.SettingsRepository
import com.calendariolaboral.app.data.settingsDataStore
import com.calendariolaboral.app.monetization.Monetization

class CalendarioLaboralApp : Application() {
    lateinit var monetization: Monetization
        private set
    lateinit var settings: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        monetization = Monetization(this)
        settings = SettingsRepository(settingsDataStore)
    }
}
