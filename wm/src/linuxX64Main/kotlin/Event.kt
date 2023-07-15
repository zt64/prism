import io.ktor.utils.io.core.*

sealed interface Event {
    val code: Short
}

class KeyPress(
    override val code: Short = 2,

    val root: Window,
    val event: Window,
    val child: Window,
    val rootX: Int,
    val rootY: Int,
    val eventX: Int,
    val eventY: Int,
    val isSameScreen: Boolean
) : Event

sealed interface Request {
    val opcode: Byte
    val requestLength: Short
}

class Bell(private val percent: Byte) : Request {
    override val opcode: Byte = 104
    override val requestLength: Short = 1

    internal val packet = buildPacket {
        writeByte(opcode)
        writeByte(percent)
        writeShort(requestLength)
    }
}

class ListProperties(private val window: Window) : Request {
    override val opcode: Byte = 21
    override val requestLength: Short = 2

    internal val packet = buildPacket {
        writeByte(opcode)
        writeByte(0) // unused
        writeShort(requestLength)
        writeInt(window.id)
    }
}