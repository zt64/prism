package dev.zt64.prism

import kotlinx.cinterop.toKString
import platform.posix.getenv
import kotlin.test.Test

class PrismTest {
    @Test
    fun test() {
        Prism(getenv("DISPLAY")?.toKString() ?: error("DISPLAY not set"))
    }
}