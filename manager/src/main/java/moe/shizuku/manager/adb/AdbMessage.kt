package moe.shizuku.manager.adb

import moe.shizuku.manager.adb.AdbProtocol.A_AUTH
import moe.shizuku.manager.adb.AdbProtocol.A_CLSE
import moe.shizuku.manager.adb.AdbProtocol.A_CNXN
import moe.shizuku.manager.adb.AdbProtocol.A_OKAY
import moe.shizuku.manager.adb.AdbProtocol.A_OPEN
import moe.shizuku.manager.adb.AdbProtocol.A_STLS
import moe.shizuku.manager.adb.AdbProtocol.A_SYNC
import moe.shizuku.manager.adb.AdbProtocol.A_WRTE
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AdbMessage(
        val command: Int,
        val arg0: Int,
        val arg1: Int,
        val data_length: Int,
        val data_crc32: Int,
        val magic: Int,
        val data: ByteArray?
) {

    constructor(command: Int, arg0: Int, arg1: Int, data: String) : this(
            command,
            arg0,
            arg1,
            "$data\u0000".toByteArray())

    constructor(command: Int, arg0: Int, arg1: Int, data: ByteArray?) : this(
            command,
            arg0,
            arg1,
            data?.size ?: 0,
            crc32(data),
            (command.toLong() xor 0xFFFFFFFF).toInt(),
            data)

    fun validate(): Boolean {
        if (command != magic xor -0x1) return false
        if (data_length != 0 && crc32(data) != data_crc32) return false
        return true
    }

    fun validateOrThrow() {
        if (!validate()) throw IllegalArgumentException("bad message ${this.toStringShort()}")
    }

    fun toByteArray(): ByteArray {
        val length = HEADER_LENGTH + (data?.size ?: 0)
        return ByteBuffer.allocate(length).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            putInt(command)
            putInt(arg0)
            putInt(arg1)
            putInt(data_length)
            putInt(data_crc32)
            putInt(magic)
            if (data != null) {
                put(data)
            }
        }.array()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AdbMessage

        if (command != other.command) return false
        if (arg0 != other.arg0) return false
        if (arg1 != other.arg1) return false
        if (data_length != other.data_length) return false
        if (data_crc32 != other.data_crc32) return false
        if (magic != other.magic) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = command
        result = 31 * result + arg0
        result = 31 * result + arg1
        result = 31 * result + data_length
        result = 31 * result + data_crc32
        result = 31 * result + magic
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "AdbMessage(${toStringShort()})"
    }

    fun toStringShort(): String {
        val commandString = when (command) {
            A_SYNC -> "A_SYNC"
            A_CNXN -> "A_CNXN"
            A_AUTH -> "A_AUTH"
            A_OPEN -> "A_OPEN"
            A_OKAY -> "A_OKAY"
            A_CLSE -> "A_CLSE"
            A_WRTE -> "A_WRTE"
            A_STLS -> "A_STLS"
            else -> command.toString()
        }
        return "command=$commandString, arg0=$arg0, arg1=$arg1, data_length=$data_length, data_crc32=$data_crc32, magic=$magic, data=${data?.contentToString()}"
    }

    companion object {

        const val HEADER_LENGTH = 24


        private fun crc32(data: ByteArray?): Int {
            if (data == null) return 0
            var res = 0
            for (b in data) {
                if (b >= 0)
                    res += b
                else
                    res += b + 256
            }
            return res
        }
    }
}
