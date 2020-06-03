package moe.shizuku.manager

import moe.shizuku.manager.adb.AdbClient
import org.junit.Test
import java.io.ByteArrayOutputStream

class AdbTest {

    private val host = "192.168.0.133"
    private val port = 41013

    @Test
    fun runUname() {
        val bos = ByteArrayOutputStream()
        AdbClient(host, port).run {
            connect()
            shellCommand("uname") {
                print(String(it))
                bos.write(it)
            }
            close()
        }
    }

    @Test
    fun runStartShizuku() {
        val bos = ByteArrayOutputStream()
        AdbClient(host, port).run {
            connect()
            shellCommand("sh /data/user_de/0/moe.shizuku.privileged.api/start.sh") {
                print(String(it))
                bos.write(it)
            }
            close()
        }
    }
}