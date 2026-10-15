package com.calendariolaboral.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.calendariolaboral.app.R
import com.calendariolaboral.app.data.AppSettings
import com.calendariolaboral.app.data.SettingsRepository
import com.calendariolaboral.app.monetization.AdBanner
import com.calendariolaboral.app.monetization.Monetization
import com.calendariolaboral.app.monetization.MonetizationState
import com.calendariolaboral.app.monetization.PremiumAction
import com.calendariolaboral.app.monetization.PremiumScreen
import com.calendariolaboral.core.HolidayCalendar
import com.calendariolaboral.core.Region
import com.calendariolaboral.core.WorkCalendar
import kotlinx.coroutines.launch
import java.time.LocalDate

private enum class Screen { MAIN, REGION, SETTINGS, PREMIUM }

enum class CalendarTab(val label: Int) {
    CALENDAR(R.string.tab_calendar),
    BRIDGES(R.string.tab_bridges),
    WORKING_DAYS(R.string.tab_working_days),
    VACATIONS(R.string.tab_vacations),
}

/** Calendario de tres años (anterior, actual y siguiente) para que los cálculos crucen el cambio de año. */
fun workCalendarFor(region: Region, localHolidays: Set<java.time.LocalDate>, year: Int): WorkCalendar =
    WorkCalendar((year - 1..year + 1).flatMap { HolidayCalendar.holidays(it, region, localHolidays) })

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioRoot(
    settings: AppSettings?,
    settingsRepository: SettingsRepository,
    monetization: Monetization,
    monetizationState: MonetizationState,
) {
    if (settings == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf(Screen.MAIN) }
    var tab by rememberSaveable { mutableStateOf(CalendarTab.CALENDAR) }
    val region = settings.region

    if (region == null || screen == Screen.REGION) {
        if (region != null) BackHandler { screen = Screen.MAIN }
        RegionPicker(selected = region) { picked ->
            scope.launch { settingsRepository.setRegion(picked) }
            screen = Screen.MAIN
        }
        return
    }

    val today = LocalDate.now()
    val calendar = remember(region, settings.localHolidays, today.year) {
        workCalendarFor(region, settings.localHolidays, today.year)
    }

    when (screen) {
        Screen.PREMIUM -> {
            BackHandler { screen = Screen.MAIN }
            PremiumScreen(
                state = monetizationState,
                monetization = monetization,
                features = listOf(stringResource(R.string.premium_feature_vacations)),
                onBack = { screen = Screen.MAIN },
            )
        }
        Screen.SETTINGS -> {
            BackHandler { screen = Screen.MAIN }
            SettingsScreen(
                region = region,
                isPremium = monetizationState.isPremium,
                showPrivacyOptions = remember { activity?.let(monetization::isPrivacyOptionsRequired) ?: false },
                onChangeRegion = { screen = Screen.REGION },
                onPrivacyOptions = { activity?.let(monetization::showPrivacyOptions) },
                onOpenPremium = { screen = Screen.PREMIUM },
                onBack = { screen = Screen.MAIN },
            )
        }
        else -> Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(stringResource(R.string.app_name))
                                Text(regionName(region), style = MaterialTheme.typography.labelMedium)
                            }
                        },
                        actions = {
                            PremiumAction(monetizationState) { screen = Screen.PREMIUM }
                            IconButton(onClick = { screen = Screen.SETTINGS }) {
                                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                            }
                        },
                    )
                    PrimaryScrollableTabRow(selectedTabIndex = tab.ordinal) {
                        CalendarTab.entries.forEach { option ->
                            Tab(
                                selected = option == tab,
                                onClick = { tab = option },
                                text = { Text(stringResource(option.label)) },
                            )
                        }
                    }
                }
            },
            bottomBar = { AdBanner(monetizationState) },
        ) { padding ->
            val modifier = Modifier.padding(padding).fillMaxSize()
            when (tab) {
                CalendarTab.CALENDAR -> MonthCalendarTab(
                    calendar = calendar,
                    region = region,
                    today = today,
                    onToggleLocal = { date -> scope.launch { settingsRepository.toggleLocalHoliday(date) } },
                    // Cambiar de mes es una pausa natural (con límite de frecuencia).
                    onMonthChanged = { activity?.let { monetization.maybeShowInterstitial(it) } },
                    modifier = modifier,
                )
                CalendarTab.BRIDGES -> BridgesTab(calendar, region, today, modifier)
                CalendarTab.WORKING_DAYS -> WorkingDaysTab(calendar, today, modifier)
                CalendarTab.VACATIONS -> VacationsTab(
                    calendar = calendar,
                    today = today,
                    vacationDays = settings.vacationDays,
                    onVacationDaysChange = { days -> scope.launch { settingsRepository.setVacationDays(days) } },
                    monetization = monetization,
                    monetizationState = monetizationState,
                    onGoPremium = { screen = Screen.PREMIUM },
                    modifier = modifier,
                )
            }
        }
    }
}
