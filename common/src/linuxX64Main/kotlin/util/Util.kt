@file:OptIn(ExperimentalForeignApi::class)

package util

import Atom
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import xlib.*

const val CURRENT_TIME = 0UL
const val NONE = 0UL

fun Boolean.toInt() = if (this) True else False

inline fun Display.sendClientMessage(
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

inline fun Display.sendClientMessage(
    type: Atom,
    propagate: Boolean = false,
    eventMask: Long = NoEventMask,
    vararg data: Long
) = sendClientMessage(type, rootWindow, propagate, eventMask, *data)