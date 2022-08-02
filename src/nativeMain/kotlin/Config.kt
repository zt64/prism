
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val general: General = General(),
    val header: Header = Header(),
    val border: Border = Border()
)

@Serializable
data class General(
    val autoStartPath: String = "~/.config/prism/autostart",
)

@Serializable
data class Header(
    val enabled: Boolean = true,
    val color: String = "#ffffff",
    val textColor: String = "#000000",
    val font: String = "DejaVu Sans Mono:size=12",
    val fontSize: Long = 12,
    val height: Long = 30,
    val items: List<Item> = emptyList()
)

@Serializable
data class Item(
    val type: String = "",
    val primaryClick: String = "",
    val onSecondaryClick: String = "",
    val onMiddleClick: String = "",
)

@Serializable
data class Border(
    val enabled: Boolean = true,
    val activeColor: String = "#000000",
    val inactiveColor: String = "#000000",
    val color: String = "#ffffff",
    val width: Long = 1
)