@file:OptIn(ExperimentalForeignApi::class)

package util

import kotlinx.cinterop.*
import xlib.*

val Display.rootWindow get() = XDefaultRootWindow(ptr)

var anonymousStruct2.bytes
    get() = listOf(b[0], b[1], b[2], b[3], b[4])
    set(v) = v.forEachIndexed { index, byte -> b[index] = byte }

var anonymousStruct2.shorts
    get() = listOf(s[0], s[1], s[2], s[3], s[4])
    set(v) = v.forEachIndexed { index, short -> s[index] = short }

var anonymousStruct2.longs
    get() = listOf(l[0], l[1], l[2], l[3], l[4])
    set(v) = v.forEachIndexed { index, long -> l[index] = long }

fun Display.reparentWindow(window: Window, parent: Window, x: Int = 0, y: Int = 0) = XReparentWindow(ptr, window, parent, x, y)

@OptIn(ExperimentalUnsignedTypes::class)
fun Display.mapWindows(vararg windows: Window) = windows.forEach(::mapWindow)
fun Display.mapWindow(window: Window) = XMapWindow(ptr, window)
fun Display.unmapWindow(window: Window) = XUnmapWindow(ptr, window)

fun Display.clearWindow(window: Window) = XClearWindow(ptr, window)
fun Display.selectInput(window: Window, mask: Long) = XSelectInput(ptr, window, mask)
fun Display.createWindow(
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
    ptr,
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

fun Display.destroyWindow(window: Window) = XDestroyWindow(ptr, window)

/**
 * TODO
 *
 * @param window
 * @param valueMask
 * @param attributesPtr
 */
fun Display.configureWindow(
    window: Window,
    valueMask: UInt,
    attributesPtr: XWindowChanges?
) = XConfigureWindow(ptr, window, valueMask, attributesPtr?.ptr)

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
fun Display.grabButton(
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
    ptr, button, modifiers.toUInt(), grabWindow, ownerEvents.toInt(), eventMask.toUInt(), pointerMode, keyboardMode, confineTo ?: NONE, cursor ?: NONE
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
fun Display.grabPointer(
    grabWindow: Window,
    ownerEvents: Boolean,
    eventMask: Long,
    pointerMode: Int = GrabModeAsync,
    keyboardMode: Int = GrabModeAsync,
    confineTo: Window = NONE,
    cursor: Cursor = NONE,
    time: Time = CURRENT_TIME
) = XGrabPointer(ptr, grabWindow, ownerEvents.toInt(), eventMask.toUInt(), pointerMode, keyboardMode, confineTo, cursor, time)
fun Display.unGrabPointer(time: Time = CURRENT_TIME) = XUngrabPointer(ptr, time)

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
fun Display.getWindowProperty(
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
    ptr,
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

fun Display.setInputFocus(
    focus: Window,
    revertTo: Int,
    time: Time = CURRENT_TIME
) = XSetInputFocus(ptr, focus, revertTo, time)

fun Display.getWindowAttributes(window: Window) = nativeHeap.alloc<XWindowAttributes> {
    getWindowAttributes(window, ptr)
}
fun Display.getWindowAttributes(
    window: Window,
    windowsAttributeReturn: CValuesRef<XWindowAttributes>?
) = XGetWindowAttributes(ptr, window, windowsAttributeReturn)

fun Display.setWindowBackground(window: Window, pixel: ULong) = XSetWindowBackground(ptr, window, pixel)

@OptIn(ExperimentalUnsignedTypes::class)
fun Display.raiseWindows(vararg window: Window) = window.forEach(::raiseWindow)
fun Display.raiseWindow(window: Window) = XRaiseWindow(ptr, window)

fun Display.moveWindow(window: Window, x: Int, y: Int) = XMoveWindow(ptr, window, x, y)
fun Display.resizeWindow(window: Window, width: Int, height: Int) = XResizeWindow(ptr, window, width.toUInt(), height.toUInt())

fun Display.sendEvent(
    window: Window,
    propagate: Boolean,
    eventMask: Long,
    event: XEvent?
) = XSendEvent(ptr, window, propagate.toInt(), eventMask, event?.ptr)

fun Display.checkTypedEvent(eventType: Int, eventReturn: CValuesRef<XEvent>?) = XCheckTypedEvent(ptr, eventType, eventReturn) == True
fun Display.nextEvent(event: XEvent?) = XNextEvent(ptr, event?.ptr)

fun Display.flush() = XFlush(ptr)
fun Display.sync(discard: Boolean) = XSync(ptr, discard.toInt())
fun Display.internAtom(name: String, onlyIfExists: Boolean = false) = XInternAtom(ptr, name, onlyIfExists.toInt())
fun Display.close() = XCloseDisplay(ptr)