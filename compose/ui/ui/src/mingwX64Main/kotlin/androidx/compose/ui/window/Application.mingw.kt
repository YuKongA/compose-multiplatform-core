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

package androidx.compose.ui.window

import platform.windows.PostQuitMessage
import platform.windows.SetProcessDPIAware
import org.jetbrains.skiko.SkikoDispatchers
import org.jetbrains.skiko.preloadAngleEgl
import org.jetbrains.skiko.runMainMessageLoop

interface ApplicationScope {
    fun exitApplication()
}

/**
 * Entry point for a Compose Desktop application on mingwX64.
 *
 * Usage:
 * ```kotlin
 * fun main() = application {
 *     Window(title = "My App") {
 *         Text("Hello!")
 *     }
 * }
 * ```
 *
 * The [content] block is called synchronously to create windows via [Window].
 * After that, a message loop runs until all windows are closed or
 * [ApplicationScope.exitApplication] is called.
 */
@OptIn(ExperimentalStdlibApi::class)
fun application(content: ApplicationScope.() -> Unit) {
    // Declare DPI awareness so Windows does not bitmap-scale the window.
    // Without this, 4K displays render at 96 DPI and stretch, causing blurriness.
    SetProcessDPIAware()

    // Preload ANGLE EGL/GLES DLLs and pre-initialize EGL display/context
    // before window creation to reduce startup latency.
    preloadAngleEgl()

    // Inject our Win32 message-loop-based dispatcher as Dispatchers.Main.
    // This is needed because kotlinx.coroutines doesn't provide a built-in Main
    // dispatcher for mingwX64. See https://github.com/Kotlin/kotlinx.coroutines/issues/4286
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    kotlinx.coroutines.Dispatchers.injectMain(SkikoDispatchers.Main)

    ensureWindowClassRegistered()

    val scope = object : ApplicationScope {
        override fun exitApplication() {
            PostQuitMessage(0)
        }
    }

    // Execute content to create windows
    scope.content()

    // Win32 message loop with integrated coroutine dispatcher.
    // Uses MsgWaitForMultipleObjects to process Win32 messages and coroutine
    // tasks at the same priority, preventing input starvation during animations.
    runMainMessageLoop()
}
