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

package androidx.compose.ui.platform

import androidx.compose.ui.text.AnnotatedString
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.cinterop.wcstr
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.pointed
import platform.windows.CF_UNICODETEXT
import platform.windows.CloseClipboard
import platform.windows.EmptyClipboard
import platform.windows.GetClipboardData
import platform.windows.GlobalAlloc
import platform.windows.GlobalFree
import platform.windows.GlobalLock
import platform.windows.GlobalUnlock
import platform.windows.GMEM_MOVEABLE
import platform.windows.OpenClipboard
import platform.windows.SetClipboardData

actual typealias NativeClipboard = Any

actual class ClipEntry(val text: String) {
    actual val clipMetadata: ClipMetadata
        get() = TODO("ClipMetadata is not implemented")
}

@OptIn(ExperimentalForeignApi::class)
@Suppress("DEPRECATION")
internal class MingwClipboardManager : ClipboardManager {
    override fun getText(): AnnotatedString? =
        getClipboardText()?.let { AnnotatedString(it) }

    override fun setText(annotatedString: AnnotatedString) {
        setClipboardText(annotatedString.text)
    }

    override fun hasText(): Boolean = !getClipboardText().isNullOrEmpty()

    override fun getClip(): ClipEntry? {
        val text = getClipboardText() ?: return null
        return ClipEntry(text)
    }

    @Suppress("GetterSetterNames")
    override fun setClip(clipEntry: ClipEntry?) {
        if (clipEntry != null) {
            setClipboardText(clipEntry.text)
        }
    }

    private fun getClipboardText(): String? {
        if (OpenClipboard(null) == 0) return null
        try {
            val handle = GetClipboardData(CF_UNICODETEXT.toUInt()) ?: return null
            val ptr = GlobalLock(handle) ?: return null
            try {
                return ptr.reinterpret<platform.windows.WCHARVar>().toKString()
            } finally {
                GlobalUnlock(handle)
            }
        } finally {
            CloseClipboard()
        }
    }

    private fun setClipboardText(text: String) {
        memScoped {
            val wstr = text.wcstr
            val size = (wstr.size * 2).toULong()
            val hMem = GlobalAlloc(GMEM_MOVEABLE.toUInt(), size) ?: return
            val ptr = GlobalLock(hMem)
            if (ptr != null) {
                platform.posix.memcpy(ptr, wstr.ptr, size)
                GlobalUnlock(hMem)
            }
            if (OpenClipboard(null) != 0) {
                EmptyClipboard()
                SetClipboardData(CF_UNICODETEXT.toUInt(), hMem)
                CloseClipboard()
            } else {
                GlobalFree(hMem)
            }
        }
    }
}

internal class MingwPlatformClipboard : Clipboard {
    private val manager = MingwClipboardManager()

    override suspend fun getClipEntry(): ClipEntry? = manager.getClip()

    override suspend fun setClipEntry(clipEntry: ClipEntry?) {
        manager.setClip(clipEntry)
    }

    override val nativeClipboard: NativeClipboard
        get() = Unit
}

@Suppress("DEPRECATION")
internal actual fun createPlatformClipboardManager(): ClipboardManager = MingwClipboardManager()

internal actual fun createPlatformClipboard(): Clipboard = MingwPlatformClipboard()
