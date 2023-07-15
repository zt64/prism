import xlib.Window

internal data class Client(
    val container: Window,
    val content: Window,
    val header: Window
)