import kotlinx.cinterop.*
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import util.*
import xlib.*

@ExperimentalCli
@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    val parser = ArgParser("prismc")

    val dpy = XOpenDisplay(null)?.pointed ?: error("Cannot open display")

    class Mul : Subcommand("mul", "Multiply a number by two") {
        val number by argument(ArgType.Int, "num", "The number to multiply by two")

        override fun execute() {
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
                            data.longs = listOf(Atom.IPC_LIST_CLIENTS_REQUEST.ordinal.toLong(), number.toLong())
                            serial = 0u
                            format = 32
                            send_event = True
                            display = dpy.ptr
                        }
                    }

                    dpy.sendEvent(dpy.rootWindow, false, SubstructureNotifyMask, event)
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

    class CloseClient : Subcommand("close-client", "Close a client") {
        val clientId by argument(ArgType.Int, "clientId", "The id of the client to close")

        override fun execute() {
            dpy.sendClientMessage(Atom.IPC_CLOSE_CLIENT, false, SubstructureNotifyMask, clientId.toLong())
        }
    }

    class Exit : Subcommand("exit", "Exit prism") {
        override fun execute() {
            println("Exiting...")

            dpy.sendClientMessage(Atom.IPC_EXIT, false, SubstructureNotifyMask)
        }
    }

    parser.subcommands(Mul(), CloseClient(), Exit())
    parser.parse(args)
}