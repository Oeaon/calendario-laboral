package com.calendariolaboral.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HolidayCalendarTest {
    @Test
    fun `domingo de pascua de varios anios`() {
        assertEquals(LocalDate.of(2025, 4, 20), HolidayCalendar.easterSunday(2025))
        assertEquals(LocalDate.of(2026, 4, 5), HolidayCalendar.easterSunday(2026))
        assertEquals(LocalDate.of(2027, 3, 28), HolidayCalendar.easterSunday(2027))
        assertEquals(LocalDate.of(2030, 4, 21), HolidayCalendar.easterSunday(2030))
    }

    @Test
    fun `madrid 2026 tiene los nacionales, semana santa y el 2 de mayo`() {
        val dates = HolidayCalendar.holidays(2026, Region.MADRID).map { it.date }

        assertTrue(LocalDate.of(2026, 1, 1) in dates)
        assertTrue(LocalDate.of(2026, 4, 2) in dates)
        assertTrue(LocalDate.of(2026, 4, 3) in dates)
        assertTrue(LocalDate.of(2026, 5, 2) in dates)
        assertTrue(LocalDate.of(2026, 12, 25) in dates)
        assertFalse(LocalDate.of(2026, 4, 6) in dates)
    }

    @Test
    fun `cataluna tiene lunes de pascua y no jueves santo`() {
        val holidays = HolidayCalendar.holidays(2026, Region.CATALUNA)
        val dates = holidays.map { it.date }

        assertFalse(LocalDate.of(2026, 4, 2) in dates)
        assertTrue(LocalDate.of(2026, 4, 6) in dates)
        assertTrue(LocalDate.of(2026, 9, 11) in dates)
        assertEquals(HolidayKind.ST_STEPHEN, holidays.first { it.date == LocalDate.of(2026, 12, 26) }.kind)
    }

    @Test
    fun `los festivos locales se suman y van ordenados`() {
        val local = LocalDate.of(2026, 5, 15)
        val holidays = HolidayCalendar.holidays(2026, Region.MADRID, setOf(local, LocalDate.of(2027, 1, 2)))

        assertEquals(HolidayScope.LOCAL, holidays.first { it.date == local }.scope)
        assertTrue(holidays.none { it.date.year == 2027 })
        assertEquals(holidays.sortedBy { it.date }, holidays)
    }

    @Test
    fun `un festivo local no tapa uno nacional`() {
        val holidays = HolidayCalendar.holidays(2026, Region.MADRID, setOf(LocalDate.of(2026, 12, 25)))

        assertEquals(HolidayScope.NATIONAL, holidays.first { it.date == LocalDate.of(2026, 12, 25) }.scope)
    }
}
