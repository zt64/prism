package util

import Atom
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.posix.getenv
import xlib.*

fun getConfigDir(): String? = getenv("XDG_CONFIG_HOME")?.toKString() ?: getenv("HOME")?.toKString()?.plus("/.config")
fun Boolean.toInt(): Int = if (this) 1 else 0
var windowCursors: MutableMap<Window, Cursor> = mutableMapOf()
var windowCursorInitialized: MutableMap<Window, Boolean> = mutableMapOf()

fun Display.sendClientMessage(type: Atom, window: Window, propagate: Boolean = false, eventMask: Long = NoEventMask, vararg data: Long) {
    memScoped {
        alloc<XEvent> {
            xclient.apply {
                this.type = ClientMessage
                this.data.longs = listOf(type.ordinal.toLong(), *data.toTypedArray())
                serial = 0u
                format = 32
                send_event = True
                display = this@sendClientMessage.ptr
            }

            sendEvent(window, propagate, eventMask, ptr)
        }

        flush()
    }
}

fun Display.sendClientMessage(type: Atom, propagate: Boolean = false, eventMask: Long = NoEventMask, vararg data: Long) =
    sendClientMessage(type, rootWindow, propagate, eventMask, *data)

fun Window.setCursor(dpy: Display, cur: UInt = 2.toUInt(), window: Window = this) { 
    var cursorInitialized = windowCursorInitialized[window]
    if (cursorInitialized != true) {
        windowCursors.put(window, XCreateFontCursor(dpy.ptr, cur))
        XDefineCursor(dpy.ptr, window, windowCursors[window]!!)
        windowCursorInitialized.put(window, true)
    }
}
fun Window.unsetCursor(dpy: Display, window: Window = this) {
    XUndefineCursor(dpy.ptr, window)
    windowCursorInitialized.put(window, false)
}
// TODO: Does this work with window focus?
// Should these be part of Window if they can be used on windows other than themselves?
// Or should `window` be removed and replaced with `this`?
// https://tronche.com/gui/x/xlib/window/XUndefineCursor.html