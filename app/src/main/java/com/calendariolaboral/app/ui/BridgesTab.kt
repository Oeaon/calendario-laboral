package com.calendariolaboral.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calendariolaboral.app.R
import com.calendariolaboral.core.Region
import com.calendariolaboral.core.WorkCalendar
import java.time.LocalDate

@Composable
fun BridgesTab(calendar: WorkCalendar, region: Region, today: LocalDate, modifier: Modifier) {
    val bridges = calendar.bridges().filter { !it.end.isBefore(today) && it.start.year <= today.year + 1 }
    LazyColumn(modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(stringResource(R.string.bridges_intro), style = MaterialTheme.typography.bodyMedium) }
        if (bridges.isEmpty()) {
            item { Text(stringResource(R.string.bridges_none), style = MaterialTheme.typography.bodyLarge) }
        }
        items(bridges, key = { it.holiday.date.toString() }) { bridge ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(holidayName(bridge.holiday, region), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.bridge_range, formatMedium(bridge.start), formatMedium(bridge.end)),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        pluralStringResource(
                            R.plurals.bridge_take,
                            bridge.daysToTake.size,
                            bridge.daysToTake.size,
                            bridge.daysToTake.joinToString { formatMedium(it) },
                            bridge.totalDaysOff,
                        ),
                    )
                }
            }
        }
    }
}
