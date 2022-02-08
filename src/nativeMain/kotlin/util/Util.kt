package util

import Atom
import kotlinx.cinterop.*
import platform.posix.getenv
import xlib.*

fun getConfigDir(): String? = getenv("XDG_CONFIG_HOME")?.toKString() ?: getenv("HOME")?.toKString()?.plus("/.config")
fun Boolean.toInt(): Int = if (this) 1 else 0

fun CPointer<Display>.sendClientMessage(type: Atom, window: Window, propagate: Boolean = false, eventMask: Long = NoEventMask, vararg data: Long): Unit =
    memScoped {
        alloc<XEvent> {
            xclient.apply {
                this.type = ClientMessage
                this.data.longs = listOf(type.ordinal.toLong(), *data.toTypedArray())
                serial = 0u
                format = 32
                send_event = True
                display = this@sendClientMessage
            }

            sendEvent(window, propagate, eventMask, ptr)
        }

        flush()
    }

fun CPointer<Display>.sendClientMessage(type: Atom, propagate: Boolean = false, eventMask: Long = NoEventMask, vararg data: Long) =
    sendClientMessage(type, rootWindow, propagate, eventMask, *data)