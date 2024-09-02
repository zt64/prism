@file:OptIn(ExperimentalForeignApi::class)

package dev.zt64.prism.util

import dev.zt64.prism.Atom
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import xlib.*

const val CURRENT_TIME = 0UL
const val NONE = 0UL

typealias DisplayPtr = CPointer<Display>

fun Boolean.toInt() = if (this) True else False

inline fun DisplayPtr.sendClientMessage(
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
                display = this@sendClientMessage
            }
        }

        sendEvent(window, propagate, eventMask, event)
    }

    flush()
}

inline fun DisplayPtr.sendClientMessage(
    type: Atom,
    propagate: Boolean = false,
    eventMask: Long = NoEventMask,
    vararg data: Long
) = sendClientMessage(type, rootWindow, propagate, eventMask, *data)