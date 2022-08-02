
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.file.TomlFileReader
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.cinterop.pointed
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.serializer
import platform.posix.R_OK
import platform.posix.access
import platform.posix.system
import util.getConfigDir
import xlib.XOpenDisplay

fun main(args: Array<String>): Unit = PrismWM().main(args)

private class PrismWM : CliktCommand() {
    val configPath: String? by option(help = "Path to config file", envvar = "PRISM_CONFIG")

    override fun run() {
        val ktomlConf = TomlInputConfig(
            ignoreUnknownNames = true,
            allowEmptyValues = true,
            allowEscapedQuotesInLiteralStrings = true,
        )

        val config = (configPath ?: getConfigDir()?.plus("/prism/config.toml"))?.run {
            if (access(this, R_OK) == 0) {
                echo("Found config file at $this")
                TomlFileReader(ktomlConf).decodeFromFile<Config>(serializer(), this)
            } else {
                echo("No config file found, loading defaults")
                null
            }
        } ?: Config()
        val dpy = XOpenDisplay(null)?.pointed ?: return echo("Failed to open display", err = true)

        runBlocking {
            launch {
                system(config.general.autoStartPath)
            }

            launch {
                Prism(config, dpy).startWM()
            }
        }
    }
}