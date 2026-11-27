package io.github.windowslpg.oplus_toolkit.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.text.DecimalFormat
import kotlin.math.roundToInt

data class DeviceInfo(
    val model: String = "加载中...",
    val soc: String = "检测中...",
    val ramStorage: String = "读取中...",
    val osVersion: String = "加载中...",
    val buildId: String = "加载中...",
    val ram: String = "读取中...",
    val storage: String = "读取中..."
) {
    companion object {
        val EMPTY = DeviceInfo(
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            soc = "检测中...",
            ramStorage = "检测中...",
            osVersion = "Android ${Build.VERSION.RELEASE}",
            buildId = Build.DISPLAY,
            ram = "检测中...",
            storage = "检测中..."
        )
    }
}

object DeviceInfoHelper {

    suspend fun getDeviceInfoAsync(context: Context): DeviceInfo = withContext(Dispatchers.IO) {
        getDeviceInfo(context)
    }

    fun getDeviceInfo(context: Context): DeviceInfo {
        val ram = getTotalRam(context)
        val storage = getStorageInfo()
        return DeviceInfo(
            model = getDeviceModel(),
            soc = getSocInfo(),
            ramStorage = "$ram + $storage",
            osVersion = getOsVersion(),
            buildId = getBuildVersion(),
            ram = ram,
            storage = storage
        )
    }

    private fun getSocInfo(): String {
        val socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL.trim()
        } else {
            ""
        }.ifBlank { getSystemProperty("ro.soc.model") }
            .ifBlank { getSystemProperty("ro.vendor.qti.soc_name") }
            .ifBlank { getSystemProperty("ro.vendor.qti.soc_model") }
            .ifBlank { getSystemProperty("ro.board.platform") }
            .ifBlank { Build.HARDWARE }

        val platform = getSystemProperty("ro.board.platform")
            .ifBlank { Build.HARDWARE }
            .lowercase()

        val socUpper = socModel.uppercase()
        val commercialName = when {
            socUpper.contains("SM8750") || platform == "sun" -> "高通骁龙 8 至尊版"
            socUpper.contains("SM8650") || platform == "pineapple" -> "高通骁龙 8 Gen 3"
            socUpper.contains("SM8635") || platform == "lanai" -> "高通骁龙 8s Gen 3"
            socUpper.contains("SM8550") || platform == "kalama" -> "高通骁龙 8 Gen 2"
            socUpper.contains("SM8475") || platform == "cape" -> "高通骁龙 8+ Gen 1"
            socUpper.contains("SM8450") || platform == "taro" -> "高通骁龙 8 Gen 1"
            socUpper.contains("SM7675") || platform == "cliffs" -> "高通骁龙 7+ Gen 3"
            socUpper.contains("SM7550") || platform == "crow" -> "高通骁龙 7 Gen 3"
            socUpper.contains("SM7475") -> "高通骁龙 7+ Gen 2"
            socUpper.contains("SM8350") || platform == "lahaina" -> "高通骁龙 888"
            socUpper.contains("SM8250") || platform == "kona" -> "高通骁龙 865"
            socUpper.contains("SM8150") || platform == "msmnile" -> "高通骁龙 855"
            socUpper.contains("SM7325") -> "高通骁龙 778G"
            socUpper.contains("SM7250") || platform == "lito" -> "高通骁龙 765G"
            socUpper.contains("MT6991") -> "联发科天玑 9400"
            socUpper.contains("MT6989") -> "联发科天玑 9300"
            socUpper.contains("MT6985") -> "联发科天玑 9200"
            socUpper.contains("MT6983") -> "联发科天玑 9000"
            socUpper.contains("MT6897") -> "联发科天玑 8300"
            socUpper.contains("MT6895") -> "联发科天玑 8100"
            socUpper.contains("MT6893") -> "联发科天玑 1200"
            socUpper.contains("MT6877") -> "联发科天玑 7050"
            socUpper.contains("MT6833") -> "联发科天玑 700"
            else -> null
        }

        return when {
            commercialName != null && socModel.isNotBlank() && !commercialName.contains(socModel, ignoreCase = true) -> {
                "$commercialName ($socModel)"
            }
            commercialName != null -> commercialName
            socModel.isNotBlank() -> when {
                socUpper.startsWith("SM") -> "高通骁龙 $socModel"
                socUpper.startsWith("MT") -> "联发科 $socModel"
                else -> socModel
            }
            platform.isNotBlank() -> platform
            else -> "未知 SoC"
        }
    }

    private fun getDeviceModel(): String {
        val marketName = getSystemProperty("ro.oplus.market.name")
            .ifBlank { getSystemProperty("ro.vendor.oplus.market.name") }
            .ifBlank { getSystemProperty("ro.product.model.nick") }

        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL

        return when {
            marketName.isNotBlank() && !model.contains(marketName, ignoreCase = true) -> {
                "$manufacturer $model $marketName"
            }
            marketName.isNotBlank() -> {
                "$manufacturer $model"
            }
            else -> {
                "$manufacturer $model"
            }
        }.trim()
    }

    private fun getOsVersion(): String {
        val androidRelease = Build.VERSION.RELEASE
        val oplusRom = getSystemProperty("ro.build.version.oplusrom")
            .ifBlank { getSystemProperty("ro.rom.different.version") }

        val baseInfo = "Android $androidRelease"
        return if (oplusRom.isNotBlank()) {
            val romTag = when {
                oplusRom.startsWith("ColorOS", ignoreCase = true) -> oplusRom
                oplusRom.startsWith("V", ignoreCase = true) -> "ColorOS $oplusRom"
                else -> "ColorOS V$oplusRom"
            }
            "$baseInfo · $romTag"
        } else {
            baseInfo
        }
    }

    private fun getBuildVersion(): String {
        return Build.DISPLAY.ifBlank { Build.ID }
    }

    private fun getTotalRam(context: Context): String {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            val totalBytes = memInfo.totalMem
            val totalGb = totalBytes.toDouble() / (1024 * 1024 * 1024)
            val nominalGb = when {
                totalGb <= 4.2 -> 4
                totalGb <= 6.2 -> 6
                totalGb <= 8.3 -> 8
                totalGb <= 12.3 -> 12
                totalGb <= 16.3 -> 16
                totalGb <= 24.3 -> 24
                else -> totalGb.roundToInt()
            }
            "${nominalGb} GB"
        } catch (_: Exception) {
            getRamFromProcMeminfo()
        }
    }

    private fun getRamFromProcMeminfo(): String {
        return try {
            val reader = BufferedReader(FileReader("/proc/meminfo"))
            val firstLine = reader.readLine()
            reader.close()
            if (firstLine != null) {
                val parts = firstLine.split("\\s+".toRegex())
                if (parts.size >= 2) {
                    val kb = parts[1].toLong()
                    val gb = kb.toDouble() / (1024 * 1024)
                    return "${gb.roundToInt()} GB"
                }
            }
            "未知运存"
        } catch (_: Exception) {
            "未知运存"
        }
    }

    private fun getStorageInfo(): String {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize

            val totalGbDecimal = totalBytes.toDouble() / (1000.0 * 1000 * 1000)
            val nominalGb = when {
                totalGbDecimal <= 70 -> 64
                totalGbDecimal <= 135 -> 128
                totalGbDecimal <= 270 -> 256
                totalGbDecimal <= 540 -> 512
                totalGbDecimal <= 1080 -> 1024
                else -> totalGbDecimal.roundToInt()
            }

            val availGb = availableBytes.toDouble() / (1024.0 * 1024 * 1024)
            val df = DecimalFormat("#.#")
            val nominalStr = if (nominalGb >= 1024) "${nominalGb / 1024} TB" else "$nominalGb GB"
            val availStr = if (availGb >= 1) "${df.format(availGb)} GB" else "${(availGb * 1024).roundToInt()} MB"

            "$nominalStr (可用 $availStr)"
        } catch (_: Exception) {
            "未知存储"
        }
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val getMethod = clazz.getMethod("get", String::class.java, String::class.java)
            (getMethod.invoke(null, key, "") as? String)?.trim().orEmpty()
        } catch (_: Exception) {
            ""
        }
    }
}
