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

package androidx.compose.ui.input.key

import androidx.compose.ui.input.key.Key.Companion.Number

/**
 * Actual implementation of [Key] for mingwX64.
 *
 * @param keyCode an integer code representing the key pressed. Note: This keycode can be used to
 * uniquely identify a hardware key. Uses Windows Virtual Key codes.
 */
actual value class Key(val keyCode: Long) {
    actual companion object {
        /** Unknown key. */
        actual val Unknown = Key(-1)

        /**
         * System Home key.
         *
         * This key is handled by the framework and is never delivered to applications.
         */
        @Deprecated(
            "`Key.Home` was mapped to the keyboard \"Home\" key in error. It is meant to be the" +
                " \"system\" home key on Android, and should never be delivered to applications. " +
                "For the keyboard \"Home\" key use `Key.MoveHome`. For the Android system " +
                "\"Home\" key (unlikely to be needed), use `Key.SystemHome`",
            level = DeprecationLevel.ERROR,
        )
        actual val Home = Key(36) // VK_HOME

        /**
         * System Home key.
         *
         * This key is handled by the framework and is never delivered to applications.
         */
        actual val SystemHome: Key = Key(-1000000207)

        /**
         * Up Arrow Key / Directional Pad Up key.
         *
         * May also be synthesized from trackball motions.
         */
        actual val DirectionUp = Key(38) // VK_UP

        /**
         * Down Arrow Key / Directional Pad Down key.
         *
         * May also be synthesized from trackball motions.
         */
        actual val DirectionDown = Key(40) // VK_DOWN

        /**
         * Left Arrow Key / Directional Pad Left key.
         *
         * May also be synthesized from trackball motions.
         */
        actual val DirectionLeft = Key(37) // VK_LEFT

        /**
         * Right Arrow Key / Directional Pad Right key.
         *
         * May also be synthesized from trackball motions.
         */
        actual val DirectionRight = Key(39) // VK_RIGHT

        /** '0' key. */
        actual val Zero = Key(48) // 0x30

        /** '1' key. */
        actual val One = Key(49) // 0x31

        /** '2' key. */
        actual val Two = Key(50) // 0x32

        /** '3' key. */
        actual val Three = Key(51) // 0x33

        /** '4' key. */
        actual val Four = Key(52) // 0x34

        /** '5' key. */
        actual val Five = Key(53) // 0x35

        /** '6' key. */
        actual val Six = Key(54) // 0x36

        /** '7' key. */
        actual val Seven = Key(55) // 0x37

        /** '8' key. */
        actual val Eight = Key(56) // 0x38

        /** '9' key. */
        actual val Nine = Key(57) // 0x39

        /** '-' key. */
        actual val Minus = Key(189) // VK_OEM_MINUS

        /** '=' key. */
        actual val Equals = Key(187) // VK_OEM_PLUS

        /** 'A' key. */
        actual val A = Key(65) // 0x41

        /** 'B' key. */
        actual val B = Key(66) // 0x42

        /** 'C' key. */
        actual val C = Key(67) // 0x43

        /** 'D' key. */
        actual val D = Key(68) // 0x44

        /** 'E' key. */
        actual val E = Key(69) // 0x45

        /** 'F' key. */
        actual val F = Key(70) // 0x46

        /** 'G' key. */
        actual val G = Key(71) // 0x47

        /** 'H' key. */
        actual val H = Key(72) // 0x48

        /** 'I' key. */
        actual val I = Key(73) // 0x49

        /** 'J' key. */
        actual val J = Key(74) // 0x4A

        /** 'K' key. */
        actual val K = Key(75) // 0x4B

        /** 'L' key. */
        actual val L = Key(76) // 0x4C

        /** 'M' key. */
        actual val M = Key(77) // 0x4D

        /** 'N' key. */
        actual val N = Key(78) // 0x4E

        /** 'O' key. */
        actual val O = Key(79) // 0x4F

        /** 'P' key. */
        actual val P = Key(80) // 0x50

        /** 'Q' key. */
        actual val Q = Key(81) // 0x51

        /** 'R' key. */
        actual val R = Key(82) // 0x52

        /** 'S' key. */
        actual val S = Key(83) // 0x53

        /** 'T' key. */
        actual val T = Key(84) // 0x54

        /** 'U' key. */
        actual val U = Key(85) // 0x55

        /** 'V' key. */
        actual val V = Key(86) // 0x56

        /** 'W' key. */
        actual val W = Key(87) // 0x57

        /** 'X' key. */
        actual val X = Key(88) // 0x58

        /** 'Y' key. */
        actual val Y = Key(89) // 0x59

        /** 'Z' key. */
        actual val Z = Key(90) // 0x5A

        /** ',' key. */
        actual val Comma = Key(188) // VK_OEM_COMMA

        /** '.' key. */
        actual val Period = Key(190) // VK_OEM_PERIOD

        /** Left Alt modifier key. */
        actual val AltLeft = Key(164) // VK_LMENU

        /** Right Alt modifier key. */
        actual val AltRight = Key(165) // VK_RMENU

        /** Left Shift modifier key. */
        actual val ShiftLeft = Key(160) // VK_LSHIFT

        /** Right Shift modifier key. */
        actual val ShiftRight = Key(161) // VK_RSHIFT

        /** Tab key. */
        actual val Tab = Key(9) // VK_TAB

        /** Space key. */
        actual val Spacebar = Key(32) // VK_SPACE

        /** Enter key. */
        actual val Enter = Key(13) // VK_RETURN

        /**
         * Backspace key.
         *
         * Deletes characters before the insertion point, unlike [Delete].
         */
        actual val Backspace = Key(8) // VK_BACK

        /**
         * Delete key.
         *
         * Deletes characters ahead of the insertion point, unlike [Backspace].
         */
        actual val Delete = Key(46) // VK_DELETE

        /** Escape key. */
        actual val Escape = Key(27) // VK_ESCAPE

        /** Left Control modifier key. */
        actual val CtrlLeft = Key(162) // VK_LCONTROL

        /** Right Control modifier key. */
        actual val CtrlRight = Key(163) // VK_RCONTROL

        /** Caps Lock key. */
        actual val CapsLock = Key(20) // VK_CAPITAL

        /** Scroll Lock key. */
        actual val ScrollLock = Key(145) // VK_SCROLL

        /** Left Meta modifier key. */
        actual val MetaLeft = Key(91) // VK_LWIN

        /** Right Meta modifier key. */
        actual val MetaRight = Key(92) // VK_RWIN

        /** System Request / Print Screen key. */
        actual val PrintScreen = Key(44) // VK_SNAPSHOT

        /**
         * Insert key.
         *
         * Toggles insert / overwrite edit mode.
         */
        actual val Insert = Key(45) // VK_INSERT

        /** '`' (backtick) key. */
        actual val Grave = Key(192) // VK_OEM_3

        /** '[' key. */
        actual val LeftBracket = Key(219) // VK_OEM_4

        /** ']' key. */
        actual val RightBracket = Key(221) // VK_OEM_6

        /** '/' key. */
        actual val Slash = Key(191) // VK_OEM_2

        /** '\' key. */
        actual val Backslash = Key(220) // VK_OEM_5

        /** ';' key. */
        actual val Semicolon = Key(186) // VK_OEM_1

        /** Page Up key. */
        actual val PageUp = Key(33) // VK_PRIOR

        /** Page Down key. */
        actual val PageDown = Key(34) // VK_NEXT

        /** F1 key. */
        actual val F1 = Key(112) // VK_F1

        /** F2 key. */
        actual val F2 = Key(113) // VK_F2

        /** F3 key. */
        actual val F3 = Key(114) // VK_F3

        /** F4 key. */
        actual val F4 = Key(115) // VK_F4

        /** F5 key. */
        actual val F5 = Key(116) // VK_F5

        /** F6 key. */
        actual val F6 = Key(117) // VK_F6

        /** F7 key. */
        actual val F7 = Key(118) // VK_F7

        /** F8 key. */
        actual val F8 = Key(119) // VK_F8

        /** F9 key. */
        actual val F9 = Key(120) // VK_F9

        /** F10 key. */
        actual val F10 = Key(121) // VK_F10

        /** F11 key. */
        actual val F11 = Key(122) // VK_F11

        /** F12 key. */
        actual val F12 = Key(123) // VK_F12

        /**
         * Num Lock key.
         *
         * This is the Num Lock key; it is different from [Number].
         * This key alters the behavior of other keys on the numeric keypad.
         */
        actual val NumLock = Key(144) // VK_NUMLOCK

        /** Numeric keypad '0' key. */
        actual val NumPad0 = Key(96) // VK_NUMPAD0

        /** Numeric keypad '1' key. */
        actual val NumPad1 = Key(97) // VK_NUMPAD1

        /** Numeric keypad '2' key. */
        actual val NumPad2 = Key(98) // VK_NUMPAD2

        /** Numeric keypad '3' key. */
        actual val NumPad3 = Key(99) // VK_NUMPAD3

        /** Numeric keypad '4' key. */
        actual val NumPad4 = Key(100) // VK_NUMPAD4

        /** Numeric keypad '5' key. */
        actual val NumPad5 = Key(101) // VK_NUMPAD5

        /** Numeric keypad '6' key. */
        actual val NumPad6 = Key(102) // VK_NUMPAD6

        /** Numeric keypad '7' key. */
        actual val NumPad7 = Key(103) // VK_NUMPAD7

        /** Numeric keypad '8' key. */
        actual val NumPad8 = Key(104) // VK_NUMPAD8

        /** Numeric keypad '9' key. */
        actual val NumPad9 = Key(105) // VK_NUMPAD9

        /** Numeric keypad '/' key (for division). */
        actual val NumPadDivide = Key(111) // VK_DIVIDE

        /** Numeric keypad '*' key (for multiplication). */
        actual val NumPadMultiply = Key(106) // VK_MULTIPLY

        /** Numeric keypad '-' key (for subtraction). */
        actual val NumPadSubtract = Key(109) // VK_SUBTRACT

        /** Numeric keypad '+' key (for addition). */
        actual val NumPadAdd = Key(107) // VK_ADD

        /** Numeric keypad Enter key. */
        actual val NumPadEnter = Key(13) // VK_RETURN (same as Enter on Windows)

        actual val MoveHome = Key(36) // VK_HOME

        actual val MoveEnd = Key(35) // VK_END

        // Unsupported Keys (no Windows VK equivalent)
        actual val SoftLeft = Key(-1000000001)
        actual val SoftRight = Key(-1000000002)
        actual val Back = Key(-1000000003)
        actual val NavigatePrevious = Key(-1000000004)
        actual val NavigateNext = Key(-1000000005)
        actual val NavigateIn = Key(-1000000006)
        actual val NavigateOut = Key(-1000000007)
        actual val SystemNavigationUp = Key(-1000000008)
        actual val SystemNavigationDown = Key(-1000000009)
        actual val SystemNavigationLeft = Key(-1000000010)
        actual val SystemNavigationRight = Key(-1000000011)
        actual val Call = Key(-1000000012)
        actual val EndCall = Key(-1000000013)
        actual val DirectionCenter = Key(-1000000014)
        actual val DirectionUpLeft = Key(-1000000015)
        actual val DirectionDownLeft = Key(-1000000016)
        actual val DirectionUpRight = Key(-1000000017)
        actual val DirectionDownRight = Key(-1000000018)
        actual val VolumeUp = Key(175) // VK_VOLUME_UP
        actual val VolumeDown = Key(174) // VK_VOLUME_DOWN
        actual val Power = Key(-1000000021)
        actual val Camera = Key(-1000000022)
        actual val Clear = Key(12) // VK_CLEAR
        actual val Symbol = Key(-1000000024)
        actual val Browser = Key(172) // VK_BROWSER_HOME
        actual val Envelope = Key(-1000000026)
        actual val Function = Key(-1000000027)
        actual val Break = Key(3) // VK_CANCEL (Ctrl+Break)
        actual val Number = Key(-1000000031)
        actual val HeadsetHook = Key(-1000000032)
        actual val Focus = Key(-1000000033)
        actual val Menu = Key(93) // VK_APPS
        actual val Notification = Key(-1000000035)
        actual val Search = Key(170) // VK_BROWSER_SEARCH
        actual val PictureSymbols = Key(-1000000037)
        actual val SwitchCharset = Key(-1000000038)
        actual val ButtonA = Key(-1000000039)
        actual val ButtonB = Key(-1000000040)
        actual val ButtonC = Key(-1000000041)
        actual val ButtonX = Key(-1000000042)
        actual val ButtonY = Key(-1000000043)
        actual val ButtonZ = Key(-1000000044)
        actual val ButtonL1 = Key(-1000000045)
        actual val ButtonR1 = Key(-1000000046)
        actual val ButtonL2 = Key(-1000000047)
        actual val ButtonR2 = Key(-1000000048)
        actual val ButtonThumbLeft = Key(-1000000049)
        actual val ButtonThumbRight = Key(-1000000050)
        actual val ButtonStart = Key(-1000000051)
        actual val ButtonSelect = Key(-1000000052)
        actual val ButtonMode = Key(-1000000053)
        actual val Button1 = Key(-1000000054)
        actual val Button2 = Key(-1000000055)
        actual val Button3 = Key(-1000000056)
        actual val Button4 = Key(-1000000057)
        actual val Button5 = Key(-1000000058)
        actual val Button6 = Key(-1000000059)
        actual val Button7 = Key(-1000000060)
        actual val Button8 = Key(-1000000061)
        actual val Button9 = Key(-1000000062)
        actual val Button10 = Key(-1000000063)
        actual val Button11 = Key(-1000000064)
        actual val Button12 = Key(-1000000065)
        actual val Button13 = Key(-1000000066)
        actual val Button14 = Key(-1000000067)
        actual val Button15 = Key(-1000000068)
        actual val Button16 = Key(-1000000069)
        actual val Forward = Key(-1000000070)
        actual val MediaPlay = Key(179) // VK_MEDIA_PLAY_PAUSE
        actual val MediaPause = Key(-1000000072)
        actual val MediaPlayPause = Key(179) // VK_MEDIA_PLAY_PAUSE
        actual val MediaStop = Key(178) // VK_MEDIA_STOP
        actual val MediaRecord = Key(-1000000075)
        actual val MediaNext = Key(176) // VK_MEDIA_NEXT_TRACK
        actual val MediaPrevious = Key(177) // VK_MEDIA_PREV_TRACK
        actual val MediaRewind = Key(-1000000078)
        actual val MediaFastForward = Key(-1000000079)
        actual val MediaClose = Key(-1000000080)
        actual val MediaAudioTrack = Key(-1000000081)
        actual val MediaEject = Key(-1000000082)
        actual val MediaTopMenu = Key(-1000000083)
        actual val MediaSkipForward = Key(-1000000084)
        actual val MediaSkipBackward = Key(-1000000085)
        actual val MediaStepForward = Key(-1000000086)
        actual val MediaStepBackward = Key(-1000000087)
        actual val MicrophoneMute = Key(-1000000088)
        actual val VolumeMute = Key(173) // VK_VOLUME_MUTE
        actual val Info = Key(-1000000090)
        actual val ChannelUp = Key(-1000000091)
        actual val ChannelDown = Key(-1000000092)
        actual val ZoomIn = Key(-1000000093)
        actual val ZoomOut = Key(-1000000094)
        actual val Tv = Key(-1000000095)
        actual val Window = Key(-1000000096)
        actual val Guide = Key(-1000000097)
        actual val Dvr = Key(-1000000098)
        actual val Bookmark = Key(-1000000099)
        actual val Captions = Key(-1000000100)
        actual val Settings = Key(-1000000101)
        actual val TvPower = Key(-1000000102)
        actual val TvInput = Key(-1000000103)
        actual val SetTopBoxPower = Key(-1000000104)
        actual val SetTopBoxInput = Key(-1000000105)
        actual val AvReceiverPower = Key(-1000000106)
        actual val AvReceiverInput = Key(-1000000107)
        actual val ProgramRed = Key(-1000000108)
        actual val ProgramGreen = Key(-1000000109)
        actual val ProgramYellow = Key(-1000000110)
        actual val ProgramBlue = Key(-1000000111)
        actual val AppSwitch = Key(-1000000112)
        actual val LanguageSwitch = Key(-1000000113)
        actual val MannerMode = Key(-1000000114)
        actual val Toggle2D3D = Key(-1000000125)
        actual val Contacts = Key(-1000000126)
        actual val Calendar = Key(-1000000127)
        actual val Music = Key(-1000000128)
        actual val Calculator = Key(-1000000129)
        actual val ZenkakuHankaru = Key(-1000000130)
        actual val Eisu = Key(-1000000131)
        actual val Muhenkan = Key(29) // VK_NONCONVERT
        actual val Henkan = Key(28) // VK_CONVERT
        actual val KatakanaHiragana = Key(-1000000134)
        actual val Yen = Key(-1000000135)
        actual val Ro = Key(-1000000136)
        actual val Kana = Key(21) // VK_KANA
        actual val Assist = Key(-1000000138)
        actual val BrightnessDown = Key(-1000000139)
        actual val BrightnessUp = Key(-1000000140)
        actual val Sleep = Key(95) // VK_SLEEP
        actual val WakeUp = Key(-1000000142)
        actual val SoftSleep = Key(-1000000143)
        actual val Pairing = Key(-1000000144)
        actual val LastChannel = Key(-1000000145)
        actual val TvDataService = Key(-1000000146)
        actual val VoiceAssist = Key(-1000000147)
        actual val TvRadioService = Key(-1000000148)
        actual val TvTeletext = Key(-1000000149)
        actual val TvNumberEntry = Key(-1000000150)
        actual val TvTerrestrialAnalog = Key(-1000000151)
        actual val TvTerrestrialDigital = Key(-1000000152)
        actual val TvSatellite = Key(-1000000153)
        actual val TvSatelliteBs = Key(-1000000154)
        actual val TvSatelliteCs = Key(-1000000155)
        actual val TvSatelliteService = Key(-1000000156)
        actual val TvNetwork = Key(-1000000157)
        actual val TvAntennaCable = Key(-1000000158)
        actual val TvInputHdmi1 = Key(-1000000159)
        actual val TvInputHdmi2 = Key(-1000000160)
        actual val TvInputHdmi3 = Key(-1000000161)
        actual val TvInputHdmi4 = Key(-1000000162)
        actual val TvInputComposite1 = Key(-1000000163)
        actual val TvInputComposite2 = Key(-1000000164)
        actual val TvInputComponent1 = Key(-1000000165)
        actual val TvInputComponent2 = Key(-1000000166)
        actual val TvInputVga1 = Key(-1000000167)
        actual val TvAudioDescription = Key(-1000000168)
        actual val TvAudioDescriptionMixingVolumeUp = Key(-1000000169)
        actual val TvAudioDescriptionMixingVolumeDown = Key(-1000000170)
        actual val TvZoomMode = Key(-1000000171)
        actual val TvContentsMenu = Key(-1000000172)
        actual val TvMediaContextMenu = Key(-1000000173)
        actual val TvTimerProgramming = Key(-1000000174)
        actual val StemPrimary = Key(-1000000175)
        actual val Stem1 = Key(-1000000176)
        actual val Stem2 = Key(-1000000177)
        actual val Stem3 = Key(-1000000178)
        actual val AllApps = Key(-1000000179)
        actual val Refresh = Key(168) // VK_BROWSER_REFRESH
        actual val ThumbsUp = Key(-1000000181)
        actual val ThumbsDown = Key(-1000000182)
        actual val ProfileSwitch = Key(-1000000183)
        actual val Help = Key(47) // VK_HELP
        actual val Plus = Key(-1000000185)
        actual val Multiply = Key(-1000000186)
        actual val Pound = Key(-1000000187)
        actual val Cut = Key(-1000000188)
        actual val Copy = Key(-1000000189)
        actual val Paste = Key(-1000000190)
        actual val Apostrophe = Key(222) // VK_OEM_7
        actual val At = Key(192) // VK_OEM_3 (same as Grave on Windows)
        actual val NumPadDot = Key(110) // VK_DECIMAL
        actual val NumPadComma = Key(-1000000194)
        actual val NumPadEquals = Key(-1000000195)
        actual val NumPadLeftParenthesis = Key(-1000000196)
        actual val NumPadRightParenthesis = Key(-1000000197)
        actual val NumPadDirectionUp = Key(-1000000198)
        actual val NumPadDirectionDown = Key(-1000000199)
        actual val NumPadDirectionLeft = Key(-1000000200)
        actual val NumPadDirectionRight = Key(-1000000201)
        actual val NumPadMoveHome = Key(-1000000202)
        actual val NumPadMoveEnd = Key(-1000000203)
        actual val NumPadPageUp = Key(-1000000204)
        actual val NumPadPageDown = Key(-1000000205)
        actual val NumPadInsert = Key(-1000000206)
        actual val NumPadDelete: Key = Key(-1000000208)
    }

    actual override fun toString() = "Key keyCode: $keyCode"
}
