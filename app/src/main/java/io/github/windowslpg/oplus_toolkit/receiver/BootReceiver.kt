package io.github.windowslpg.oplus_toolkit.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.windowslpg.oplus_toolkit.data.root.RootShellExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED") {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val logDir = context.filesDir
                    if (!logDir.exists()) {
                        logDir.mkdirs()
                    }
                    val logFile = File(logDir, "boot_panel.log")
                    val command = "dmesg | grep -iE 'panel|dsi|lcd|tp' > ${logFile.absolutePath} && chmod 666 ${logFile.absolutePath}"
                    RootShellExecutor.executeCommand(command)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
