# Calendario Laboral

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white) ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white) ![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)

Calendario laboral de España por comunidad autónoma: festivos, puentes, días laborables y planificador de vacaciones.

App Android nativa desarrollada en **Kotlin** con **Jetpack Compose** (Material 3).

- Calendario mensual con festivos nacionales, autonómicos y locales, y la cuenta atrás hasta el próximo festivo.
- Festivos locales: se marcan tocando el día (cada municipio tiene dos).
- **Puentes**: festivos en martes, miércoles o jueves y qué días coger para el fin de semana largo.
- **Días laborables** entre dos fechas y del año entero.
- **Planificador de vacaciones** (Premium o 24 h con un anuncio): reparte tus días entre los bloques que dan más días libres seguidos.
- Funciona sin conexión.

## Anuncios y Premium

| Formato | Dónde | Límite |
|---|---|---|
| Banner adaptativo | Pantallas de consulta | — |
| Intersticial | Solo en pausas naturales (al cambiar de mes) | Uno cada 3 min como mucho |
| App Open | Al volver a la app desde segundo plano, nunca en el primer arranque | Uno cada 5 min como mucho |
| Recompensado | Cuando el usuario lo pide para desbloquear el planificador de vacaciones durante 24 h | — |

Premium (Google Play Billing) quita todos los anuncios y desbloquea el planificador de vacaciones para siempre:

- `premium_monthly`: suscripción mensual.
- `premium_lifetime`: producto de compra única.

Créalos en Play Console con esos IDs. Al arrancar se consulta la tienda, así que si la suscripción caduca vuelven los anuncios. Para una app con muchos ingresos conviene añadir verificación de compras en servidor.

Los IDs de AdMob van en `admob.properties` (no se sube a git; plantilla en `admob.properties.example`). Sin ese archivo se usan las unidades de prueba de Google. El consentimiento RGPD se pide con UMP antes de cargar ningún anuncio.

## Idiomas

Castellano (por defecto), catalán, gallego e inglés.

## Festivos

`HolidayCalendar` genera los festivos de cualquier año por reglas:

- Nacionales fijos (1/1, 6/1, 1/5, 15/8, 12/10, 1/11, 6/12, 8/12, 25/12) y Viernes Santo.
- Jueves Santo en todas las comunidades salvo Cataluña y la Comunitat Valenciana; Lunes de Pascua en Cataluña, Comunitat Valenciana, Illes Balears, Navarra y País Vasco.
- El día propio de cada comunidad y sus fiestas fijas (Sant Joan, Sant Esteve, Santiago…).

Cada año las comunidades trasladan algunos festivos (sobre todo los que caen en domingo) o eligen otros. Esos cambios van en `OfficialAdjustments.kt`: **antes de publicar, revisa el calendario del año con la resolución del BOE y los decretos de cada comunidad** y añade allí las diferencias. Para 2026, el 1 de noviembre y el 6 de diciembre caen en domingo.

## Estructura

```
core/   Kotlin puro: festivos, Pascua, días laborables, puentes y planificador (con tests)
app/    Android: Compose y Material 3, ajustes con DataStore
  ui/             pestañas y componentes
  monetization/   consentimiento, anuncios y compras de Premium
```

## Compilar

Requisitos: Android Studio reciente, Android SDK 36 y un JDK entre el 17 y el 25 (probado con Java 25). Gradle 9.7, Android Gradle Plugin 9.4 (con Kotlin integrado) y Kotlin 2.3.

```bash
./gradlew :core:test        # tests de la lógica
./gradlew installDebug      # instala en el emulador o el móvil (anuncios de prueba)
./gradlew bundleRelease     # AAB para Google Play
```

### Release

1. Crea el keystore de subida y un `keystore.properties` en la raíz (no se sube a git):
   ```
   storeFile=upload.jks
   storePassword=...
   keyAlias=upload
   keyPassword=...
   ```
2. Copia `admob.properties.example` como `admob.properties` y pon los IDs reales de AdMob de esta app.
3. Crea en Play Console los productos `premium_monthly` (suscripción) y `premium_lifetime` (compra única).
4. Publica `docs/privacy.html` (por ejemplo con GitHub Pages) y enlázala en la ficha de Play.
