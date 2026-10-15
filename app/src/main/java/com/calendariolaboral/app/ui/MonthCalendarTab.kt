package com.calendariolaboral.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.calendariolaboral.app.R
import com.calendariolaboral.app.ui.theme.DayColors
import com.calendariolaboral.core.Holiday
import com.calendariolaboral.core.HolidayScope
import com.calendariolaboral.core.Region
import com.calendariolaboral.core.WorkCalendar
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun MonthCalendarTab(
    calendar: WorkCalendar,
    region: Region,
    today: LocalDate,
    onToggleLocal: (LocalDate) -> Unit,
    onMonthChanged: () -> Unit,
    modifier: Modifier,
) {
    var monthText by rememberSaveable { mutableStateOf(YearMonth.from(today).toString()) }
    val month = YearMonth.parse(monthText)
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val firstAllowed = YearMonth.of(today.year - 1, 1)
    val lastAllowed = YearMonth.of(today.year + 1, 12)

    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        calendar.nextHoliday(today)?.let { NextHolidayCard(it, region, today) }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    monthText = month.minusMonths(1).toString()
                    onMonthChanged()
                },
                enabled = month > firstAllowed,
            ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_month)) }
            Text(
                monthTitle(month),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    monthText = month.plusMonths(1).toString()
                    onMonthChanged()
                },
                enabled = month < lastAllowed,
            ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_month)) }
        }

        MonthGrid(month, calendar, today) { selected = it.toString() }
        Legend()

        val monthHolidays = (1..month.lengthOfMonth()).mapNotNull { calendar.holidayOn(month.atDay(it)) }
        if (monthHolidays.isNotEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    monthHolidays.forEach { holiday -> HolidayRow(holiday, region) }
                }
            }
        }
        Text(
            stringResource(R.string.working_days_in_month, calendar.workingDaysBetween(month.atDay(1), month.atEndOfMonth())),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            stringResource(R.string.calendar_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    selected?.let { LocalDate.parse(it) }?.let { date ->
        DayDialog(date, calendar.holidayOn(date), region, calendar.isWorkingDay(date), onToggleLocal) { selected = null }
    }
}

@Composable
private fun NextHolidayCard(holiday: Holiday, region: Region, today: LocalDate) {
    val days = ChronoUnit.DAYS.between(today, holiday.date).toInt()
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.next_holiday), style = MaterialTheme.typography.labelLarge)
            Text(holidayName(holiday, region), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(formatFull(holiday.date))
            Text(
                if (days == 0) stringResource(R.string.today_is_holiday) else pluralStringResource(R.plurals.days_left, days, days),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun MonthGrid(month: YearMonth, calendar: WorkCalendar, today: LocalDate, onDayClick: (LocalDate) -> Unit) {
    val locale = Locale.getDefault()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row {
            DayOfWeek.entries.forEach { dow ->
                Text(
                    dow.getDisplayName(TextStyle.NARROW, locale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val leading = month.atDay(1).dayOfWeek.value - 1
        val cells = List(leading) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (0 until 7).forEach { i ->
                    val date = week.getOrNull(i)
                    Box(Modifier.weight(1f).aspectRatio(1f)) {
                        if (date != null) DayCell(date, calendar.holidayOn(date), calendar.isWorkingDay(date), date == today) { onDayClick(date) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, holiday: Holiday?, working: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val background = holiday?.let { scopeColor(it.scope) }
    val textColor = when {
        background != null -> Color.White
        !working -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(background ?: Color.Transparent, CircleShape)
            .then(if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "${date.dayOfMonth}",
            color = textColor,
            fontWeight = if (holiday != null || isToday) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun Legend() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(DayColors.national, stringResource(R.string.scope_national))
        LegendItem(DayColors.regional, stringResource(R.string.scope_regional))
        LegendItem(DayColors.local, stringResource(R.string.scope_local))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun HolidayRow(holiday: Holiday, region: Region) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(scopeColor(holiday.scope), CircleShape))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(holidayName(holiday, region), style = MaterialTheme.typography.bodyLarge)
            Text(formatFull(holiday.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun scopeColor(scope: HolidayScope): Color = when (scope) {
    HolidayScope.NATIONAL -> DayColors.national
    HolidayScope.REGIONAL -> DayColors.regional
    HolidayScope.LOCAL -> DayColors.local
}

@Composable
private fun DayDialog(
    date: LocalDate,
    holiday: Holiday?,
    region: Region,
    working: Boolean,
    onToggleLocal: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    // Solo se pueden marcar o quitar festivos locales; los oficiales no se tocan.
    val canToggle = holiday == null || holiday.scope == HolidayScope.LOCAL
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(formatFull(date)) },
        text = {
            Text(
                when {
                    holiday != null -> holidayName(holiday, region)
                    working -> stringResource(R.string.working_day)
                    else -> stringResource(R.string.weekend)
                },
            )
        },
        confirmButton = {
            if (canToggle) {
                TextButton(onClick = {
                    onToggleLocal(date)
                    onDismiss()
                }) {
                    Text(stringResource(if (holiday == null) R.string.mark_local else R.string.unmark_local))
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
    )
}

private fun monthTitle(month: YearMonth): String {
    val name = month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
    return "${name.replaceFirstChar { it.titlecase() }} ${month.year}"
}
