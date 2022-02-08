import com.akuleshov7.ktoml.KtomlConf

val ktomlConf = KtomlConf(
    // allow/prohibit unknown names during the deserialization, default false
    ignoreUnknownNames = false,
    // allow/prohibit empty values like "a = # comment", default true
    emptyValuesAllowed = true,
    // allow/prohibit escaping of single quotes in literal strings, default true
    escapedQuotesInLiteralStringsAllowed = true
)

val CurrentTime get() = xlib.CurrentTime.toULong()
val None = xlib.None.toULong()