package dev.zt64.prism

import kotlinx.cinterop.toKString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import platform.posix.getenv
import kotlin.test.Test
import kotlin.test.fail

class PrismTest {
    @Test
    fun test() = runTest {
        println("Display: ${getenv("DISPLAY")?.toKString()}")
        val prism = try {
            Prism(null)
        } catch (e: Exception) {
            fail("Failed to create Prism", e)
        }

        launch(Dispatchers.IO) {
            try {
                prism.start()
            } catch (e: Exception) {
                fail("Failed to start Prism", e)
            }
        }

        delay(4000)

        try {
            prism.close()
        } catch (e: Exception) {
            fail("Failed to close Prism", e)
        }
    }
}