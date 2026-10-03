package io.github.windowslpg.oplus_toolkit.data.model

import androidx.annotation.Keep

@Keep
enum class HardwareCategory(val displayName: String, val nodePath: String) {
    SCREEN("屏幕 (LCD/OLED)", "/proc/devinfo/lcd"),
    RAM("运行内存 (DDR)", "/proc/devinfo/ddr"),
    ROM("闪存存储 (UFS)", "/proc/devinfo/ufs")
}

@Keep
data class HardwareItem(
    val category: HardwareCategory,
    val rawCode: String,
    val vendorName: String,
    val readSuccess: Boolean = true,
    val errorMessage: String? = null,
    val detectionSource: String? = null,
    val warningMessage: String? = null
)
