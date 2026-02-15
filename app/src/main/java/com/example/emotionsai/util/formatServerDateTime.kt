package com.example.emotionsai.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeEng = Locale("eng", "ENG")
private val zone = ZoneId.systemDefault()


// 14 Feb 2026, 15:30
private val dtLong = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", localeEng)

// 15:30 (если сегодня)
private val timeOnly = DateTimeFormatter.ofPattern("HH:mm", localeEng)

// 14 Feb (если в этом году)
private val dayMonth = DateTimeFormatter.ofPattern("d MMM", localeEng)

fun formatServerDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"

    // 1) пробуем ISO instant: 2026-02-14T10:12:30Z / +00:00
    val instant = runCatching { Instant.parse(raw) }.getOrNull()
        // 2) пробуем OffsetDateTime: 2026-02-14T15:12:30+05:00
        ?: runCatching { OffsetDateTime.parse(raw).toInstant() }.getOrNull()
        // 3) пробуем LocalDateTime без зоны: 2026-02-14T15:12:30
        ?: runCatching { LocalDateTime.parse(raw).atZone(zone).toInstant() }.getOrNull()
        ?: return raw // если формат неожиданный — покажем как есть, чтобы не ломать UI

    val zdt = instant.atZone(zone)
    val today = LocalDate.now(zone)

    return when {
        zdt.toLocalDate() == today -> "Today, ${zdt.format(timeOnly)}"
        zdt.toLocalDate().year == today.year -> zdt.format(dayMonth) + ", " + zdt.format(timeOnly)
        else -> zdt.format(dtLong)
    }
}
