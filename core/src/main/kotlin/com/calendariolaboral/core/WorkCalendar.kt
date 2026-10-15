package com.calendariolaboral.core

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Puente: con [daysToTake] días de vacaciones se encadenan [totalDaysOff] días libres. */
data class Bridge(val holiday: Holiday, val daysToTake: List<LocalDate>, val start: LocalDate, val end: LocalDate) {
    val totalDaysOff: Int get() = ChronoUnit.DAYS.between(start, end).toInt() + 1
}

/** Bloque de vacaciones propuesto por el planificador. */
data class VacationPlan(val start: LocalDate, val end: LocalDate, val vacationDaysUsed: Int) {
    val totalDaysOff: Int get() = ChronoUnit.DAYS.between(start, end).toInt() + 1
    val efficiency: Double get() = totalDaysOff.toDouble() / vacationDaysUsed
}

class WorkCalendar(private val holidays: List<Holiday>) {
    private val holidayDates = holidays.map { it.date }.toSet()

    fun isWorkingDay(date: LocalDate) = HolidayCalendar.isWorkingDay(date, holidayDates)

    fun holidayOn(date: LocalDate): Holiday? = holidays.firstOrNull { it.date == date }

    /** Días laborables entre dos fechas, ambas incluidas. */
    fun workingDaysBetween(from: LocalDate, to: LocalDate): Int {
        if (to.isBefore(from)) return 0
        return generateSequence(from) { it.plusDays(1) }.takeWhile { !it.isAfter(to) }.count { isWorkingDay(it) }
    }

    fun nextHoliday(from: LocalDate): Holiday? = holidays.firstOrNull { !it.date.isBefore(from) }

    /** Festivos en martes o jueves (puente de 1 día) y en miércoles (2 días) que dan un fin de semana largo. */
    fun bridges(): List<Bridge> = holidays.mapNotNull { holiday ->
        val date = holiday.date
        val toTake = when (date.dayOfWeek) {
            DayOfWeek.TUESDAY -> listOf(date.minusDays(1))
            DayOfWeek.THURSDAY -> listOf(date.plusDays(1))
            DayOfWeek.WEDNESDAY -> listOf(date.plusDays(1), date.plusDays(2))
            else -> return@mapNotNull null
        }.filter { isWorkingDay(it) }
        if (toTake.isEmpty()) return@mapNotNull null
        val (start, end) = extendOff(minOf(date, toTake.min()), maxOf(date, toTake.max()))
        Bridge(holiday, toTake, start, end)
    }

    /**
     * Propone los mejores bloques de vacaciones del año: para cada día laborable de inicio toma el
     * bloque más largo que gasta como mucho [vacationDays] días y lo alarga con los días libres
     * de alrededor. Ordena por días libres por día gastado y devuelve [count] bloques sin solaparse.
     */
    fun planVacations(year: Int, vacationDays: Int, count: Int = 5, from: LocalDate = LocalDate.of(year, 1, 1)): List<VacationPlan> {
        require(vacationDays >= 1)
        val last = LocalDate.of(year, 12, 31)
        val candidates = mutableListOf<VacationPlan>()
        var day = from
        while (!day.isAfter(last)) {
            if (isWorkingDay(day)) {
                var end = day
                var used = 0
                var cursor = day
                while (!cursor.isAfter(last)) {
                    if (isWorkingDay(cursor)) {
                        if (used == vacationDays) break
                        used++
                    }
                    end = cursor
                    cursor = cursor.plusDays(1)
                }
                val (start, extendedEnd) = extendOff(day, end)
                candidates += VacationPlan(start, extendedEnd, used)
            }
            day = day.plusDays(1)
        }
        val chosen = mutableListOf<VacationPlan>()
        candidates
            .sortedWith(compareByDescending<VacationPlan> { it.efficiency }.thenByDescending { it.totalDaysOff }.thenBy { it.start })
            .forEach { plan ->
                if (chosen.size < count && chosen.none { it.overlaps(plan) }) chosen += plan
            }
        return chosen.sortedBy { it.start }
    }

    /**
     * Reparte [totalVacationDays] entre los mejores bloques del año (de hasta [maxBlock] días cada uno),
     * empezando por los que dan más días libres por día gastado.
     */
    fun planYear(year: Int, totalVacationDays: Int, from: LocalDate = LocalDate.of(year, 1, 1), maxBlock: Int = 5): List<VacationPlan> {
        var remaining = totalVacationDays
        val chosen = mutableListOf<VacationPlan>()
        planVacations(year, minOf(maxBlock, totalVacationDays), count = 60, from = from)
            .sortedWith(compareByDescending<VacationPlan> { it.efficiency }.thenByDescending { it.totalDaysOff })
            .forEach { plan ->
                if (plan.vacationDaysUsed <= remaining) {
                    chosen += plan
                    remaining -= plan.vacationDaysUsed
                }
            }
        return chosen.sortedBy { it.start }
    }

    /** Alarga [start]..[end] con los días no laborables pegados por delante y por detrás. */
    private fun extendOff(start: LocalDate, end: LocalDate): Pair<LocalDate, LocalDate> {
        var s = start
        while (!isWorkingDay(s.minusDays(1))) s = s.minusDays(1)
        var e = end
        while (!isWorkingDay(e.plusDays(1))) e = e.plusDays(1)
        return s to e
    }

    private fun VacationPlan.overlaps(other: VacationPlan) = !end.isBefore(other.start) && !other.end.isBefore(start)
}
