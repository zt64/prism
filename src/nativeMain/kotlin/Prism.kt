import kotlinx.cinterop.*
import kotlinx.coroutines.coroutineScope
import platform.posix.free
import util.*
import xlib.*
import kotlin.system.exitProcess

class Prism(
    private val config: Config,
    private val dpy: CPointer<Display>
) {
    private val clients = mutableListOf<Client>()
    private var motionInfo: MotionInfo? = null
    private val root: Window = dpy.rootWindow

    suspend fun startWM() = coroutineScope {
        val event = nativeHeap.alloc<XEvent>()

        dpy.grabButton(1u, Mod1Mask, root, True, ButtonPressMask, GrabModeAsync, GrabModeAsync)
        dpy.grabButton(3u, Mod1Mask, root, True, ButtonPressMask, GrabModeAsync, GrabModeAsync)

        dpy.selectInput(root, SubstructureRedirectMask or SubstructureNotifyMask)
        dpy.setWindowBackground(root, XBlackPixel(dpy, 0))
        dpy.clearWindow(root)

        println("Starting event loop")
        while (true) {
            dpy.nextEvent(event.ptr)
            when (event.type) {
                MapRequest -> event.xmaprequest.run {
                    memScoped {
                        val attrs = alloc<XWindowAttributes> {
                            dpy.getWindowAttributes(window, ptr)
                        }

                        if (attrs.override_redirect == 1) return@memScoped

                        val frameAttrsPtr = alloc<XSetWindowAttributes> {
                            colormap = attrs.colormap
                            border_pixel = alloc<XftColor> {
                                XftColorAllocName(dpy, attrs.visual, attrs.colormap, config.border.color, ptr)
                            }.pixel
                        }.ptr
                        val container = dpy.createWindow(
                            parent = root,
                            x = attrs.x,
                            y = attrs.y,
                            width = attrs.width,
                            height = attrs.height + config.header.height.toInt(),
                            borderWidth = config.border.width.toInt(),
                            depth = attrs.depth,
                            clazz = InputOutput,
                            visual = attrs.visual,
                            valueMask = CWBackPixel or CWBorderPixel or CWColormap,
                            attributesPtr = frameAttrsPtr
                        )

                        dpy.selectInput(container, ExposureMask or SubstructureNotifyMask or SubstructureRedirectMask or PropertyChangeMask)

                        val header = dpy.createWindow(
                            parent = container,
                            x = 0,
                            y = 0,
                            width = attrs.width,
                            height = config.header.height.toInt(),
                            borderWidth = 0,
                            depth = attrs.depth,
                            clazz = InputOutput,
                            visual = attrs.visual,
                            valueMask = CWBackPixel or CWBorderPixel or CWColormap,
                            attributesPtr = frameAttrsPtr
                        )

                        //                            val titleWindow = dpy.createWindow(
                        //                                parent = header,
                        //                                x = 0,
                        //                                y = 0,
                        //                                width = attrs.width,
                        //                                height = config.header.height.toInt(),
                        //                                borderWidth = 0,
                        //                                depth = attrs.depth,
                        //                                clazz = InputOutput,
                        //                                visual = attrs.visual,
                        //                                valueMask = CWBackPixel or CWBorderPixel or CWColormap,
                        //                                attributesPtr = frameAttrsPtr
                        //                            )

                        dpy.setWindowBackground(header, alloc<XftColor> {
                            XftColorAllocName(dpy, attrs.visual, attrs.colormap, config.header.color, ptr)
                        }.pixel)
                        dpy.reparentWindow(window, container, 0, config.header.height.toInt())
                        dpy.mapWindows(container, window, header)

                        clients.add(Client(this@Prism, container, window, header))

                        val currEvent = alloc<XEvent>()
                        while (dpy.nextEvent(currEvent.ptr) == Success) {
                            if (currEvent.type == Expose) {
                                val fetchedName = alloc<CPointerVar<ByteVar>> {
                                    XFetchName(dpy, window, ptr)
                                }

                                @Suppress("UNCHECKED_CAST")
                                val windowName = (alloc<CPointerVar<UByteVar>> {
                                    val atr = alloc<AtomVar>().ptr
                                    val afr = alloc<IntVar>().ptr
                                    val nr = alloc<ULongVar>().ptr
                                    val bar = alloc<ULongVar>().ptr
                                    dpy.getWindowProperty(
                                        window,
                                        dpy.internAtom("_NET_WM_NAME", false),
                                        0, Long.MAX_VALUE, false, dpy.internAtom("UTF8_STRING"), atr, afr, nr, bar, ptr
                                    )
                                } as CPointerVar<ByteVar>).value?.toKString() ?: fetchedName.value?.toKString() ?: "Unknown name"

                                val draw = XftDrawCreate(dpy, header, attrs.visual, attrs.colormap)
                                val font = XftFontOpenName(dpy, 0, config.header.font)
                                val color = alloc<XftColor> {
                                    XftColorAllocName(dpy, attrs.visual, attrs.colormap, config.header.textColor, ptr)
                                }
                                val extents = alloc<XGlyphInfo> {
                                    XftTextExtentsUtf8(dpy, font, windowName.cstr.ptr.reinterpret(), windowName.length, ptr)
                                }

                                XftDrawStringUtf8(
                                    draw,
                                    color.ptr,
                                    font,
                                    (attrs.width / 2) - (extents.width.toInt() / 2),
                                    (config.header.height.toInt() / 2) + extents.height.toInt() / 2,
                                    windowName.cstr.ptr.reinterpret(),
                                    windowName.length
                                )
                                break
                            }
                        }

                        arrayOf(1u, 3u).forEach { button ->
                            arrayOf(
                                0,
                                Mod2Mask,
                                LockMask,
                                Mod3Mask,
                                Mod2Mask or LockMask,
                                LockMask or Mod3Mask,
                                Mod2Mask or Mod3Mask,
                                Mod2Mask or LockMask or Mod3Mask
                            ).forEach {
                                dpy.grabButton(button, it, container, True, ButtonPressMask or PointerMotionMask, GrabModeAsync, GrabModeAsync)
                            }
                        }

                        dpy.raiseWindow(container)
                        dpy.raiseWindow(window)
                        dpy.raiseWindow(header)
                    }
                }
                UnmapNotify -> event.xunmap.run {
                    val client = clients.find { it.content == window } ?: return@run

                    dpy.unmapWindow(client.container)

                    clients.remove(client)
                }
                DestroyNotify -> event.xdestroywindow.run {
                    val client = clients.find { it.content == window } ?: return@run

                    XDestroyWindow(dpy, client.container)

                    clients.remove(client)
                }
                ConfigureRequest -> event.xconfigurerequest.let { ev ->
                    memScoped {
                        XConfigureWindow(dpy, ev.window, ev.value_mask.toUInt(), alloc<XWindowChanges> {
                            x = ev.x
                            y = ev.y
                            width = ev.width
                            height = ev.height
                            border_width = ev.border_width
                            sibling = ev.above
                            stack_mode = ev.detail
                        }.ptr)
                    }
                }
                KeyPress -> event.xkey.run {
                    if (subwindow != None) dpy.raiseWindow(subwindow)
                }
                ButtonPress -> event.xbutton.run {
                    val client = clients.find { it.container == subwindow || it.header == subwindow } ?: return@run

                    XGrabPointer(
                        dpy,
                        client.container,
                        True,
                        (PointerMotionMask or ButtonReleaseMask).toUInt(),
                        GrabModeAsync,
                        GrabModeAsync,
                        None,
                        None,
                        CurrentTime
                    )

                    XSetInputFocus(dpy, client.content, RevertToPointerRoot, CurrentTime)

                    motionInfo = MotionInfo(nativeHeap.alloc {
                        button = event.xbutton.button
                        x_root = event.xbutton.x_root
                        y_root = event.xbutton.y_root
                    }, nativeHeap.alloc {
                        XGetWindowAttributes(dpy, client.container, ptr)
                    })
                }
                MotionNotify -> if (motionInfo != null) event.xmotion.run {
                    val client = clients.find { it.container == window || it.header == window } ?: return@run

                    while (XCheckTypedEvent(dpy, MotionNotify, event.ptr) == True) Unit

                    val start = motionInfo!!.start
                    val attrs = motionInfo!!.attrs

                    val deltaX = x_root - start.x_root
                    val deltaY = y_root - start.y_root

                    if (start.button == 1u) XMoveWindow(dpy, client.container, attrs.x + deltaX, attrs.y + deltaY)
                    else if (start.button == 3u) {
                        XResizeWindow(dpy, client.container, maxOf(1, (attrs.width + deltaX)).toUInt(), maxOf(1, (attrs.height + deltaY)).toUInt())
                        XResizeWindow(
                            dpy,
                            client.content,
                            maxOf(1, (attrs.width + deltaX)).toUInt(),
                            maxOf(1, attrs.height + deltaY - config.header.height).toUInt()
                        )
                        XResizeWindow(dpy, client.header, maxOf(1, (attrs.width + deltaX)).toUInt(), config.header.height.toUInt())
                    }
                }
                ButtonRelease -> if (motionInfo != null) {
                    free(motionInfo!!.start.ptr)
                    free(motionInfo!!.attrs.ptr)
                    XUngrabPointer(dpy, CurrentTime)
                    motionInfo = null
                }
                ClientMessage -> event.xclient.run {
                    when (Atom.from(data.longs[0].toInt())) {
                        Atom.IPC_CLOSE_CLIENT -> {
                            val client = clients.getOrNull(data.l[1].toInt()) ?: return@run

                            memScoped {
                                alloc<XEvent> {
                                    xclient.apply {
                                        type = ClientMessage
                                        window = client.content
                                        format = 32
                                        message_type = dpy.internAtom("WM_PROTOCOLS")
                                        data.longs = listOf(dpy.internAtom("WM_DELETE_WINDOW").toLong())
                                    }

                                    dpy.sendEvent(client.content, false, NoEventMask, ptr)
                                }
                            }

                            println("Closed client")
                        }
                        Atom.IPC_EXIT -> {
                            XCloseDisplay(dpy)
                            exitProcess(0)
                        }
                        Atom.IPC_LIST_CLIENTS_REQUEST -> memScoped {
                            dpy.sendClientMessage(Atom.IPC_LIST_CLIENTS_RESPONSE, window, false, SubstructureNotifyMask, data.longs[1] * 2)

                            dpy.flush()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}