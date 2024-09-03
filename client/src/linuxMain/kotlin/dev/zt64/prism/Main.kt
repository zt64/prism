package dev.zt64.prism

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int
import dev.zt64.prism.util.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import xlib.*

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    val dpy = XOpenDisplay(null) ?: error("Cannot open display")

    class RunPrism : CliktCommand() {
        override fun run() {
            memScoped {
                val dummyWindow = dpy.createWindow(
                    width = 1,
                    height = 1,
                    borderWidth = 0,
                    depth = 0,
                    clazz = InputOutput,
                    visual = alloc(),
                    valueMask = 0L,
                    attributes = alloc<XSetWindowAttributes> {
                        override_redirect = True
                    }
                )

                dpy.selectInput(dummyWindow, SubstructureNotifyMask)

                memScoped {
                    val event = alloc<XEvent> {
                        xclient.apply {
                            type = ClientMessage
                            window = dummyWindow
                            data.longs = listOf(Atom.IPC_LIST_CLIENTS_REQUEST.ordinal.toLong())
                            serial = 0u
                            format = 32
                            send_event = True
                            display = dpy
                        }
                    }

                    dpy.sendEvent(false, SubstructureNotifyMask, event)
                    dpy.flush()
                }

                memScoped {
                    val event = alloc<XEvent>()

                    while (dpy.nextEvent(event) == Success) {
                        if (
                            event.type == ClientMessage &&
                            event.xclient.data.l[0] == Atom.IPC_LIST_CLIENTS_RESPONSE.ordinal.toLong()
                        ) {
                            println(event.xclient.data.l[1])
                            break
                        }
                    }
                }
            }
        }
    }

    class CloseClient : CliktCommand("Close a client") {
        private val clientId by argument("clientId", "The id of the client to close").int()

        override fun run() {
            dpy.sendClientMessage(Atom.IPC_CLOSE_CLIENT, false, SubstructureNotifyMask, clientId.toLong())
        }
    }

    class Exit : CliktCommand("Exit prism") {
        override fun run() {
            println("Exiting...")

            dpy.sendClientMessage(Atom.IPC_EXIT, false, SubstructureNotifyMask)
        }
    }

    NoOpCliktCommand().subcommands(
        RunPrism(),
        CloseClient(),
        Exit()
    ).main(args)
}