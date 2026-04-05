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

package androidx.compose.ui.input.pointer

/**
 * Represents a Windows cursor type identified by its resource ID.
 *
 * Standard cursor IDs from Windows API:
 * - IDC_ARROW = 32512
 * - IDC_IBEAM = 32513
 * - IDC_CROSS = 32515
 * - IDC_HAND = 32649
 */
internal class MingwCursor(val cursorId: Int) : PointerIcon {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MingwCursor) return false
        return cursorId == other.cursorId
    }

    override fun hashCode(): Int = cursorId

    override fun toString(): String = "MingwCursor(cursorId=$cursorId)"
}

internal actual val pointerIconDefault: PointerIcon = MingwCursor(32512)    // IDC_ARROW
internal actual val pointerIconCrosshair: PointerIcon = MingwCursor(32515)  // IDC_CROSS
internal actual val pointerIconText: PointerIcon = MingwCursor(32513)       // IDC_IBEAM
internal actual val pointerIconHand: PointerIcon = MingwCursor(32649)       // IDC_HAND
