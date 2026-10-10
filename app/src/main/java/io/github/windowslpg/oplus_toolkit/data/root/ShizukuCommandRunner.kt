package io.github.windowslpg.oplus_toolkit.data.root

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuCommandRunner {
    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            isAvailable() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermission(requestCode: Int = 0) {
        try {
            if (!Shizuku.isPreV11() && Shizuku.getVersion() >= 11) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun executeCommand(command: String): String = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext "Permission denied"

        try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            ).apply { isAccessible = true }

            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val builder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                builder.append(line).append("\n")
            }
            process.waitFor()
            builder.toString().trim()
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.message}"
        }
    }

    fun executeStream(command: String): Process? {
        if (!hasPermission()) return null
        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            ).apply { isAccessible = true }

            method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
