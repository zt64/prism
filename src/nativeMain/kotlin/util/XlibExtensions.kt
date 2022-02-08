package util

import None
import kotlinx.cinterop.*
import xlib.*

fun CPointer<Display>.reparentWindow(window: Window, parent: Window, x: Int, y: Int) = XReparentWindow(this, window, parent, x, y)
fun CPointer<Display>.mapWindow(window: Window) = XMapWindow(this, window)
fun CPointer<Display>.mapWindows(vararg windows: Window) = windows.forEach { XMapWindow(this, it) }
fun CPointer<Display>.unmapWindow(window: Window) = XUnmapWindow(this, window)
fun CPointer<Display>.clearWindow(window: Window) = XClearWindow(this, window)
fun CPointer<Display>.selectInput(window: Window, mask: Long) = XSelectInput(this, window, mask)
fun CPointer<Display>.createWindow(
    parent: Window,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    borderWidth: Int,
    depth: Int,
    clazz: Int,
    visual: CValuesRef<Visual>?,
    valueMask: Long,
    attributesPtr: CValuesRef<XSetWindowAttributes>?
) = XCreateWindow(
    this, parent, x, y, width.toUInt(), height.toUInt(), borderWidth.toUInt(), depth, clazz.toUInt(), visual, valueMask.toULong(), attributesPtr
)

fun CPointer<Display>.grabButton(
    button: UInt,
    modifiers: Int,
    grabWindow: Window,
    ownerEvents: Int,
    eventMask: Long,
    pointerMode: Int,
    keyboardMode: Int,
    confineTo: Window? = null,
    cursor: Cursor? = null,
) = XGrabButton(
    this, button, modifiers.toUInt(), grabWindow, ownerEvents, eventMask.toUInt(), pointerMode, keyboardMode, confineTo ?: None, cursor ?: None
)

fun CPointer<Display>.getWindowProperty(
    window: Window,
    property: Atom,
    longOffset: Long,
    longLength: Long,
    delete: Boolean,
    reqType: Atom,
    actualType: CPointer<AtomVar>?,
    actualFormat: CPointer<IntVar>?,
    nitems: CPointer<ULongVar>?,
    bytesAfter: CPointer<ULongVar>?,
    prop:  CValuesRef<CPointerVar<UByteVar>>?
) = XGetWindowProperty(
    this, window, property, longOffset, longLength, delete.toInt(), reqType, actualType, actualFormat, nitems, bytesAfter, prop
)

fun CPointer<Display>.getWindowAttributes(window: Window, pointer: CValuesRef<XWindowAttributes>?) = XGetWindowAttributes(this, window, pointer)
fun CPointer<Display>.setWindowBackground(window: Window, pixel: ULong) = XSetWindowBackground(this, window, pixel)
fun CPointer<Display>.raiseWindow(window: Window) = XRaiseWindow(this, window)
fun CPointer<Display>.sendEvent(
    window: Window,
    propagate: Boolean,
    eventMask: Long,
    eventPtr: CValuesRef<XEvent>?
) = XSendEvent(this, window, propagate.toInt(), eventMask, eventPtr)
fun CPointer<Display>.nextEvent(eventPtr: CValuesRef<XEvent>?) = XNextEvent(this, eventPtr)

fun CPointer<Display>.flush() = XFlush(this)
fun CPointer<Display>.sync(discard: Boolean) = XSync(this, discard.toInt())
fun CPointer<Display>.internAtom(name: String, onlyIfExists: Boolean = false) = XInternAtom(this, name, onlyIfExists.toInt())

val CPointer<Display>.rootWindow
    get() = XDefaultRootWindow(this)

var anonymousStruct2.bytes
    get() = listOf(b[0], b[1], b[2], b[3], b[4])
    set(v) = v.forEachIndexed { index, byte -> b[index] = byte }

var anonymousStruct2.shorts
    get() = listOf(s[0], s[1], s[2], s[3], s[4])
    set(v) = v.forEachIndexed { index, short -> s[index] = short }

var anonymousStruct2.longs
    get() = listOf(l[0], l[1], l[2], l[3], l[4])
    set(v) = v.forEachIndexed { index, long -> l[index] = long }