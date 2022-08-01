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

public fun Window.setCursor(dpy: Display, cur: UInt = 2.toUInt()) { 
    var cursorInitialized = windowCursorInitialized[this]
    if (cursorInitialized != true) {
        XDefineCursor(dpy.ptr, this, XCreateFontCursor(dpy.ptr, cur))
        windowCursorInitialized.put(this, true)
    }
}
public fun Window.unsetCursor(dpy: Display) {
    XUndefineCursor(dpy.ptr, this)
    windowCursorInitialized.put(this, false)
}
// TODO: Does this work with window focus?
// https://tronche.com/gui/x/xlib/window/XUndefineCursor.html