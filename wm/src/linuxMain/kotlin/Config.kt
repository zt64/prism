import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.file.TomlFileReader
import kotlinx.cinterop.toKString
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import platform.posix.R_OK
import platform.posix.access
import platform.posix.getenv

@Serializable
data class Config(
    val general: General = General(),
    val header: Header = Header(),
    val border: Border = Border()
) {
    @Serializable
    data class General(
        val autoStartPath: String = "~/.config/prism/autostart"
    )

    @Serializable
    data class Header(
        val enabled: Boolean = true,
        val color: String = "#ffffff",
        val textColor: String = "#000000",
        val font: String = "DejaVu Sans Mono:size=12",
        val fontSize: Int = 12,
        val height: Int = 30,
        val items: List<Item> = emptyList()
    )

    @Serializable
    data class Item(
        val type: String = "",
        val primaryClick: String = "",
        val onSecondaryClick: String = "",
        val onMiddleClick: String = ""
    )

    @Serializable
    data class Border(
        val enabled: Boolean = true,
        val activeColor: String = "#000000",
        val inactiveColor: String = "#000000",
        val color: String = "#ffffff",
        val width: Int = 1
    )
}

private val ktomlConf = TomlInputConfig(
    ignoreUnknownNames = true,
    allowEmptyValues = true,
    allowEscapedQuotesInLiteralStrings = true
)

fun getConfig(path: String? = null): Config {
    val configDir = getenv("XDG_CONFIG_HOME")?.toKString()
        ?: getenv("HOME")?.toKString()?.plus("/.config")

    val path = (path ?: configDir?.plus("/prism/config.toml"))?.takeIf {
        access(it, R_OK) == 0
    }

    return if (path != null) {
        println("Found config file at $path")
        TomlFileReader(ktomlConf).decodeFromFile(serializer(), path)
    } else {
        println("No config file found, loading defaults")
        Config()
    }
}