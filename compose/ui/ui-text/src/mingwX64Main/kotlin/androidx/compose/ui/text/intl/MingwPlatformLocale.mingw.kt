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

package androidx.compose.ui.text.intl

import androidx.compose.runtime.Immutable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.windows.LOCALE_NAME_MAX_LENGTH
import platform.windows.GetUserDefaultLocaleName
import platform.windows.WCHARVar

/**
 * A simple parsed BCP47 language tag used as the platform locale on mingwX64.
 */
internal class MingwLocale(val languageTag: String) {
    val language: String
    val script: String
    val region: String

    init {
        // Parse BCP47 tag: language[-script][-region]
        val parts = languageTag.replace("_", "-").split("-")
        language = parts.getOrElse(0) { "en" }
        var parsedScript = ""
        var parsedRegion = ""
        for (i in 1 until parts.size) {
            val part = parts[i]
            when {
                part.length == 4 && part[0].isUpperCase() -> parsedScript = part
                part.length == 2 && part.all { it.isUpperCase() } -> parsedRegion = part
                part.length == 3 && part.all { it.isDigit() } -> parsedRegion = part
            }
        }
        script = parsedScript
        region = parsedRegion
    }
}

@Immutable
actual class Locale internal constructor(internal val platformLocale: MingwLocale) {
    actual val language: String
        get() = platformLocale.language
    actual val script: String
        get() = platformLocale.script
    actual val region: String
        get() = platformLocale.region

    actual fun toLanguageTag(): String = platformLocale.languageTag

    actual override operator fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is Locale) return false
        if (this === other) return true
        return toLanguageTag() == other.toLanguageTag()
    }

    actual override fun hashCode(): Int = toLanguageTag().hashCode()

    actual override fun toString(): String = toLanguageTag()

    actual companion object {
        actual val current: Locale
            get() = platformLocaleDelegate.current[0]
    }

    actual constructor(languageTag: String) : this(MingwLocale(languageTag))
}

@OptIn(ExperimentalForeignApi::class)
private fun getSystemLocaleTag(): String {
    return memScoped {
        val buffer = allocArray<WCHARVar>(LOCALE_NAME_MAX_LENGTH)
        val len = GetUserDefaultLocaleName(buffer, LOCALE_NAME_MAX_LENGTH)
        if (len > 0) buffer.toKString() else "en-US"
    }
}

internal actual fun createPlatformLocaleDelegate(): PlatformLocaleDelegate =
    object : PlatformLocaleDelegate {
        override val current: LocaleList
            get() = LocaleList(listOf(Locale(getSystemLocaleTag())))
    }

private val rtlLanguagesSet = setOf("ar", "fa", "he", "iw", "ji", "ur", "yi")

internal actual fun Locale.isRtl(): Boolean = this.language in rtlLanguagesSet
