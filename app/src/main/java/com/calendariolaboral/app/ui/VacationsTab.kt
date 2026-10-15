package com.calendariolaboral.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calendariolaboral.app.R
import com.calendariolaboral.app.data.SettingsRepository
import com.calendariolaboral.app.monetization.LockedFeature
import com.calendariolaboral.app.monetization.Monetization
import com.calendariolaboral.app.monetization.MonetizationState
import com.calendariolaboral.core.WorkCalendar
import java.time.LocalDate
import java.util.Locale

/** Función Premium: el planificador que exprime los días de vacaciones. */
@Composable
fun VacationsTab(
    calendar: WorkCalendar,
    today: LocalDate,
    vacationDays: Int,
    onVacationDaysChange: (Int) -> Unit,
    monetization: Monetization,
    monetizationState: MonetizationState,
    onGoPremium: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LockedFeature(
            state = monetizationState,
            monetization = monetization,
            title = stringResource(R.string.vacations_title),
            description = stringResource(R.string.vacations_locked),
            onGoPremium = onGoPremium,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.vacations_title), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.vacations_days_to_use), modifier = Modifier.weight(1f))
                    FilledTonalIconButton(onClick = { onVacationDaysChange(vacationDays - 1) }, enabled = vacationDays > 1) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text("$vacationDays", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 12.dp))
                    FilledTonalIconButton(
                        onClick = { onVacationDaysChange(vacationDays + 1) },
                        enabled = vacationDays < SettingsRepository.MAX_VACATION_DAYS,
                    ) { Icon(Icons.Filled.Add, contentDescription = null) }
                }
                Text(
                    stringResource(R.string.vacations_explanation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                var year by rememberSaveable { mutableIntStateOf(today.year) }
                val years = listOf(today.year, today.year + 1)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    years.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = option == year,
                            onClick = { year = option },
                            shape = SegmentedButtonDefaults.itemShape(index, years.size),
                        ) { Text("$option") }
                    }
                }
                val from = if (year == today.year) today else LocalDate.of(year, 1, 1)
                val plans = calendar.planYear(year, vacationDays, from = from)
                Text(
                    stringResource(
                        R.string.vacations_summary,
                        plans.sumOf { it.vacationDaysUsed },
                        plans.sumOf { it.totalDaysOff },
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                plans.forEach { plan ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                stringResource(R.string.bridge_range, formatMedium(plan.start), formatMedium(plan.end)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(stringResource(R.string.vacations_plan, plan.vacationDaysUsed, plan.totalDaysOff))
                            Text(
                                stringResource(R.string.vacations_efficiency, String.format(Locale.getDefault(), "%.1f", plan.efficiency)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}
