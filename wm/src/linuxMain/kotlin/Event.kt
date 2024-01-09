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
    val opcode: OpCode
    val requestLength: Short
}

class Bell(private val percent: Byte) : Request {
    override val opcode: OpCode = OpCode.BELL
    override val requestLength: Short = 1

    internal val packet = buildPacket {
        writeByte(opcode.ordinal.toByte())
        writeByte(percent)
        writeShort(requestLength)
    }
}

class ListProperties(private val window: Window) : Request {
    override val opcode: OpCode = OpCode.LIST_PROPERTIES
    override val requestLength: Short = 2

    internal val packet = buildPacket {
        writeByte(opcode.ordinal.toByte())
        writeByte(0) // unused
        writeShort(requestLength)
        writeInt(window.id)
    }
}