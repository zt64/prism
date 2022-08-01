package util

import None
import kotlinx.cinterop.*
import xlib.*

fun Display.reparentWindow(window: Window, parent: Window, x: Int, y: Int) = XReparentWindow(ptr, window, parent, x, y)
fun Display.mapWindow(window: Window) = XMapWindow(ptr, window)
@OptIn(ExperimentalUnsignedTypes::class)
fun Display.mapWindows(vararg windows: Window) = windows.forEach(::mapWindow)
fun Display.unmapWindow(window: Window) = XUnmapWindow(ptr, window)
fun Display.clearWindow(window: Window) = XClearWindow(ptr, window)
fun Display.selectInput(window: Window, mask: Long) = XSelectInput(ptr, window, mask)
fun Display.createWindow(
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
    ptr, parent, x, y, width.toUInt(), height.toUInt(), borderWidth.toUInt(), depth, clazz.toUInt(), visual, valueMask.toULong(), attributesPtr
)

fun Display.grabButton(
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
    ptr, button, modifiers.toUInt(), grabWindow, ownerEvents, eventMask.toUInt(), pointerMode, keyboardMode, confineTo ?: None, cursor ?: None
)

fun Display.getWindowProperty(
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
    ptr, window, property, longOffset, longLength, delete.toInt(), reqType, actualType, actualFormat, nitems, bytesAfter, prop
)

fun Display.getWindowAttributes(window: Window): XWindowAttributes {
    return nativeHeap.alloc<XWindowAttributes> {
        XGetWindowAttributes(this@getWindowAttributes.ptr, window, ptr)
    }
}
fun Display.setWindowBackground(window: Window, pixel: ULong) = XSetWindowBackground(ptr, window, pixel)
fun Display.raiseWindow(window: Window) = XRaiseWindow(ptr, window)
fun Display.sendEvent(
    window: Window,
    propagate: Boolean,
    eventMask: Long,
    eventPtr: CValuesRef<XEvent>?
) = XSendEvent(ptr, window, propagate.toInt(), eventMask, eventPtr)
fun Display.nextEvent(eventPtr: CValuesRef<XEvent>?) = XNextEvent(ptr, eventPtr)

fun Display.flush() = XFlush(ptr)
fun Display.sync(discard: Boolean) = XSync(ptr, discard.toInt())
fun Display.internAtom(name: String, onlyIfExists: Boolean = false) = XInternAtom(ptr, name, onlyIfExists.toInt())

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