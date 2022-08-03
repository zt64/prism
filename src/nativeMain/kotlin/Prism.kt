
import kotlinx.cinterop.*
import platform.posix.free
import util.*
import util.None
import xlib.*
import kotlin.system.exitProcess

class Prism(
    private val config: Config,
    private val dpy: Display
) {
    private val clients = mutableListOf<Client>()
    private var motionInfo: MotionInfo? = null
    private val root = dpy.rootWindow

    @OptIn(ExperimentalUnsignedTypes::class)
    fun startWM() {
        val event = nativeHeap.alloc<XEvent>()

        XDefineCursor(dpy.ptr, root, XCreateFontCursor(dpy.ptr, XC_left_ptr))

        dpy.selectInput(root, StructureNotifyMask or SubstructureRedirectMask or SubstructureNotifyMask or ButtonPressMask)
        dpy.clearWindow(root)

        println("Starting event loop")

        while (true) {
            dpy.nextEvent(event)

            when (event.type) {
                MapRequest -> event.xmaprequest.run {
                    memScoped {
                        val attrs = dpy.getWindowAttributes(window)

                        if (attrs.override_redirect == 1) return@memScoped

                        val frameAttrs = alloc<XSetWindowAttributes> {
                            colormap = attrs.colormap
                            border_pixel = alloc<XftColor> {
                                XftColorAllocName(dpy.ptr, attrs.visual, attrs.colormap, config.border.color, ptr)
                            }.pixel
                        }
                        val containerWindow = dpy.createWindow(
                            x = attrs.x,
                            y = attrs.y,
                            width = attrs.width,
                            height = (attrs.height + config.header.height).toInt(),
                            borderWidth = config.border.width.toInt(),
                            depth = attrs.depth,
                            clazz = InputOutput,
                            visual = attrs.visual?.pointed,
                            valueMask = CWBackPixel or CWBorderPixel or CWColormap,
                            attributes = alloc<XSetWindowAttributes> {
                                colormap = attrs.colormap
                                border_pixel = alloc<XftColor> {
                                    XftColorAllocName(dpy.ptr, attrs.visual, attrs.colormap, config.border.color, ptr)
                                }.pixel
                            }
                        )

                        dpy.selectInput(containerWindow, ExposureMask or SubstructureNotifyMask or SubstructureRedirectMask or PropertyChangeMask)

                        val headerWindow = dpy.createWindow(
                            parent = containerWindow,
                            width = attrs.width,
                            height = config.header.height.toInt(),
                            borderWidth = 0,
                            depth = attrs.depth,
                            clazz = InputOutput,
                            visual = attrs.visual?.pointed,
                            valueMask = CWBackPixel or CWBorderPixel or CWColormap,
                            attributes = frameAttrs
                        )

                        dpy.setWindowBackground(
                            window = headerWindow,
                            pixel = alloc<XftColor> {
                                XftColorAllocName(dpy.ptr, attrs.visual, attrs.colormap, config.header.color, ptr)
                            }.pixel
                        )
                        dpy.reparentWindow(window, containerWindow, 0, config.header.height.toInt())
                        dpy.mapWindows(containerWindow, window, headerWindow)

                        clients.add(Client(this@Prism, containerWindow, window, headerWindow))

                        val currEvent = alloc<XEvent>()
                        while (dpy.nextEvent(currEvent) == Success) {
                            if (currEvent.type == Expose) {
                                val fetchedName = alloc<CPointerVar<ByteVar>> {
                                    XFetchName(dpy.ptr, window, ptr)
                                }.value?.toKString() ?: alloc<CPointerVar<UByteVar>> {
                                    dpy.getWindowProperty(
                                        window = window,
                                        property = dpy.internAtom("_NET_WM_NAME"),
                                        reqType = dpy.internAtom("UTF8_STRING"),
                                        actualType = alloc(),
                                        actualFormat = alloc(),
                                        nitems = alloc(),
                                        bytesAfter = alloc(),
                                        prop = ptr
                                    )
                                }.pointed?.value?.toString() ?: "Unknown"

                                val draw = XftDrawCreate(dpy.ptr, headerWindow, attrs.visual, attrs.colormap)
                                val font = XftFontOpenName(dpy.ptr, 0, "${config.header.font}:size=${config.header.fontSize}")
                                val color = alloc<XftColor> {
                                    XftColorAllocName(dpy.ptr, attrs.visual, attrs.colormap, config.header.textColor, ptr)
                                }
                                val extents = alloc<XGlyphInfo> {
                                    XftTextExtentsUtf8(dpy.ptr, font, fetchedName.cstr.ptr.reinterpret(), fetchedName.length, ptr)
                                }

                                XftDrawStringUtf8(
                                    draw = draw,
                                    color = color.ptr,
                                    pub = font,
                                    x = (attrs.width / 2) - (extents.width.toInt() / 2),
                                    y = (config.header.height / 2 + extents.height.toInt() / 2).toInt(),
                                    string = fetchedName.cstr.ptr.reinterpret(),
                                    len = fetchedName.length
                                )

                                break
                            }
                        }

//                        dpy.raiseWindows(container, window, header)
                        dpy.selectInput(window, EnterWindowMask or FocusChangeMask or PropertyChangeMask or StructureNotifyMask)
                    }
                }
                UnmapNotify -> event.xunmap.run {
                    val client = clients.find { it.content == window } ?: return@run

                    dpy.unmapWindow(client.container)

                    clients.remove(client)
                }
                DestroyNotify -> event.xdestroywindow.run {
                    val client = clients.find { it.content == window } ?: return@run

                    dpy.destroyWindow(client.container)

                    clients.remove(client)
                }
                ConfigureRequest -> event.xconfigurerequest.let { ev ->
                    memScoped {
                        dpy.configureWindow(
                            window = ev.window,
                            valueMask = ev.value_mask.toUInt(),
                            attributesPtr = alloc<XWindowChanges> {
                                x = ev.x
                                y = ev.y
                                width = ev.width
                                height = ev.height
                                border_width = ev.border_width
                                sibling = ev.above
                                stack_mode = ev.detail
                            }
                        )
                    }
                }
                KeyPress -> event.xkey.run {
                    if (subwindow != None) dpy.raiseWindow(subwindow)
                }
                ButtonPress -> event.xbutton.run {
                    dpy.raiseWindow(subwindow)

                    val client = clients.find { client -> client.container == subwindow || client.header == subwindow } ?: return@run

                    dpy.grabPointer(
                        grabWindow = client.container,
                        ownerEvents = true,
                        eventMask = PointerMotionMask or ButtonReleaseMask
                    )

                    dpy.setInputFocus(client.content, RevertToPointerRoot)

                    motionInfo = MotionInfo(
                        attrs = nativeHeap.alloc<XWindowAttributes> {
                            dpy.getWindowAttributes(client.container, ptr)
                        },
                        start = nativeHeap.alloc<XButtonEvent> {
                            button = event.xbutton.button
                            x_root = event.xbutton.x_root
                            y_root = event.xbutton.y_root
                        }
                    )
                }
                MotionNotify -> if (motionInfo != null) event.xmotion.run {
                    val client = clients.find { it.container == window || it.header == window } ?: return@run

                    while (dpy.checkTypedEvent(MotionNotify, event.ptr)) Unit

                    val start = motionInfo!!.start
                    val attrs = motionInfo!!.attrs

                    val deltaX = x_root - start.x_root
                    val deltaY = y_root - start.y_root

                    when (start.button) {
                        1u -> {
                            dpy.moveWindow(
                                window = client.container,
                                x = attrs.x + deltaX,
                                y = attrs.y + deltaY
                            )
                        }

                        3u -> {
                            dpy.resizeWindow(
                                window = client.container,
                                width = maxOf(1, attrs.width + deltaX),
                                height = maxOf(1, attrs.height + deltaY)
                            )

                            dpy.resizeWindow(
                                window = client.content,
                                width = maxOf(1, attrs.width + deltaX),
                                height = maxOf(1, attrs.height + deltaY - config.header.height).toInt()
                            )

                            dpy.resizeWindow(
                                window = client.header,
                                width = maxOf(1, attrs.width + deltaX),
                                height = config.header.height.toInt()
                            )
                        }
                    }
                }
                ButtonRelease -> if (motionInfo != null) {
                    free(motionInfo!!.start.ptr)
                    dpy.unGrabPointer()
                    motionInfo = null
                }
                ClientMessage -> event.xclient.run {
                    when (Atom.from(data.longs[0].toInt())) {
                        Atom.IPC_CLOSE_CLIENT -> {
                            val client = clients.getOrNull(data.l[1].toInt()) ?: return@run

                            memScoped {
                                val xEvent = alloc<XEvent> {
                                    xclient.apply {
                                        type = ClientMessage
                                        window = client.content
                                        format = 32
                                        message_type = dpy.internAtom("WM_PROTOCOLS")
                                        data.longs = listOf(dpy.internAtom("WM_DELETE_WINDOW").toLong())
                                    }
                                }

                                dpy.sendEvent(client.content, false, NoEventMask, xEvent)
                            }

                            println("Closed client")
                        }

                        Atom.IPC_EXIT -> {
                            dpy.close()
                            exitProcess(0)
                        }

                        Atom.IPC_LIST_CLIENTS_REQUEST -> memScoped {
                            dpy.sendClientMessage(
                                type = Atom.IPC_LIST_CLIENTS_RESPONSE,
                                window = window,
                                false,
                                eventMask = SubstructureNotifyMask,
                                data.longs[1] * 2
                            )

                            dpy.flush()
                        }

                        Atom.IPC_ICONIFY_CLIENT -> {}
                        Atom.IPC_MAXIMIZE_CLIENT -> {}
                        Atom.IPC_TOGGLE_CLIENT_FULLSCREEN -> {}
                        Atom.IPC_LIST_CLIENTS_RESPONSE -> {}
                    }
                }
            }
        }
    }
}