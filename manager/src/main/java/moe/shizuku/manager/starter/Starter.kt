package moe.shizuku.manager.starter

import android.annotation.SuppressLint
import android.content.Context
import android.os.UserManager
import android.system.ErrnoException
import android.system.Os
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.shizuku.api.ShizukuApiConstants
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ktx.createDeviceProtectedStorageContextCompat
import moe.shizuku.manager.ktx.logd
import rikka.core.os.FileUtils
import rikka.core.util.BuildUtils
import java.io.*
import java.util.*

object Starter {

    private val DEX_NAME = String.format(Locale.ENGLISH, "server-v%d.dex", ShizukuApiConstants.SERVER_VERSION)

    private var _command: String? = null

    val command get() = _command!!

    val commandAdb: String
        get() = "adb shell $command"

    fun writeFilesAsync(context: Context, force: Boolean = false) {
        GlobalScope.launch(Dispatchers.IO) { writeFiles(context.applicationContext, force) }
    }

    fun writeFiles(context: Context, force: Boolean = false) {
        if (!force && _command != null) {
            logd("already written")
            return
        }

        try {
            val out = getRoot(context)
            try {
                Os.chmod(out.absolutePath, 457)
            } catch (e: ErrnoException) {
                e.printStackTrace()
            }
            val dexPath = copyDex(context, "server.dex", File(out, DEX_NAME))
            _command = "sh " + writeShellFile(context, File(out, "start.sh"), dexPath)
            writeLegacyCompatAdbShellFile(context)
            logd(_command!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getRoot(context: Context): File {
        return context.createDeviceProtectedStorageContextCompat().filesDir.parentFile!!
    }

    @Throws(IOException::class)
    private fun copyDex(context: Context, source: String, out: File): String {
        if (out.exists() && !BuildConfig.DEBUG) {
            return out.absolutePath
        }
        val `is` = context.assets.open(source)
        val os: OutputStream = FileOutputStream(out)
        FileUtils.copy(`is`, os)
        os.flush()
        os.close()
        `is`.close()
        try {
            Os.chmod(out.absolutePath, 420)
        } catch (e: ErrnoException) {
            e.printStackTrace()
        }
        return out.absolutePath
    }

    @Throws(IOException::class)
    private fun writeShellFile(context: Context, out: File, dex: String): String {
        if (!out.exists()) {
            out.createNewFile()
        }
        val `is` = BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.start)))
        val os = PrintWriter(FileWriter(out))
        var line: String?
        while (`is`.readLine().also { line = it } != null) {
            os.println(line!!
                    .replace("%%%STARTER_PATH%%%", getLibPath(context, "libshizuku.so"))
                    .replace("%%%STARTER_PARAM%%%", getStarterParam(dex))
                    //.replace("%%%LIBRARY_PATH%%%", getLibPath(context, "libhelper.so"))
            )
        }
        os.flush()
        os.close()
        try {
            Os.chmod(out.absolutePath, 420)
        } catch (e: ErrnoException) {
            e.printStackTrace()
        }
        return out.absolutePath
    }

    @SuppressLint("NewApi")
    @Throws(IOException::class)
    private fun writeLegacyCompatAdbShellFile(context: Context) {
        val um = context.getSystemService(UserManager::class.java)
        if (um == null || !BuildUtils.atLeast24 || !um.isUserUnlocked) {
            return
        }
        val parent = context.getExternalFilesDir(null) ?: return
        val out = File(parent, "start.sh")
        val os = PrintWriter(FileWriter(out))
        os.println("#!/system/bin/sh")
        os.println("echo \"↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ PLEASE READ ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓\"")
        os.println("echo \"The start script has been moved to /data, this compatibility script will be eventually removed.\"")
        os.println("echo \"Open Shizuku app to view the new command.\"")
        os.println("echo \"↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ PLEASE READ ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑\"")
        os.println("sleep 10")
        os.println(command)
        os.flush()
        os.close()
    }

    private fun getStarterParam(dex: String): String {
        Objects.requireNonNull(dex)
        return ("--path=" + dex
                + if (ShizukuSettings.isKeepSuContext()) "" else " --use-shell-context")
    }

    private fun getLibPath(context: Context, name: String): String {
        val path = context.applicationInfo.publicSourceDir
        val sourceDir = File(path)
        val libDir = File(sourceDir.parentFile, "lib").listFiles()!![0]
        val starter = File(libDir, name)
        return starter.absolutePath
    }
}