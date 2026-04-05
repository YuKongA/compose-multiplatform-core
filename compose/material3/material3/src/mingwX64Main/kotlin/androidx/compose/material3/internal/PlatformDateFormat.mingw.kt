/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalTime::class)

package androidx.compose.material3.internal

import androidx.compose.material3.CalendarLocale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime

internal actual class PlatformDateFormat actual constructor(private val locale: CalendarLocale) {

    actual val firstDayOfWeek: Int
        get() = firstDayOfWeek()

    private val firstDaysOfWeekByRegionCode: Map<String, Int> by lazy {
        listOf(
            7 to listOf("TH", "ET", "SG", "JM", "BT", "IN", "US", "MO", "KE", "DO", "AU", "IL",
                "AS", "TW", "MZ", "MM", "CN", "PR", "PK", "BD", "NP", "HN", "BR", "HK", "TT",
                "ZA", "VE", "MT", "PH", "PE", "ID", "DM", "WS", "ZW", "UM", "LA", "BZ", "JP",
                "SV", "SA", "CO", "GT", "BW", "KR", "PA", "YE", "BS", "MX", "MH", "GU", "PY",
                "AG", "CA", "KH", "PT", "VI", "NI"),
            6 to listOf("EG", "AF", "SY", "IR", "OM", "IQ", "DZ", "DJ", "AE", "SD", "KW", "JO",
                "BH", "QA", "LY")
        ).flatMap { (day, tags) -> tags.map { it to day } }.toMap()
    }

    private val regionsWith12HourFormat by lazy {
        listOf("AE", "AG", "AL", "AS", "AU", "BB", "BD", "BH", "BM", "BN", "BS", "BT", "CA",
            "CN", "CO", "CY", "DJ", "DM", "DO", "DZ", "EG", "EH", "ER", "ET", "FJ", "FM",
            "GD", "GH", "GM", "GR", "GU", "GY", "HK", "IN", "IQ", "JM", "JO", "KH", "KI",
            "KN", "KP", "KR", "KW", "KY", "LB", "LC", "LR", "LS", "LY", "MH", "MO", "MP",
            "MR", "MW", "MY", "NA", "NZ", "OM", "PA", "PG", "PH", "PK", "PR", "PS", "PW",
            "QA", "SA", "SB", "SD", "SG", "SL", "SO", "SS", "SY", "SZ", "TC", "TD", "TN",
            "TO", "TT", "TW", "UM", "US", "VC", "VE", "VG", "VI", "VU", "WS", "YE", "ZM")
    }

    private val defaultWeekdayNames = listOf(
        "Monday" to "M", "Tuesday" to "T", "Wednesday" to "W",
        "Thursday" to "T", "Friday" to "F", "Saturday" to "S", "Sunday" to "S"
    )

    @OptIn(FormatStringsInDatetimeFormats::class)
    actual fun formatWithPattern(
        utcTimeMillis: Long,
        pattern: String,
        cache: MutableMap<String, Any>
    ): String {
        val dateTime = Instant
            .fromEpochMilliseconds(utcTimeMillis)
            .toLocalDateTime(TimeZone.UTC)

        return try {
            val fmt = kotlinx.datetime.LocalDateTime.Format { byUnicodePattern(pattern) }
            fmt.format(dateTime)
        } catch (_: Throwable) {
            "${dateTime.year}-${dateTime.monthNumber.toString().padStart(2, '0')}-${dateTime.dayOfMonth.toString().padStart(2, '0')}"
        }
    }

    actual fun formatWithSkeleton(
        utcTimeMillis: Long,
        skeleton: String,
        cache: MutableMap<String, Any>
    ): String {
        // Simplified: treat skeleton as pattern for common cases
        val pattern = when {
            skeleton.contains("yMMM") && skeleton.contains("d") -> "MMM d, yyyy"
            skeleton.contains("yMMMM") && skeleton.contains("EEEE") && skeleton.contains("d") ->
                "EEEE, MMMM d, yyyy"
            skeleton.contains("yMMMM") || skeleton.contains("LLLL") -> "MMMM yyyy"
            else -> skeleton
        }
        return formatWithPattern(utcTimeMillis, pattern, cache)
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    actual fun parse(
        date: String,
        pattern: String,
        locale: CalendarLocale,
        cache: MutableMap<String, Any>
    ): CalendarDate? {
        return try {
            LocalDate.parse(
                input = date,
                format = LocalDate.Format { byUnicodePattern(pattern) }
            ).let { localDate ->
                Instant.fromEpochMilliseconds(
                    localDate.toEpochDays().toLong() * MillisecondsIn24Hours
                ).toCalendarDate(TimeZone.UTC)
            }
        } catch (_: Throwable) {
            null
        }
    }

    actual fun getDateInputFormat(): DateInputFormat {
        val region = locale.region.uppercase()
        // US-style: MM/dd/yyyy, most others: dd/MM/yyyy
        val pattern = if (region == "US" || region == "PH" || region == "CA") {
            "MM/dd/yyyy"
        } else {
            "dd/MM/yyyy"
        }
        return datePatternAsInputFormat(pattern)
    }

    actual val weekdayNames: List<Pair<String, String>>
        get() = defaultWeekdayNames

    private fun firstDayOfWeek(): Int {
        val region = locale.region.uppercase()
        return firstDaysOfWeekByRegionCode[region] ?: 1
    }

    actual fun is24HourFormat(): Boolean {
        val region = locale.region.uppercase()
        return region !in regionsWith12HourFormat
    }
}

private const val MillisecondsIn24Hours = 86400000L
