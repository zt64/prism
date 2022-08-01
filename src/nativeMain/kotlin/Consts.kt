import com.akuleshov7.ktoml.TomlInputConfig

val ktomlConf = TomlInputConfig(
    ignoreUnknownNames = true,
    allowEmptyValues = true,
    allowEscapedQuotesInLiteralStrings = true,
)

val CurrentTime get() = xlib.CurrentTime.toULong()
val None = xlib.None.toULong()