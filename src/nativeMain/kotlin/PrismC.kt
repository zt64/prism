import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import util.*
import xlib.*

private lateinit var dpy: Display

@Suppress("Unused")
fun prismClient(args: Array<String>): Unit = PrismClient().subcommands(Exit(), Reload(), SR(), CloseClient()).main(args)

private class PrismClient : CliktCommand(name = "prismc") {
    init {
        dpy = XOpenDisplay(null)?.pointed ?: error("Cannot open display")
    }

    override fun run() = Unit
}

private class Reload : CliktCommand(help = "Reload config") {
    override fun run() = TODO("Add reload ability")
}

private class SR : CliktCommand(help = "Send a client message") {
    private val num by argument("num", help = "The number to multiply by 2").int()

    override fun run() = runBlocking {
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
                        data.longs = listOf(Atom.IPC_LIST_CLIENTS_REQUEST.ordinal.toLong(), num.toLong())
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
                    if (event.type == ClientMessage && event.xclient.data.l[0] == Atom.IPC_LIST_CLIENTS_RESPONSE.ordinal.toLong()) {
                        println(event.xclient.data.l[1])
                        break
                    }
                }
            }
        }
    }
}

private class Exit : CliktCommand(help = "Exit prism") {
    override fun run() {
        println("Exiting...")

        dpy.sendClientMessage(Atom.IPC_EXIT, false, SubstructureNotifyMask)
    }
}

private class CloseClient : CliktCommand(help = "Close a client") {
    val clientId by argument(name = "id", help = "The id of the client to close").int()

    override fun run() = dpy.sendClientMessage(Atom.IPC_CLOSE_CLIENT, false, SubstructureNotifyMask, clientId.toLong())
}