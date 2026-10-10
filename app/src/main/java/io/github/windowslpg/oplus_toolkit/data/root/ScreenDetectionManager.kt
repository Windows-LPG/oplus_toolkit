package io.github.windowslpg.oplus_toolkit.data.root

import android.content.Context
import androidx.annotation.Keep
import io.github.windowslpg.oplus_toolkit.data.model.RuleDictionary
import io.github.windowslpg.oplus_toolkit.data.repository.HardwareParserEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Keep
enum class ScreenDetectionSource(val displayName: String) {
    CMDLINE("内核引导参数 (/proc/cmdline)"),
    TP_NODE("TP 节点 (/proc/devinfo/tp)"),
    LCD_NODE("LCD 节点 (/proc/devinfo/lcd)"),
    NONE("未能识别")
}

@Keep
data class ScreenDetectionResult(
    val rawCode: String,
    val parsedVendor: String,
    val source: ScreenDetectionSource,
    val nodePath: String
)

object ScreenDetectionManager {

    suspend fun detectScreen(context: Context, rules: RuleDictionary): ScreenDetectionResult = withContext(Dispatchers.IO) {
        // 1. 第一优先级（最高准度）：使用 Root 命令安全读取 /proc/cmdline
        val cmdline = RootShellExecutor.executeCommand("cat /proc/cmdline")
        if (isValidContent(cmdline)) {
            val cmdlineVendorResult = parseCmdline(cmdline, rules)
            if (cmdlineVendorResult != null) {
                return@withContext ScreenDetectionResult(
                    rawCode = cmdlineVendorResult.rawSnippet,
                    parsedVendor = cmdlineVendorResult.vendorName,
                    source = ScreenDetectionSource.CMDLINE,
                    nodePath = "/proc/cmdline"
                )
            }
        }

        // 2. 第二优先级：使用 Root 命令读取 /proc/devinfo/tp 节点并做包含匹配
        val tpContent = RootShellExecutor.executeCommand("cat /proc/devinfo/tp")
        if (isValidContent(tpContent)) {
            val vendor = parseTpNodeContent(tpContent, rules)
            if (vendor != null) {
                return@withContext ScreenDetectionResult(
                    rawCode = tpContent,
                    parsedVendor = vendor,
                    source = ScreenDetectionSource.TP_NODE,
                    nodePath = "/proc/devinfo/tp"
                )
            }
        }

        // 3. 第三优先级：兜底使用 Root 命令读取 /proc/devinfo/lcd 节点 + 本地 JSON 字典
        val lcdContent = RootShellExecutor.executeCommand("cat /proc/devinfo/lcd")
        if (isValidContent(lcdContent)) {
            val vendor = HardwareParserEngine.parseScreen(lcdContent, rules)
            return@withContext ScreenDetectionResult(
                rawCode = lcdContent,
                parsedVendor = vendor,
                source = ScreenDetectionSource.LCD_NODE,
                nodePath = "/proc/devinfo/lcd"
            )
        }

        // 4. 默认兜底
        return@withContext ScreenDetectionResult(
            rawCode = "未能读取到屏幕参数",
            parsedVendor = "未识别到屏幕模组",
            source = ScreenDetectionSource.NONE,
            nodePath = "未知节点"
        )
    }

    private data class CmdlineMatch(val rawSnippet: String, val vendorName: String)

    private fun parseCmdline(cmdline: String, rules: RuleDictionary): CmdlineMatch? {
        // 提取面板代号上下文，适配高通 (mdss_dsi_panel_) 与联发科 (panel_)
        val regex = Regex("""(?:mdss_dsi_panel_|panel_)([A-Za-z0-9_]+?)(?:_dsc|_cmd|:|\s|$)""", RegexOption.IGNORE_CASE)
        val match = regex.find(cmdline)
        
        // 提取 touch_panel 参数作为辅助展示
        val touchPanelSegment = cmdline.split(" ", "\t", "\n").firstOrNull {
            it.contains("touch_panel", ignoreCase = true)
        }

        if (match != null) {
            val fullSnippet = match.value
            val panelCode = match.groupValues[1]

            // 原始代码展示片段（面板代号 + touch_panel_xx）
            val rawDisplay = if (touchPanelSegment != null && touchPanelSegment != fullSnippet) {
                "$touchPanelSegment |\n$fullSnippet"
            } else {
                fullSnippet
            }

            // 直接走 JSON 规则字典匹配 (如 AA590, AA599, AA607)
            val dictionaryVendor = HardwareParserEngine.parseScreen(panelCode, rules)
            if (dictionaryVendor != "未能读取节点信息" && !dictionaryVendor.startsWith("未匹配到") && !dictionaryVendor.startsWith("未知厂商")) {
                return CmdlineMatch(rawDisplay, dictionaryVendor)
            }
        }

        // 如果上述面板代号解析失败，降级进行触控参数段和全局关键字匹配
        if (touchPanelSegment != null) {
            val vendorFromTouchPanel = matchVendorKeyword(touchPanelSegment, rules)
            if (vendorFromTouchPanel != null) {
                return CmdlineMatch(touchPanelSegment, vendorFromTouchPanel)
            }
        }

        val globalVendor = matchVendorKeyword(cmdline, rules)
        if (globalVendor != null) {
            return CmdlineMatch(touchPanelSegment ?: "cmdline", globalVendor)
        }

        return null
    }

    // 解析 /proc/devinfo/tp 节点文本（统一转大写，全段包含匹配，解决带 _HBP 后缀匹配问题）
    private fun parseTpNodeContent(tpContent: String, rules: RuleDictionary): String? {
        val upperText = tpContent.uppercase()

        // 字典规则匹配优先
        rules.lcd.entries.firstOrNull { upperText.contains(it.key.uppercase()) }?.let {
            return it.value
        }

        // 不区分大小写的包含匹配
        return when {
            upperText.contains("TIANMA") -> "天马 (Tianma)"
            upperText.contains("BOE") -> "京东方 (BOE)"
            upperText.contains("SAMSUNG") || upperText.contains("SDC") -> "三星 (Samsung)"
            upperText.contains("VISIONOX") -> "维信诺 (Visionox)"
            upperText.contains("CSOT") -> "华星光电 (CSOT)"
            else -> null
        }
    }

    // 通用关键字与字典扫描辅助函数
    private fun matchVendorKeyword(text: String, rules: RuleDictionary): String? {
        val lower = text.lowercase()

        // 字典匹配优先
        rules.lcd.entries.firstOrNull { lower.contains(it.key.lowercase()) }?.let {
            return it.value
        }

        return when {
            lower.contains("tianma") -> "天马 (Tianma)"
            lower.contains("boe") -> "京东方 (BOE)"
            lower.contains("samsung") || lower.contains("sdc") -> "三星 (Samsung)"
            lower.contains("visionox") -> "维信诺 (Visionox)"
            lower.contains("csot") -> "华星光电 (CSOT)"
            else -> null
        }
    }

    private fun isValidContent(content: String): Boolean {
        if (content.isBlank()) return false
        if (content.startsWith("无法获取") || content.startsWith("cat:")) return false
        if (content.contains("Permission denied", ignoreCase = true)) return false
        if (content.contains("No such file", ignoreCase = true)) return false
        return true
    }
}
