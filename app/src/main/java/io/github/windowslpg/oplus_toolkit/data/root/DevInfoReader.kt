package io.github.windowslpg.oplus_toolkit.data.root

import io.github.windowslpg.oplus_toolkit.data.model.HardwareCategory

object DevInfoReader {

    suspend fun readRawNode(category: HardwareCategory): String {
        val result = RootShellExecutor.executeCommand("cat ${category.nodePath}")
        return result.ifBlank { "无法获取/节点为空" }
    }
}
