package com.calendariolaboral.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WorkCalendarTest {
    private val madrid = WorkCalendar(HolidayCalendar.holidays(2026, Region.MADRID))

    @Test
    fun `dias laborables de enero de 2026 en madrid`() {
        // 22 días entre semana menos el 1 (jueves) y el 6 (martes).
        assertEquals(20, madrid.workingDaysBetween(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
    }

    @Test
    fun `fechas al reves dan cero`() {
        assertEquals(0, madrid.workingDaysBetween(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)))
    }

    @Test
    fun `puente de la inmaculada 2026 cogiendo el lunes 7`() {
        val bridge = madrid.bridges().first { it.holiday.date == LocalDate.of(2026, 12, 8) }

        assertEquals(listOf(LocalDate.of(2026, 12, 7)), bridge.daysToTake)
        assertEquals(LocalDate.of(2026, 12, 5), bridge.start)
        assertEquals(LocalDate.of(2026, 12, 8), bridge.end)
        assertEquals(4, bridge.totalDaysOff)
    }

    @Test
    fun `el 12 de octubre de 2026 en lunes no es puente`() {
        assertTrue(madrid.bridges().none { it.holiday.date == LocalDate.of(2026, 10, 12) })
    }

    @Test
    fun `proximo festivo`() {
        assertEquals(LocalDate.of(2026, 4, 2), madrid.nextHoliday(LocalDate.of(2026, 3, 1))!!.date)
    }

    @Test
    fun `el planificador aprovecha semana santa`() {
        val plans = madrid.planVacations(2026, vacationDays = 3, count = 3)

        assertTrue(plans.isNotEmpty())
        // Del 28 de marzo al 5 de abril: lunes 30, martes 31 y miércoles 1 de vacaciones = 9 días libres.
        val easter = plans.first { !it.start.isAfter(LocalDate.of(2026, 3, 30)) && !it.end.isBefore(LocalDate.of(2026, 4, 5)) }
        assertEquals(3, easter.vacationDaysUsed)
        assertEquals(9, easter.totalDaysOff)
        assertTrue(plans.zipWithNext().all { (a, b) -> a.end.isBefore(b.start) })
    }

    @Test
    fun `el plan del anio no gasta mas dias de los que hay`() {
        val plans = madrid.planYear(2026, totalVacationDays = 22)

        assertTrue(plans.sumOf { it.vacationDaysUsed } <= 22)
        assertTrue(plans.sumOf { it.totalDaysOff } > 22)
        assertTrue(plans.zipWithNext().all { (a, b) -> a.end.isBefore(b.start) })
    }
}
