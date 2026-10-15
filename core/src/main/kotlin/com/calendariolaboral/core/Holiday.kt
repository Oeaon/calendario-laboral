package com.calendariolaboral.core

import java.time.LocalDate

enum class HolidayScope { NATIONAL, REGIONAL, LOCAL }

/** Identificador estable del festivo; la app lo traduce a cada idioma. */
enum class HolidayKind {
    NEW_YEAR, EPIPHANY, MAUNDY_THURSDAY, GOOD_FRIDAY, EASTER_MONDAY, LABOUR_DAY, ASSUMPTION, NATIONAL_DAY,
    ALL_SAINTS, CONSTITUTION, IMMACULATE_CONCEPTION, CHRISTMAS, ST_STEPHEN, ST_JOSEPH, ST_JOHN, ST_JAMES,
    REGION_DAY, LOCAL,
}

data class Holiday(val date: LocalDate, val kind: HolidayKind, val scope: HolidayScope)
