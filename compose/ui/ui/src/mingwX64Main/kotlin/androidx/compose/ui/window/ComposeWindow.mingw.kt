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

package androidx.compose.ui.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.input.key.createComposeKeyEvent
import androidx.compose.ui.input.pointer.MingwCursor
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.platform.DefaultArchitectureComponentsOwner
import androidx.compose.ui.platform.MingwTextInputService
import androidx.compose.ui.platform.PlatformContext
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import androidx.compose.ui.platform.WindowInfoImpl
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.text.input.CommitTextCommand
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toDpSize
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.enableSavedStateHandles
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toLong
import kotlinx.cinterop.wcstr
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoRenderDelegate
import platform.windows.CW_USEDEFAULT
import platform.windows.CS_HREDRAW
import platform.windows.CS_VREDRAW
import platform.windows.CreateWindowExW
import platform.windows.DefWindowProcW
import platform.windows.GetModuleHandleW
import platform.windows.HTCLIENT
import platform.windows.HWND
import platform.windows.IDC_ARROW
import platform.windows.LPARAM
import platform.windows.LRESULT
import platform.windows.LoadCursorW
import platform.windows.LoadIconW
import platform.windows.PostQuitMessage
import platform.windows.RegisterClassExW
import platform.windows.ReleaseCapture
import platform.windows.SetCapture
import platform.windows.SW_SHOW
import platform.windows.SetCursor
import platform.windows.ShowWindow
import platform.windows.UINT
import platform.windows.UpdateWindow
import platform.windows.WHITE_BRUSH
import platform.windows.WM_DESTROY
import platform.windows.WM_KEYDOWN
import platform.windows.WM_KEYUP
import platform.windows.WM_KILLFOCUS
import platform.windows.WM_LBUTTONDOWN
import platform.windows.WM_LBUTTONUP
import platform.windows.WM_MBUTTONDOWN
import platform.windows.WM_MBUTTONUP
import platform.windows.WM_MOUSEMOVE
import platform.windows.WM_MOUSEWHEEL
import platform.windows.WM_PAINT
import platform.windows.WM_RBUTTONDOWN
import platform.windows.WM_RBUTTONUP
import platform.windows.WM_SETFOCUS
import platform.windows.WM_MOVE
import platform.windows.WM_SIZE
import platform.windows.WM_SYSKEYDOWN
import platform.windows.WM_SYSKEYUP
import platform.windows.WNDCLASSEXW
import platform.windows.WPARAM
import platform.windows.WS_OVERLAPPEDWINDOW
import platform.windows.WHEEL_DELTA
import platform.windows.WM_MOUSEHWHEEL
import platform.windows.WM_SETCURSOR
import platform.windows.WM_CHAR
import platform.windows.WM_ERASEBKGND
import platform.windows.WM_IME_CHAR
import platform.windows.WM_IME_COMPOSITION
import platform.windows.WM_IME_ENDCOMPOSITION
import platform.windows.WM_IME_STARTCOMPOSITION
import platform.windows.ImmGetContext
import platform.windows.ImmReleaseContext
import platform.windows.ImmSetCompositionWindow
import platform.windows.COMPOSITIONFORM
import platform.windows.CFS_POINT
import platform.windows.POINT
import platform.windows.ScreenToClient

private const val COMPOSE_WINDOW_CLASS = "ComposeWindowClass"
private var windowClassRegistered = false

// Global map from HWND to ComposeWindow for WndProc dispatch
private val windowMap = mutableMapOf<Long, ComposeWindow>()

interface WindowScope {
    val hwnd: HWND
}

fun Window(
    title: String = "ComposeWindow",
    size: DpSize = DpSize(800.dp, 600.dp),
    content: @Composable WindowScope.() -> Unit,
) {
    ComposeWindow(
        title = title,
        size = size,
        content = content,
    )
}

internal fun ensureWindowClassRegistered() {
    if (windowClassRegistered) return
    memScoped {
        val hInstance = GetModuleHandleW(null)
        val wc = alloc<WNDCLASSEXW>()
        wc.cbSize = sizeOf<WNDCLASSEXW>().toUInt()
        wc.style = 0u // Don't use CS_HREDRAW/CS_VREDRAW — they cause full repaint flicker on resize
        wc.lpfnWndProc = staticCFunction(::composeWndProc)
        wc.hInstance = hInstance
        wc.hCursor = LoadCursorW(null, IDC_ARROW)
        // Load application icon from exe resources (resource ID 1, set by windres .rc file).
        // Falls back to default Windows icon if no icon resource is embedded.
        wc.hIcon = LoadIconW(hInstance, 1L.toCPointer())
        wc.hIconSm = LoadIconW(hInstance, 1L.toCPointer())
        wc.hbrBackground = null // No background brush — prevents white flash on resize
        wc.lpszClassName = COMPOSE_WINDOW_CLASS.wcstr.ptr
        RegisterClassExW(wc.ptr)
    }
    windowClassRegistered = true
}

private fun composeWndProc(hwnd: HWND?, msg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT {
    val window = hwnd?.let { windowMap[it.toLong()] }
    if (window != null) {
        val result = window.handleMessage(msg, wParam, lParam)
        if (result != null) return result
    }
    return DefWindowProcW(hwnd, msg, wParam, lParam)
}

internal class ComposeWindow(
    title: String,
    size: DpSize,
    content: @Composable WindowScope.() -> Unit,
) : WindowScope {
    private var isDisposed = false
    private val mingwTextInputService = MingwTextInputService()
    private val _windowInfo = WindowInfoImpl().apply {
        isWindowFocused = true
    }
    private val archComponentsOwner = DefaultArchitectureComponentsOwner(
        enforceMainThread = true
    )

    // Stores the active text input request from BasicTextField for WM_CHAR routing
    private var currentInputRequest: PlatformTextInputMethodRequest? = null
    // Current cursor handle set by Compose, used to restore cursor on WM_SETCURSOR.
    private var currentCursorHandle = LoadCursorW(null, IDC_ARROW)
    // For UTF-16 surrogate pair handling (supplementary CJK characters)
    private var pendingHighSurrogate: Char? = null

    private val platformContext: PlatformContext =
        object : PlatformContext by PlatformContext.Empty() {
            override val windowInfo get() = _windowInfo
            override val architectureComponentsOwner get() = archComponentsOwner
            override val textInputService get() = mingwTextInputService
            override fun setPointerIcon(pointerIcon: PointerIcon) {
                val cursor = (pointerIcon as? MingwCursor)?.let {
                    LoadCursorW(null, it.cursorId.toLong().toCPointer())
                } ?: LoadCursorW(null, IDC_ARROW)
                currentCursorHandle = cursor
                SetCursor(cursor)
            }

            override suspend fun startInputMethod(request: PlatformTextInputMethodRequest): Nothing {
                currentInputRequest = request
                try {
                    awaitCancellation()
                } finally {
                    currentInputRequest = null
                }
            }
        }
    private val skiaLayer = SkiaLayer()
    private val scene = CanvasLayersComposeScene(
        coroutineContext = Dispatchers.Main,
        platformContext = platformContext,
        invalidate = skiaLayer::needRender,
    )
    private val renderDelegate = object : SkikoRenderDelegate {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            val sizeInPx = IntSize(width, height)
            _windowInfo.containerSize = sizeInPx
            _windowInfo.containerDpSize = sizeInPx.toSize().toDpSize(scene.density)
            scene.size = sizeInPx
            scene.render(canvas.asComposeCanvas(), nanoTime)
        }
    }

    override val hwnd: HWND

    init {
        ensureWindowClassRegistered()

        val hInstance = GetModuleHandleW(null)
        // Scale window size by DPI so dp values produce correct physical sizes.
        // Without this, 800x600 dp would be 800x600 physical pixels (tiny on 4K).
        val dpiScale = skiaLayer.systemDpiScale
        val windowWidth = (size.width.value * dpiScale).toInt()
        val windowHeight = (size.height.value * dpiScale).toInt()
        val screenW = platform.windows.GetSystemMetrics(platform.windows.SM_CXSCREEN)
        val screenH = platform.windows.GetSystemMetrics(platform.windows.SM_CYSCREEN)
        val posX = (screenW - windowWidth) / 2
        val posY = (screenH - windowHeight) / 2
        hwnd = CreateWindowExW(
            0u,
            COMPOSE_WINDOW_CLASS,
            title,
            WS_OVERLAPPEDWINDOW.toUInt(),
            posX, posY,
            windowWidth, windowHeight,
            null, null, hInstance, null
        ) ?: error("CreateWindowExW failed")

        windowMap[hwnd.toLong()] = this

        skiaLayer.renderDelegate = renderDelegate
        skiaLayer.attachTo(hwnd)

        // Use systemDpiScale for Compose density (dp→px), not contentScale (which is 1.0
        // because DPI-aware mode makes GetClientRect return physical pixels directly).
        scene.density = Density(skiaLayer.systemDpiScale)
        scene.setContent {
            content()
        }

        archComponentsOwner.enableSavedStateHandles()
        archComponentsOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // Render the first frame before showing the window to avoid white flash.
        skiaLayer.renderImmediately()

        ShowWindow(hwnd, SW_SHOW)
        UpdateWindow(hwnd)
    }

    fun dispose() {
        if (isDisposed) return
        isDisposed = true
        archComponentsOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        archComponentsOwner.viewModelStore.clear()
        skiaLayer.detach()
        scene.close()
        windowMap.remove(hwnd.toLong())
    }

    internal fun handleMessage(msg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT? {
        if (isDisposed) return null
        return when (msg.toInt()) {
            WM_DESTROY -> {
                dispose()
                PostQuitMessage(0)
                0
            }
            WM_ERASEBKGND -> {
                1 // Prevent background erase — eliminates flicker on resize
            }
            WM_SIZE, WM_MOVE -> {
                skiaLayer.renderImmediately()
                0
            }
            WM_PAINT -> {
                skiaLayer.needRender()
                null // Let DefWindowProc validate the region
            }
            WM_SETFOCUS -> {
                _windowInfo.isWindowFocused = true
                0
            }
            WM_KILLFOCUS -> {
                _windowInfo.isWindowFocused = false
                0
            }
            WM_MOUSEMOVE -> {
                onMouseEvent(PointerEventType.Move, lParam)
                0
            }
            WM_LBUTTONDOWN -> {
                SetCapture(hwnd)
                onMouseEvent(PointerEventType.Press, lParam, PointerButton.Primary)
                0
            }
            WM_LBUTTONUP -> {
                ReleaseCapture()
                onMouseEvent(PointerEventType.Release, lParam, PointerButton.Primary)
                0
            }
            WM_RBUTTONDOWN -> {
                SetCapture(hwnd)
                onMouseEvent(PointerEventType.Press, lParam, PointerButton.Secondary)
                0
            }
            WM_RBUTTONUP -> {
                ReleaseCapture()
                onMouseEvent(PointerEventType.Release, lParam, PointerButton.Secondary)
                0
            }
            WM_MBUTTONDOWN -> {
                SetCapture(hwnd)
                onMouseEvent(PointerEventType.Press, lParam, PointerButton.Tertiary)
                0
            }
            WM_MBUTTONUP -> {
                ReleaseCapture()
                onMouseEvent(PointerEventType.Release, lParam, PointerButton.Tertiary)
                0
            }
            WM_MOUSEWHEEL -> {
                val delta = (wParam.toInt() shr 16).toShort().toFloat() / WHEEL_DELTA
                scene.sendPointerEvent(
                    eventType = PointerEventType.Scroll,
                    position = screenLParamToClientOffset(lParam),
                    scrollDelta = Offset(0f, -delta),
                    nativeEvent = null,
                )
                0
            }
            WM_MOUSEHWHEEL -> {
                val delta = (wParam.toInt() shr 16).toShort().toFloat() / WHEEL_DELTA
                scene.sendPointerEvent(
                    eventType = PointerEventType.Scroll,
                    position = screenLParamToClientOffset(lParam),
                    scrollDelta = Offset(delta, 0f),
                    nativeEvent = null,
                )
                0
            }
            WM_CHAR, WM_IME_CHAR -> {
                // Character input from keyboard or IME (including CJK).
                // Route through the active PlatformTextInputMethodRequest if a text field is focused.
                val codeUnit = wParam.toInt()
                if (codeUnit in 0xD800..0xDBFF) {
                    // High surrogate — store and wait for low surrogate
                    pendingHighSurrogate = codeUnit.toChar()
                } else {
                    val text = if (codeUnit in 0xDC00..0xDFFF && pendingHighSurrogate != null) {
                        // Surrogate pair → supplementary character
                        charArrayOf(pendingHighSurrogate!!, codeUnit.toChar()).concatToString().also {
                            pendingHighSurrogate = null
                        }
                    } else {
                        pendingHighSurrogate = null
                        if (codeUnit >= 0x20 || codeUnit == 0x09) {
                            codeUnit.toChar().toString()
                        } else null
                    }
                    if (text != null) {
                        currentInputRequest?.onEditCommand?.invoke(
                            listOf(CommitTextCommand(text, 1))
                        )
                    }
                }
                0
            }
            WM_IME_STARTCOMPOSITION -> {
                // Position the IME candidate window near the focused text field
                positionImeWindow()
                null // Let DefWindowProc handle the rest
            }
            WM_IME_COMPOSITION, WM_IME_ENDCOMPOSITION -> {
                null // Let the default IME handler process composition
            }
            WM_KEYDOWN, WM_SYSKEYDOWN -> {
                // When a text field is active, skip printable character keys — they'll
                // arrive as WM_CHAR instead. Only send control/navigation keys.
                val vk = wParam.toInt()
                if (currentInputRequest != null && isPrintableVk(vk)) {
                    0 // Skip — WM_CHAR will handle character insertion
                } else {
                    val event = createComposeKeyEvent(msg, wParam)
                    scene.sendKeyEvent(event)
                    0
                }
            }
            WM_KEYUP, WM_SYSKEYUP -> {
                val vk = wParam.toInt()
                if (currentInputRequest != null && isPrintableVk(vk)) {
                    0
                } else {
                    val event = createComposeKeyEvent(msg, wParam)
                    scene.sendKeyEvent(event)
                    0
                }
            }
            WM_SETCURSOR -> {
                if ((lParam.toInt() and 0xFFFF) == HTCLIENT) {
                    // Restore Compose-managed cursor when entering client area
                    // (e.g. after showing resize cursor at window border)
                    SetCursor(currentCursorHandle)
                    1
                } else {
                    null
                }
            }
            else -> null
        }
    }

    private fun onMouseEvent(
        eventType: PointerEventType,
        lParam: LPARAM,
        button: PointerButton? = null,
    ) {
        if (isDisposed) return
        scene.sendPointerEvent(
            eventType = eventType,
            position = lParamToOffset(lParam),
            nativeEvent = null,
            button = button,
        )
    }

    private fun lParamToOffset(lParam: LPARAM): Offset {
        val x = (lParam.toInt() and 0xFFFF).toShort().toFloat()
        val y = ((lParam.toInt() shr 16) and 0xFFFF).toShort().toFloat()
        return Offset(x, y)
    }

    /**
     * Convert screen coordinates from WM_MOUSEWHEEL/WM_MOUSEHWHEEL lParam to client coordinates.
     * Unlike WM_MOUSEMOVE, wheel messages provide screen-relative coordinates per Win32 docs.
     */
    private fun screenLParamToClientOffset(lParam: LPARAM): Offset {
        val screenX = (lParam.toInt() and 0xFFFF).toShort().toInt()
        val screenY = ((lParam.toInt() shr 16) and 0xFFFF).toShort().toInt()
        memScoped {
            val pt = alloc<POINT>()
            pt.x = screenX
            pt.y = screenY
            ScreenToClient(hwnd, pt.ptr)
            return Offset(pt.x.toFloat(), pt.y.toFloat())
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private fun positionImeWindow() {
        val request = currentInputRequest ?: return
        val rect = request.focusedRectInRoot() ?: return
        val himc = ImmGetContext(hwnd) ?: return
        memScoped {
            val cf = alloc<COMPOSITIONFORM>()
            cf.dwStyle = CFS_POINT.toUInt()
            cf.ptCurrentPos.x = rect.left.toInt()
            cf.ptCurrentPos.y = rect.bottom.toInt()
            ImmSetCompositionWindow(himc, cf.ptr)
        }
        ImmReleaseContext(hwnd, himc)
    }

    /**
     * Returns true if the virtual key code corresponds to a printable character
     * (letters, digits, OEM keys). These should be handled by WM_CHAR, not WM_KEYDOWN,
     * when a text field is active.
     */
    private fun isPrintableVk(vk: Int): Boolean {
        return vk in 0x30..0x39       // 0-9
            || vk in 0x41..0x5A       // A-Z
            || vk in 0xBA..0xC0       // OEM ;=,-./`
            || vk in 0xDB..0xDF       // OEM [\]'
            || vk in 0x60..0x6F       // Numpad 0-9, *, +, -, ., /
            || vk == 0x20             // Space
        }
}
