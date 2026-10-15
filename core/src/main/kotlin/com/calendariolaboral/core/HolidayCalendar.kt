package com.calendariolaboral.core

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.MonthDay

/**
 * Festivos laborales por reglas: los nacionales fijos, los que dependen de la Semana Santa y el día
 * de cada comunidad. Las comunidades cambian algunos cada año (sobre todo cuando caen en domingo),
 * así que [OfficialAdjustments] recoge las correcciones del calendario publicado en el BOE.
 */
object HolidayCalendar {
    private val nationalFixed = listOf(
        MonthDay.of(1, 1) to HolidayKind.NEW_YEAR,
        MonthDay.of(1, 6) to HolidayKind.EPIPHANY,
        MonthDay.of(5, 1) to HolidayKind.LABOUR_DAY,
        MonthDay.of(8, 15) to HolidayKind.ASSUMPTION,
        MonthDay.of(10, 12) to HolidayKind.NATIONAL_DAY,
        MonthDay.of(11, 1) to HolidayKind.ALL_SAINTS,
        MonthDay.of(12, 6) to HolidayKind.CONSTITUTION,
        MonthDay.of(12, 8) to HolidayKind.IMMACULATE_CONCEPTION,
        MonthDay.of(12, 25) to HolidayKind.CHRISTMAS,
    )

    /** Jueves Santo es festivo en todas salvo Cataluña y la Comunidad Valenciana. */
    private val withoutMaundyThursday = setOf(Region.CATALUNA, Region.VALENCIA)

    private val withEasterMonday = setOf(Region.CATALUNA, Region.VALENCIA, Region.BALEARES, Region.NAVARRA, Region.PAIS_VASCO)

    /** Festivos propios de cada comunidad que se repiten todos los años. */
    private val regionalFixed: Map<Region, List<Pair<MonthDay, HolidayKind>>> = mapOf(
        Region.ANDALUCIA to listOf(MonthDay.of(2, 28) to HolidayKind.REGION_DAY),
        Region.ARAGON to listOf(MonthDay.of(4, 23) to HolidayKind.REGION_DAY),
        Region.ASTURIAS to listOf(MonthDay.of(9, 8) to HolidayKind.REGION_DAY),
        Region.BALEARES to listOf(MonthDay.of(3, 1) to HolidayKind.REGION_DAY),
        Region.CANARIAS to listOf(MonthDay.of(5, 30) to HolidayKind.REGION_DAY),
        Region.CANTABRIA to listOf(MonthDay.of(7, 28) to HolidayKind.REGION_DAY),
        Region.CASTILLA_LA_MANCHA to listOf(MonthDay.of(5, 31) to HolidayKind.REGION_DAY),
        Region.CASTILLA_Y_LEON to listOf(MonthDay.of(4, 23) to HolidayKind.REGION_DAY),
        Region.CATALUNA to listOf(
            MonthDay.of(6, 24) to HolidayKind.ST_JOHN,
            MonthDay.of(9, 11) to HolidayKind.REGION_DAY,
            MonthDay.of(12, 26) to HolidayKind.ST_STEPHEN,
        ),
        Region.CEUTA to listOf(MonthDay.of(9, 2) to HolidayKind.REGION_DAY),
        Region.VALENCIA to listOf(
            MonthDay.of(3, 19) to HolidayKind.ST_JOSEPH,
            MonthDay.of(6, 24) to HolidayKind.ST_JOHN,
            MonthDay.of(10, 9) to HolidayKind.REGION_DAY,
        ),
        Region.EXTREMADURA to listOf(MonthDay.of(9, 8) to HolidayKind.REGION_DAY),
        Region.GALICIA to listOf(
            MonthDay.of(5, 17) to HolidayKind.REGION_DAY,
            MonthDay.of(7, 25) to HolidayKind.ST_JAMES,
        ),
        Region.MADRID to listOf(MonthDay.of(5, 2) to HolidayKind.REGION_DAY),
        Region.MELILLA to listOf(MonthDay.of(9, 17) to HolidayKind.REGION_DAY),
        Region.MURCIA to listOf(MonthDay.of(6, 9) to HolidayKind.REGION_DAY),
        Region.NAVARRA to listOf(MonthDay.of(12, 3) to HolidayKind.REGION_DAY),
        Region.PAIS_VASCO to listOf(MonthDay.of(7, 25) to HolidayKind.ST_JAMES),
        Region.LA_RIOJA to listOf(MonthDay.of(6, 9) to HolidayKind.REGION_DAY),
    )

    fun holidays(year: Int, region: Region, localDates: Set<LocalDate> = emptySet()): List<Holiday> {
        val easter = easterSunday(year)
        val result = mutableMapOf<LocalDate, Holiday>()
        fun add(date: LocalDate, kind: HolidayKind, scope: HolidayScope) {
            result.putIfAbsent(date, Holiday(date, kind, scope))
        }

        nationalFixed.forEach { (day, kind) -> add(day.atYear(year), kind, HolidayScope.NATIONAL) }
        add(easter.minusDays(2), HolidayKind.GOOD_FRIDAY, HolidayScope.NATIONAL)
        if (region !in withoutMaundyThursday) add(easter.minusDays(3), HolidayKind.MAUNDY_THURSDAY, HolidayScope.REGIONAL)
        if (region in withEasterMonday) add(easter.plusDays(1), HolidayKind.EASTER_MONDAY, HolidayScope.REGIONAL)
        regionalFixed[region].orEmpty().forEach { (day, kind) -> add(day.atYear(year), kind, HolidayScope.REGIONAL) }

        OfficialAdjustments.forYear(year, region).let { adjustment ->
            adjustment.removed.forEach { result.remove(it) }
            adjustment.added.forEach { (date, kind) -> result[date] = Holiday(date, kind, HolidayScope.REGIONAL) }
        }
        localDates.filter { it.year == year }.forEach { add(it, HolidayKind.LOCAL, HolidayScope.LOCAL) }
        return result.values.sortedBy { it.date }
    }

    fun isWorkingDay(date: LocalDate, holidays: Set<LocalDate>): Boolean =
        date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY && date !in holidays

    /** Domingo de Pascua (algoritmo anónimo gregoriano, Meeus/Jones/Butcher). */
    fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }
}
