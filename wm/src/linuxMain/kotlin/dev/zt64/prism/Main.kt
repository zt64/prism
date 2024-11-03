package dev.zt64.prism

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import platform.posix.system
import xlib.XOpenDisplay

fun main(args: Array<String>) = WmCommand().main(args)

class WmCommand : CliktCommand() {
    private val path by option("-c", "--config", help = "Path to config file")

    override fun run() {
        val config = getConfig(path)

        val dpy = XOpenDisplay(null) ?: return println("Failed to open display")

        runBlocking {
            launch {
                system(config.general.autoStartPath)
            }

            launch {
                Prism(config, dpy).start()
            }
        }

        // runBlocking {
        //     val display = getenv("DISPLAY")?.toKString()
        //     val selectorManager = SelectorManager(Dispatchers.Main)
        //     val socketPath = "/tmp/.X11-unix/X2"
        //     val tcp = aSocket(selectorManager).tcp()
        //     val clientConnection = try {
        //         tcp.connect(UnixSocketAddress(socketPath))
        //     } catch (e: Exception) {
        //         selectorManager.close()
        //         println(e.message)
        //         exitProcess(1)
        //     }
        //
        //     val readChannel = clientConnection.openReadChannel()
        //     val sendChannel = clientConnection.openWriteChannel(autoFlush = true)
        //
        //     val authName = ""
        //     val authData = ""
        //
        //     fun pad(bytes: Int) = (4 - bytes % 4) % 4
        //     fun BytePacketBuilder.writePadding(bytes: Int) {
        //         repeat(pad(bytes)) { writeByte(0) }
        //     }
        //
        //     sendChannel.writePacket {
        //         writeText("B")
        //         writeByte(0)
        //         writeUShort(11u)
        //         writeUShort(0u)
        //         writeUShort(authName.length.toUShort())
        //         writeUShort(authData.length.toUShort())
        //         writeShort(0)
        //         writeText(authName)
        //         writePadding(authName.length)
        //         writeText(authData)
        //         writePadding(authData.length)
        //     }
        //
        //     when (val status = readChannel.readByte().toInt()) {
        //         0 -> {
        //             val lengthOfReason = readChannel.readByte().toInt()
        //
        //             readChannel.discardExact(2) // protocol major version
        //             readChannel.discardExact(2) // protocol minor version
        //             readChannel.discardExact(2) // length
        //
        //             val reason = buildString {
        //                 repeat(lengthOfReason) {
        //                     append(readChannel.readByte().toInt().toChar())
        //                 }
        //             }
        //
        //             println("XServer returned: $reason")
        //
        //             exitProcess(1)
        //         }
        //
        //         1 -> println("Authentication not implemented, continuing")
        //         else -> println("Unknown server reply: $status")
        //     }
        //
        //     readChannel.discardExact(1)
        //     readChannel.discardExact(2) // protocol major version
        //     readChannel.discardExact(2) // protocol minor version
        //     readChannel.discardExact(2) // length
        //
        //     println("Release number: ${readChannel.readInt()}")
        //     println("Resource ID base: ${readChannel.readInt()}")
        //     println("Resource ID mask: ${readChannel.readInt()}")
        //     println("Motion buffer size: ${readChannel.readInt()}")
        //
        //     val lengthOfVendor = readChannel.readShort().toInt()
        //     println("Length of vendor: $lengthOfVendor")
        //     println("Maximum request length: ${readChannel.readShort()}")
        //     val numOfScreens = readChannel.readByte().toInt()
        //     println("Number of screens: $numOfScreens")
        //     val numOfFormats = readChannel.readByte().toInt()
        //     println("Number of formats: $numOfFormats")
        //     println("Image byte order: ${readChannel.readByte()}")
        //     println("Bitmap bit order: ${readChannel.readByte()}")
        //     println("Bitmap scanline unit: ${readChannel.readByte()}")
        //     println("Bitmap scanline pad: ${readChannel.readByte()}")
        //     println("Min key code: ${readChannel.readByte()}")
        //     println("Max key code: ${readChannel.readByte()}")
        //
        //     readChannel.discardExact(4) // unused
        //
        //     val vendor = buildString {
        //         repeat(lengthOfVendor) {
        //             append(readChannel.readByte().toInt().toChar())
        //         }
        //     }
        //
        //     println("Vendor: $vendor")
        //
        //     readChannel.discard(pad(lengthOfVendor).toLong())
        //
        //     val formats = buildList(numOfFormats) {
        //         repeat(numOfFormats) {
        //             add(
        //                 Format(
        //                     depth = readChannel.readByte().toInt(),
        //                     bitsPerPixel = readChannel.readByte().toInt(),
        //                     scanlinePad = readChannel.readByte().toInt()
        //                 )
        //             )
        //
        //             readChannel.discardExact(5)
        //         }
        //     }
        //
        //     lateinit var root: Window
        //
        //     val screens = buildList(numOfScreens) {
        //         repeat(numOfScreens) {
        //             root = Window(readChannel.readInt())
        //             val defaultColormap = readChannel.readInt()
        //             val whitePixel = readChannel.readInt()
        //             val blackPixel = readChannel.readInt()
        //             val currentInputMasks = readChannel.readInt()
        //             val widthInPixels = readChannel.readShort().toInt()
        //             val heightInPixels = readChannel.readShort().toInt()
        //             val widthInMillimeters = readChannel.readShort().toInt()
        //             val heightInMillimeters = readChannel.readShort().toInt()
        //             val minInstalledMaps = readChannel.readShort().toInt()
        //             val maxInstalledMaps = readChannel.readShort().toInt()
        //             val rootVisual = readChannel.readInt()
        //             val backingStores = BackingStore.entries[readChannel.readByte().toInt()]
        //             val saveUnders = readChannel.readByte().toBoolean()
        //             val rootDepth = readChannel.readByte().toInt()
        //             val allowedDepths = readChannel.readByte().toInt()
        //
        //             add(
        //                 Screen(
        //                     root = root,
        //                     defaultColormap = defaultColormap,
        //                     whitePixel = whitePixel,
        //                     blackPixel = blackPixel,
        //                     currentInputMasks = currentInputMasks,
        //                     widthInPixels = widthInPixels,
        //                     heightInPixels = heightInPixels,
        //                     widthInMillimeters = widthInMillimeters,
        //                     heightInMillimeters = heightInMillimeters,
        //                     minInstalledMaps = minInstalledMaps,
        //                     maxInstalledMaps = maxInstalledMaps,
        //                     rootVisual = rootVisual,
        //                     backingStores = backingStores,
        //                     saveUnders = saveUnders,
        //                     rootDepth = rootDepth,
        //                     allowedDepths = buildList(allowedDepths) {
        //                         repeat(allowedDepths) {
        //                             val depth = readChannel.readByte().toInt()
        //                             readChannel.discardExact(1) // unused
        //                             val numOfVisualTypes = readChannel.readShort().toInt()
        //                             readChannel.discardExact(4) // unused
        //
        //                             add(
        //                                 Depth(
        //                                     depth = depth,
        //                                     visuals = buildList(numOfVisualTypes) {
        //                                         repeat(numOfVisualTypes) {
        //                                             add(
        //                                                 Visual(
        //                                                     visualId = readChannel.readInt(),
        //                                                     type = VisualType.entries[readChannel.readByte().toInt()],
        //                                                     bitsPerRgbValue = readChannel.readByte().toInt(),
        //                                                     colormapEntries = readChannel.readShort().toInt(),
        //                                                     redMask = readChannel.readInt(),
        //                                                     greenMask = readChannel.readInt(),
        //                                                     blueMask = readChannel.readInt()
        //                                                 )
        //                                             )
        //
        //                                             readChannel.discardExact(4)
        //                                         }
        //                                     }
        //                                 )
        //                             )
        //                         }
        //                     }
        //                 )
        //             )
        //         }
        //     }
        //
        //     sendChannel.writePacket {
        //         writeByte(110)
        //         writeByte(0)
        //         writeShort(0)
        //     }
        //     readChannel.awaitContent()
        //     println(readChannel.readRemaining())
        // }
    }
}

data class Format(
    val depth: Int,
    val bitsPerPixel: Int,
    val scanlinePad: Int
)