package com.calendariolaboral.core

import java.time.LocalDate

data class Adjustment(
    val added: List<Pair<LocalDate, HolidayKind>> = emptyList(),
    val removed: List<LocalDate> = emptyList(),
)

/**
 * Correcciones del calendario oficial (resolución anual del BOE y decretos de cada comunidad)
 * sobre las reglas de [HolidayCalendar]: festivos que se trasladan al lunes, días que una comunidad
 * cambia por otro, etc. Añadir aquí cada año los cambios publicados.
 */
object OfficialAdjustments {
    private val byYear: Map<Int, Map<Region, Adjustment>> = mapOf(
        // 2026: revisar con el BOE. 1 de noviembre y 6 de diciembre caen en domingo; las comunidades
        // que los trasladen al lunes (2/11 y 7/12) deben añadirse aquí.
    )

    fun forYear(year: Int, region: Region): Adjustment = byYear[year]?.get(region) ?: Adjustment()
}
