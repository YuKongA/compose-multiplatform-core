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

package androidx.compose.foundation.gestures

import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFold
import kotlin.math.abs

internal actual fun platformScrollableDefaultFlingBehavior(): ScrollableDefaultFlingBehavior =
    DefaultFlingBehavior(
        SplineBasedFloatDecayAnimationSpec(UnityDensity).generateDecayAnimationSpec()
    )

@Composable
internal actual fun rememberPlatformDefaultFlingBehavior(): FlingBehavior {
    val flingSpec = rememberSplineBasedDecay<Float>()
    return remember(flingSpec) {
        DefaultFlingBehavior(flingSpec)
    }
}

internal actual fun CompositionLocalConsumerModifierNode.platformScrollConfig(): ScrollConfig =
    WindowsScrollConfig

private object WindowsScrollConfig : ScrollConfig {
    override var isSmoothScrollingEnabled = true
        internal set

    override fun isPreciseWheelScroll(event: PointerEvent): Boolean {
        // Windows 10+ precision touchpads send fractional WHEEL_DELTA values,
        // while regular mice always send exact multiples of WHEEL_DELTA (delta = ±1.0 per notch).
        // Detect precision by checking for sub-notch (fractional) scroll deltas.
        val delta = event.totalScrollDelta
        val absY = abs(delta.y)
        val absX = abs(delta.x)
        return (absY > 0f && absY < 1f) || (absX > 0f && absX < 1f)
    }

    override fun Density.calculateMouseWheelScroll(event: PointerEvent, bounds: IntSize): Offset {
        if (event.type == PointerEventType.PanMove) {
            return -event.changes.fastFold(Offset.Zero) { acc, c -> acc + c.panOffset }
        }

        val totalDelta = event.totalScrollDelta
        return if (isPreciseWheelScroll(event)) {
            // Precision touchpad: use fixed dp multiplier for smooth pixel-level scrolling
            // (matches macOS behavior where trackpad deltas use a small fixed multiplier)
            val multiplier = 10.dp.toPx()
            Offset(
                x = totalDelta.x * multiplier,
                y = totalDelta.y * multiplier
            ) * -1f
        } else {
            // Regular mouse wheel: scale relative to viewport size (1 notch = 5% of viewport)
            Offset(
                x = totalDelta.x * (bounds.width / 20f),
                y = totalDelta.y * (bounds.height / 20f)
            ) * -1f
        }
    }
}

private val PointerEvent.totalScrollDelta
    get() = this.changes.fastFold(Offset.Zero) { acc, c -> acc + c.scrollDelta }
