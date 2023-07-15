import kotlinx.serialization.Serializable

@Serializable
internal data class Config(
    val general: General = General(),
    val header: Header = Header(),
    val border: Border = Border()
) {
    @Serializable
    internal data class General(
        val autoStartPath: String = "~/.config/prism/autostart"
    )

    @Serializable
    internal data class Header(
        val enabled: Boolean = true,
        val color: String = "#ffffff",
        val textColor: String = "#000000",
        val font: String = "DejaVu Sans Mono:size=12",
        val fontSize: Int = 12,
        val height: Int = 30,
        val items: List<Item> = emptyList()
    )

    @Serializable
    internal data class Item(
        val type: String = "",
        val primaryClick: String = "",
        val onSecondaryClick: String = "",
        val onMiddleClick: String = ""
    )

    @Serializable
    internal data class Border(
        val enabled: Boolean = true,
        val activeColor: String = "#000000",
        val inactiveColor: String = "#000000",
        val color: String = "#ffffff",
        val width: Int = 1
    )
}