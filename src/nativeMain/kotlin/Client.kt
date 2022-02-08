import xlib.Window

data class Client(
    private val prism: Prism,

    val container: Window,
    val content: Window,
    val header: Window
)