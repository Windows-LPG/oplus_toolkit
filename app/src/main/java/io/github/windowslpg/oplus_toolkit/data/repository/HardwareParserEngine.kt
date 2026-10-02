package io.github.windowslpg.oplus_toolkit.data.repository

import io.github.windowslpg.oplus_toolkit.data.model.HardwareCategory
import io.github.windowslpg.oplus_toolkit.data.model.RuleDictionary

object HardwareParserEngine {

    fun parse(category: HardwareCategory, rawCode: String, dictionary: RuleDictionary): String {
        if (rawCode.isBlank() || rawCode.startsWith("无法获取") || rawCode.contains("Permission denied", ignoreCase = true)) {
            return "未能读取节点信息"
        }

        val rulesMap = when (category) {
            HardwareCategory.SCREEN -> dictionary.lcd
            HardwareCategory.RAM -> dictionary.ddr
            HardwareCategory.ROM -> dictionary.ufs
        }

        if (rulesMap.isEmpty()) {
            return "未匹配到解析规则 (代号: $rawCode)"
        }

        // 1. 精确匹配
        rulesMap[rawCode]?.let { return it }

        // 2. 忽略大小写精确匹配
        rulesMap.entries.firstOrNull { it.key.equalsIgnoreCase(rawCode) }?.let { return it.value }

        // 3. 包含子串匹配 (如果原始代号长文本包含字典 Key，或者 Key 包含原始代号)
        rulesMap.entries.firstOrNull {
            rawCode.contains(it.key, ignoreCase = true) || it.key.contains(rawCode, ignoreCase = true)
        }?.let { return it.value }

        return "未知厂商代号 ($rawCode)"
    }

    private fun String.equalsIgnoreCase(other: String): Boolean =
        this.equals(other, ignoreCase = true)
}
