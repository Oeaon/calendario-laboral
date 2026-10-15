package com.calendariolaboral.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calendariolaboral.app.R
import com.calendariolaboral.core.WorkCalendar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun WorkingDaysTab(calendar: WorkCalendar, today: LocalDate, modifier: Modifier) {
    var fromText by rememberSaveable { mutableStateOf(today.toString()) }
    var toText by rememberSaveable { mutableStateOf(today.withDayOfYear(today.lengthOfYear()).toString()) }
    val from = LocalDate.parse(fromText)
    val to = LocalDate.parse(toText)
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.working_days_intro), style = MaterialTheme.typography.bodyMedium)
        DateButton(stringResource(R.string.from_date), from) { fromText = it.toString() }
        DateButton(stringResource(R.string.to_date), to) { toText = it.toString() }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.working_days_result, calendar.workingDaysBetween(from, to)),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            stringResource(
                R.string.working_days_year,
                today.year,
                calendar.workingDaysBetween(LocalDate.of(today.year, 1, 1), LocalDate.of(today.year, 12, 31)),
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateButton(label: String, date: LocalDate, onDate: (LocalDate) -> Unit) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
        Text("$label: ${formatMedium(date)}")
    }
    if (open) {
        // El DatePicker trabaja en milisegundos UTC.
        val state = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                    open = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text(stringResource(android.R.string.cancel)) } },
        ) { DatePicker(state) }
    }
}
