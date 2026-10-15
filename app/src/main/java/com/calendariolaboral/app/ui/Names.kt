package com.calendariolaboral.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.calendariolaboral.app.R
import com.calendariolaboral.core.Holiday
import com.calendariolaboral.core.HolidayKind
import com.calendariolaboral.core.Region
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun regionName(region: Region): String = stringResource(
    when (region) {
        Region.ANDALUCIA -> R.string.region_andalucia
        Region.ARAGON -> R.string.region_aragon
        Region.ASTURIAS -> R.string.region_asturias
        Region.BALEARES -> R.string.region_baleares
        Region.CANARIAS -> R.string.region_canarias
        Region.CANTABRIA -> R.string.region_cantabria
        Region.CASTILLA_LA_MANCHA -> R.string.region_castilla_la_mancha
        Region.CASTILLA_Y_LEON -> R.string.region_castilla_y_leon
        Region.CATALUNA -> R.string.region_cataluna
        Region.CEUTA -> R.string.region_ceuta
        Region.VALENCIA -> R.string.region_valencia
        Region.EXTREMADURA -> R.string.region_extremadura
        Region.GALICIA -> R.string.region_galicia
        Region.MADRID -> R.string.region_madrid
        Region.MELILLA -> R.string.region_melilla
        Region.MURCIA -> R.string.region_murcia
        Region.NAVARRA -> R.string.region_navarra
        Region.PAIS_VASCO -> R.string.region_pais_vasco
        Region.LA_RIOJA -> R.string.region_la_rioja
    },
)

@Composable
fun holidayName(holiday: Holiday, region: Region): String = stringResource(
    when (holiday.kind) {
        HolidayKind.NEW_YEAR -> R.string.holiday_new_year
        HolidayKind.EPIPHANY -> R.string.holiday_epiphany
        HolidayKind.MAUNDY_THURSDAY -> R.string.holiday_maundy_thursday
        HolidayKind.GOOD_FRIDAY -> R.string.holiday_good_friday
        HolidayKind.EASTER_MONDAY -> R.string.holiday_easter_monday
        HolidayKind.LABOUR_DAY -> R.string.holiday_labour_day
        HolidayKind.ASSUMPTION -> R.string.holiday_assumption
        HolidayKind.NATIONAL_DAY -> R.string.holiday_national_day
        HolidayKind.ALL_SAINTS -> R.string.holiday_all_saints
        HolidayKind.CONSTITUTION -> R.string.holiday_constitution
        HolidayKind.IMMACULATE_CONCEPTION -> R.string.holiday_immaculate
        HolidayKind.CHRISTMAS -> R.string.holiday_christmas
        HolidayKind.ST_STEPHEN -> R.string.holiday_st_stephen
        HolidayKind.ST_JOSEPH -> R.string.holiday_st_joseph
        HolidayKind.ST_JOHN -> R.string.holiday_st_john
        HolidayKind.ST_JAMES -> R.string.holiday_st_james
        HolidayKind.LOCAL -> R.string.holiday_local
        HolidayKind.REGION_DAY -> regionDay(region)
    },
)

private fun regionDay(region: Region): Int = when (region) {
    Region.ANDALUCIA -> R.string.day_andalucia
    Region.ARAGON -> R.string.day_aragon
    Region.ASTURIAS -> R.string.day_asturias
    Region.BALEARES -> R.string.day_baleares
    Region.CANARIAS -> R.string.day_canarias
    Region.CANTABRIA -> R.string.day_cantabria
    Region.CASTILLA_LA_MANCHA -> R.string.day_castilla_la_mancha
    Region.CASTILLA_Y_LEON -> R.string.day_castilla_y_leon
    Region.CATALUNA -> R.string.day_cataluna
    Region.CEUTA -> R.string.day_ceuta
    Region.VALENCIA -> R.string.day_valencia
    Region.EXTREMADURA -> R.string.day_extremadura
    Region.GALICIA -> R.string.day_galicia
    Region.MADRID -> R.string.day_madrid
    Region.MELILLA -> R.string.day_melilla
    Region.MURCIA -> R.string.day_murcia
    Region.NAVARRA -> R.string.day_navarra
    Region.PAIS_VASCO -> R.string.day_pais_vasco
    Region.LA_RIOJA -> R.string.day_la_rioja
}

private val FULL_DATE: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
private val MEDIUM_DATE: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

fun formatFull(date: LocalDate): String = date.format(FULL_DATE).replaceFirstChar { it.titlecase() }

fun formatMedium(date: LocalDate): String = date.format(MEDIUM_DATE)
