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

@file:OptIn(ExperimentalForeignApi::class)

package androidx.compose.ui.input.key

import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import kotlinx.cinterop.ExperimentalForeignApi
import platform.windows.GetKeyState
import platform.windows.MapVirtualKeyW
import platform.windows.VK_CONTROL
import platform.windows.VK_LWIN
import platform.windows.VK_MENU
import platform.windows.VK_RWIN
import platform.windows.VK_SHIFT
import platform.windows.WM_KEYDOWN
import platform.windows.WM_KEYUP
import platform.windows.WM_SYSKEYDOWN
import platform.windows.WM_SYSKEYUP
import platform.windows.WPARAM

internal fun createComposeKeyEvent(msg: UInt, wParam: WPARAM): KeyEvent {
    return KeyEvent(
        nativeKeyEvent = InternalKeyEvent(
            key = Key(wParam.toLong()),
            type = when (msg.toInt()) {
                WM_KEYDOWN, WM_SYSKEYDOWN -> KeyEventType.KeyDown
                WM_KEYUP, WM_SYSKEYUP -> KeyEventType.KeyUp
                else -> KeyEventType.Unknown
            },
            codePoint = MapVirtualKeyW(wParam.toUInt(), 2u).toInt(),
            modifiers = getKeyboardModifiers(),
            nativeEvent = null
        )
    )
}

internal fun getKeyboardModifiers() = PointerKeyboardModifiers(
    isCtrlPressed = GetKeyState(VK_CONTROL).toInt() < 0,
    isShiftPressed = GetKeyState(VK_SHIFT).toInt() < 0,
    isAltPressed = GetKeyState(VK_MENU).toInt() < 0,
    isMetaPressed = GetKeyState(VK_LWIN).toInt() < 0 || GetKeyState(VK_RWIN).toInt() < 0,
)
