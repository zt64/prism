import com.akuleshov7.ktoml.file.TomlFileReader
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.serializer
import platform.posix.R_OK
import platform.posix.access
import util.getConfigDir
import xlib.XOpenDisplay

fun main(args: Array<String>) = PrismWM().main(args)

private class PrismWM : CliktCommand() {
    val configPath: String? by option(help = "Path to config file", envvar = "PRISM_CONFIG")

    override fun run(): Unit = runBlocking {
        val config = (configPath ?: getConfigDir()?.plus("/prism/config.toml"))?.run {
            if (access(this, R_OK) == 0) {
                println("Found config file at $this")
                TomlFileReader(ktomlConf).decodeFromFile<Config>(serializer(), this)
            } else {
                println("No config file found, loading defaults")
                null
            }
        } ?: Config()
        val dpy = XOpenDisplay(null) ?: error("Cannot open display")

        Prism(config, dpy).startWM()
    }
}