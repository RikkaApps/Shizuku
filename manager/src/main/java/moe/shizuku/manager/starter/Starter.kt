package moe.shizuku.manager.starter

import moe.shizuku.manager.application
import moe.shizuku.manager.utils.UserHandleCompat
import java.io.File

object Starter {

    private val starterFile = File(application.applicationInfo.nativeLibraryDir, "libshizuku.so")

    val userCommand: String = starterFile.absolutePath

    val adbCommand: String
        get() {
            val user = UserHandleCompat.myUserId()
            return if (user == 0) "adb shell $userCommand" else "adb shell $userCommand --user=$user"
        }

    val internalCommand: String
        get() {
            val user = UserHandleCompat.myUserId()
            return "$userCommand --apk=${application.applicationInfo.sourceDir} --user=$user"
        }
}
