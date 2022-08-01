import kotlinx.cinterop.*
import platform.posix.free
import util.*
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
    fun startWM()  {
        val event = nativeHeap.alloc<XEvent>()

        dpy.grabButton(
            button = 1u,
            modifiers = Mod1Mask,
            grabWindow = root,
            ownerEvents = True,
            eventMask = ButtonPressMask,
            pointerMode = GrabModeAsync,
            keyboardMode = GrabModeAsync
        )
        dpy.grabButton(
            button = 3u,
            modifiers = Mod1Mask,
            grabWindow = root,
            ownerEvents = True,
            eventMask = ButtonPressMask,
            pointerMode = GrabModeAsync,
            keyboardMode = GrabModeAsync
        )

        dpy.selectInput(root, SubstructureRedirectMask or SubstructureNotifyMask)
        dpy.setWindowBackground(root, XBlackPixel(dpy.ptr, 0))
        dpy.clearWindow(root)

        println("Starting event loop")

        while (true) {
            dpy.nextEvent(event.ptr)
            root.setCursor(dpy, 58u) // https://tronche.com/gui/x/xlib/appendix/b/
            // root.unsetCursor(dpy)         // your DE will replace these cursors with its own
            when (event.type) {              // TODO: When should we set the root window cursor? When should we unset it?
                MapRequest -> event.xmaprequest.run {
                    memScoped {
                        val attrs = dpy.getWindowAttributes(window)

                        if (attrs.override_redirect == 1) return@memScoped

                        val frameAttrsPtr = alloc<XSetWindowAttributes> {
                            colormap = attrs.colormap
                            border_pixel = alloc<XftColor> {
                                XftColorAllocName(dpy.ptr, attrs.visual, attrs.colormap, config.border.color, ptr)
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
                            XftColorAllocName(dpy.ptr, attrs.visual, attrs.colormap, config.header.color, ptr)
                        }.pixel)
                        dpy.reparentWindow(window, container, 0, config.header.height.toInt())
                        dpy.mapWindows(container, window, header)

                        clients.add(Client(this@Prism, container, window, header))

                        val currEvent = alloc<XEvent>()
                        while (dpy.nextEvent(currEvent.ptr) == Success) {
                            if (currEvent.type == Expose) {
                                val fetchedName = alloc<CPointerVar<ByteVar>> {
                                    XFetchName(dpy.ptr, window, ptr)
                                }

                                @Suppress("UNCHECKED_CAST")
                                val windowName = (alloc<CPointerVar<UByteVar>> {
                                    dpy.getWindowProperty(
                                        window = window,
                                        property = dpy.internAtom("_NET_WM_NAME", false),
                                        longOffset = 0,
                                        longLength = Long.MAX_VALUE,
                                        delete = false,
                                        reqType = dpy.internAtom("UTF8_STRING"),
                                        actualType = alloc<AtomVar>().ptr,
                                        actualFormat = alloc<IntVar>().ptr,
                                        nitems = alloc<ULongVar>().ptr,
                                        bytesAfter = alloc<ULongVar>().ptr,
                                        prop = ptr
                                    )
                                } as CPointerVar<ByteVar>).value?.toKString() ?: fetchedName.value?.toKString() ?: "Unknown name"

                                val draw = XftDrawCreate(dpy.ptr, header, attrs.visual, attrs.colormap)
                                val font = XftFontOpenName(dpy.ptr, 0, "${config.header.font}:size=${config.header.fontSize}")
                                val color = alloc<XftColor> {
                                    XftColorAllocName(dpy.ptr, attrs.visual, attrs.colormap, config.header.textColor, ptr)
                                }
                                val extents = alloc<XGlyphInfo> {
                                    XftTextExtentsUtf8(dpy.ptr, font, windowName.cstr.ptr.reinterpret(), windowName.length, ptr)
                                }

                                XftDrawStringUtf8(
                                    draw = draw,
                                    color = color.ptr,
                                    pub = font,
                                    x = (attrs.width / 2) - (extents.width.toInt() / 2),
                                    y = (config.header.height.toInt() / 2) + extents.height.toInt() / 2,
                                    string = windowName.cstr.ptr.reinterpret(),
                                    len = windowName.length
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

                    XDestroyWindow(dpy.ptr, client.container)

                    clients.remove(client)
                }
                ConfigureRequest -> event.xconfigurerequest.let { ev ->
                    memScoped {
                        XConfigureWindow(dpy.ptr, ev.window, ev.value_mask.toUInt(), alloc<XWindowChanges> {
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
                        dpy.ptr,
                        client.container,
                        True,
                        (PointerMotionMask or ButtonReleaseMask).toUInt(),
                        GrabModeAsync,
                        GrabModeAsync,
                        None,
                        None,
                        CurrentTime
                    )

                    XSetInputFocus(dpy.ptr, client.content, RevertToPointerRoot, CurrentTime)

                    motionInfo = MotionInfo(
                        attrs = nativeHeap.alloc<XWindowAttributes> {
                            XGetWindowAttributes(dpy.ptr, client.container, ptr)
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

                    while (XCheckTypedEvent(dpy.ptr, MotionNotify, event.ptr) == True) Unit

                    val start = motionInfo!!.start
                    val attrs = motionInfo!!.attrs

                    val deltaX = x_root - start.x_root
                    val deltaY = y_root - start.y_root

                    if (start.button == 1u) XMoveWindow(dpy.ptr, client.container, attrs.x + deltaX, attrs.y + deltaY)
                    else if (start.button == 3u) {
                        XResizeWindow(dpy.ptr, client.container, maxOf(1, (attrs.width + deltaX)).toUInt(), maxOf(1, (attrs.height + deltaY)).toUInt())
                        XResizeWindow(
                            dpy.ptr,
                            client.content,
                            maxOf(1, (attrs.width + deltaX)).toUInt(),
                            maxOf(1, attrs.height + deltaY - config.header.height).toUInt()
                        )
                        XResizeWindow(dpy.ptr, client.header, maxOf(1, (attrs.width + deltaX)).toUInt(), config.header.height.toUInt())
                    }
                }
                ButtonRelease -> if (motionInfo != null) {
                    free(motionInfo!!.start.ptr)
                    XUngrabPointer(dpy.ptr, CurrentTime)
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
                            XCloseDisplay(dpy.ptr)
                            exitProcess(0)
                        }
                        Atom.IPC_LIST_CLIENTS_REQUEST -> memScoped {
                            dpy.sendClientMessage(Atom.IPC_LIST_CLIENTS_RESPONSE, window, false, SubstructureNotifyMask, data.longs[1] * 2)

                            dpy.flush()
                        }
                        Atom.IPC_ICONIFY_CLIENT -> { }
                        Atom.IPC_MAXIMIZE_CLIENT -> { }
                        Atom.IPC_TOGGLE_CLIENT_FULLSCREEN -> { }
                        Atom.IPC_LIST_CLIENTS_RESPONSE -> { }
                    }
                }
            }
        }
    }
}