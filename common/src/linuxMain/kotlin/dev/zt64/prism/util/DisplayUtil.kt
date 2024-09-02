@file:OptIn(ExperimentalForeignApi::class)

package dev.zt64.prism.util

import kotlinx.cinterop.*
import xlib.*

val DisplayPtr.rootWindow get() = XDefaultRootWindow(this)

var anonymousStruct2.bytes
    get() = listOf(b[0], b[1], b[2], b[3], b[4])
    set(v) = v.forEachIndexed { index, byte -> b[index] = byte }

var anonymousStruct2.shorts
    get() = listOf(s[0], s[1], s[2], s[3], s[4])
    set(v) = v.forEachIndexed { index, short -> s[index] = short }

var anonymousStruct2.longs
    get() = listOf(l[0], l[1], l[2], l[3], l[4])
    set(v) = v.forEachIndexed { index, long -> l[index] = long }

fun DisplayPtr.reparentWindow(window: Window, parent: Window, x: Int = 0, y: Int = 0): Int {
    return XReparentWindow(this, window, parent, x, y)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun DisplayPtr.mapWindows(vararg windows: Window) = windows.forEach(::mapWindow)

fun DisplayPtr.mapWindow(window: Window) = XMapWindow(this, window)

fun DisplayPtr.unmapWindow(window: Window) = XUnmapWindow(this, window)

fun DisplayPtr.clearWindow(window: Window) = XClearWindow(this, window)

fun DisplayPtr.selectInput(window: Window, vararg mask: Long): Int {
    return XSelectInput(this, window, mask.reduce(Long::or))
}

fun DisplayPtr.createWindow(
    parent: Window = rootWindow,
    x: Int = 0,
    y: Int = 0,
    width: Int,
    height: Int,
    borderWidth: Int,
    depth: Int,
    clazz: Int,
    visual: Visual?,
    valueMask: Long,
    attributes: XSetWindowAttributes?
) = XCreateWindow(
    this,
    parent,
    x,
    y,
    width.toUInt(),
    height.toUInt(),
    borderWidth.toUInt(),
    depth,
    clazz.toUInt(),
    visual?.ptr,
    valueMask.toULong(),
    attributes?.ptr
)

fun DisplayPtr.destroyWindow(window: Window) = XDestroyWindow(this, window)

/**
 * TODO
 *
 * @param window
 * @param valueMask
 * @param attributesPtr
 */
fun DisplayPtr.configureWindow(window: Window, valueMask: UInt, attributesPtr: XWindowChanges?) =
    XConfigureWindow(this, window, valueMask, attributesPtr?.ptr)

/**
 * TODO
 *
 * @param button
 * @param modifiers
 * @param grabWindow
 * @param ownerEvents
 * @param eventMask
 * @param pointerMode
 * @param keyboardMode
 * @param confineTo
 * @param cursor
 */
fun DisplayPtr.grabButton(
    button: UInt,
    modifiers: Int,
    grabWindow: Window,
    ownerEvents: Boolean,
    eventMask: Long,
    pointerMode: Int = GrabModeAsync,
    keyboardMode: Int = GrabModeAsync,
    confineTo: Window? = null,
    cursor: Cursor? = null
) = XGrabButton(
    this,
    button,
    modifiers.toUInt(),
    grabWindow,
    ownerEvents.toInt(),
    eventMask.toUInt(),
    pointerMode,
    keyboardMode,
    confineTo ?: NONE,
    cursor ?: NONE
)

/**
 * TODO
 *
 * @param grabWindow
 * @param ownerEvents
 * @param eventMask
 * @param pointerMode
 * @param keyboardMode
 * @param confineTo
 * @param cursor
 * @param time
 */
fun DisplayPtr.grabPointer(
    grabWindow: Window,
    ownerEvents: Boolean,
    eventMask: Long,
    pointerMode: Int = GrabModeAsync,
    keyboardMode: Int = GrabModeAsync,
    confineTo: Window = NONE,
    cursor: Cursor = NONE,
    time: Time = CURRENT_TIME
) = XGrabPointer(
    this,
    grabWindow,
    ownerEvents.toInt(),
    eventMask.toUInt(),
    pointerMode,
    keyboardMode,
    confineTo,
    cursor,
    time
)

fun DisplayPtr.ungrabPointer(time: Time = CURRENT_TIME) = XUngrabPointer(this, time)

/**
 * TODO
 *
 * @param window
 * @param property
 * @param longOffset
 * @param longLength
 * @param delete
 * @param reqType
 * @param actualType
 * @param actualFormat
 * @param nitems
 * @param bytesAfter
 * @param prop
 */
fun DisplayPtr.getWindowProperty(
    window: Window,
    property: Atom,
    longOffset: Long = 0,
    longLength: Long = Long.MAX_VALUE,
    delete: Boolean = false,
    reqType: Atom,
    actualType: AtomVar? = null,
    actualFormat: IntVar? = null,
    nitems: ULongVar? = null,
    bytesAfter: ULongVar? = null,
    prop: CValuesRef<CPointerVar<UByteVar>>? = null
) = XGetWindowProperty(
    this,
    window,
    property,
    longOffset,
    longLength,
    delete.toInt(),
    reqType,
    actualType?.ptr,
    actualFormat?.ptr,
    nitems?.ptr,
    bytesAfter?.ptr,
    prop
)

fun DisplayPtr.setInputFocus(focus: Window, revertTo: Int, time: Time = CURRENT_TIME) =
    XSetInputFocus(this, focus, revertTo, time)

fun DisplayPtr.getWindowAttributes(window: Window) =
    nativeHeap.alloc<XWindowAttributes> {
        getWindowAttributes(window, ptr)
    }

fun DisplayPtr.getWindowAttributes(window: Window, windowsAttributeReturn: CValuesRef<XWindowAttributes>?) =
    XGetWindowAttributes(this, window, windowsAttributeReturn)

fun DisplayPtr.setWindowBackground(window: Window, pixel: ULong) = XSetWindowBackground(this, window, pixel)

@OptIn(ExperimentalUnsignedTypes::class)
fun DisplayPtr.raiseWindows(vararg window: Window) = window.forEach(::raiseWindow)

fun DisplayPtr.raiseWindow(window: Window) = XRaiseWindow(this, window)

fun DisplayPtr.moveWindow(window: Window, x: Int, y: Int) = XMoveWindow(this, window, x, y)

fun DisplayPtr.resizeWindow(window: Window, width: Int, height: Int): Int {
    return XResizeWindow(this, window, width.toUInt(), height.toUInt())
}

fun DisplayPtr.sendEvent(propagate: Boolean, eventMask: Long, event: XEvent?) =
    sendEvent(rootWindow, propagate, eventMask, event)

fun DisplayPtr.sendEvent(window: Window, propagate: Boolean, eventMask: Long, event: XEvent?) =
    XSendEvent(this, window, propagate.toInt(), eventMask, event?.ptr)

fun DisplayPtr.checkTypedEvent(eventType: Int, eventReturn: CValuesRef<XEvent>?): Boolean {
    return XCheckTypedEvent(this, eventType, eventReturn) == True
}

fun DisplayPtr.nextEvent(event: XEvent?) = XNextEvent(this, event?.ptr)

fun DisplayPtr.flush() = XFlush(this)

fun DisplayPtr.sync(discard: Boolean) = XSync(this, discard.toInt())

fun DisplayPtr.internAtom(name: String, onlyIfExists: Boolean = false) = XInternAtom(this, name, onlyIfExists.toInt())

fun DisplayPtr.close() = XCloseDisplay(this)