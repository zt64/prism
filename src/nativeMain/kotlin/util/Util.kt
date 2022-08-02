package util

import Atom
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.posix.getenv
import xlib.*

val CurrentTime get() = xlib.CurrentTime.toULong()
val None = xlib.None.toULong()

fun getConfigDir() = getenv("XDG_CONFIG_HOME")?.toKString() ?: getenv("HOME")?.toKString()?.plus("/.config")
fun Boolean.toInt() = if (this) True else False

fun Display.sendClientMessage(
    type: Atom,
    window: Window = rootWindow,
    propagate: Boolean = false,
    eventMask: Long = NoEventMask,
    vararg data: Long
) {
    memScoped {
        val event = alloc<XEvent> {
            xclient.apply {
                this.type = ClientMessage
                this.data.longs = listOf(type.ordinal.toLong(), *data.toTypedArray())
                serial = 0u
                format = 32
                send_event = True
                display = this@sendClientMessage.ptr
            }
        }

        sendEvent(window, propagate, eventMask, event)
    }

    flush()
}

fun Display.sendClientMessage(
    type: Atom,
    propagate: Boolean = false,
    eventMask: Long = NoEventMask,
    vararg data: Long
) = sendClientMessage(type, rootWindow, propagate, eventMask, *data)