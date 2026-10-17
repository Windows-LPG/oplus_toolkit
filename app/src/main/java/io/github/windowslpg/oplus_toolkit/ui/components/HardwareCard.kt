package io.github.windowslpg.oplus_toolkit.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.windowslpg.oplus_toolkit.data.model.HardwareCategory
import io.github.windowslpg.oplus_toolkit.data.model.HardwareItem

@Composable
fun HardwareCard(
    item: HardwareItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val icon = when (item.category) {
        HardwareCategory.SCREEN -> Icons.Default.Tv
        HardwareCategory.RAM -> Icons.Default.Memory
        HardwareCategory.ROM -> Icons.Default.DeveloperBoard
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icon + Category Name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.category.displayName,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Parsed Vendor Result (Prominent)
            Text(
                text = item.vendorName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Raw Code & Node Details (Collapsible for long logs)
            var isLogExpanded by remember { mutableStateOf(false) }
            val lineCount = item.rawCode.lines().size
            val isLongLog = lineCount > 3 || item.rawCode.length > 120

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "底层节点: ${item.detectionSource ?: item.category.nodePath}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.weight(1f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isLongLog) {
                                Text(
                                    text = if (isLogExpanded) "收起日志 ▲" else "展开日志 (${lineCount}行) ▼",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable { isLogExpanded = !isLogExpanded }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }

                            // 一键跳转 AI App 智能搜索按钮
                            IconButton(
                                onClick = {
                                    launchAiSearch(context, item)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "一键 AI 搜索",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // 复制原始代号按钮
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Hardware Raw Code", item.rawCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "已复制原始代号", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "复制原始代号",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "原始代号:\n${item.rawCode}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        ),
                        maxLines = if (isLogExpanded || !isLongLog) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private data class AiAppCandidate(
    val name: String,
    val packages: List<String>
)

private fun isProxyOrVpnActive(context: Context): Boolean {
    return try {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        val isVpn = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        val proxyHost = System.getProperty("http.proxyHost")
        val isProxy = !proxyHost.isNullOrBlank()

        isVpn || isProxy
    } catch (_: Exception) {
        false
    }
}

private fun launchAiSearch(context: Context, item: HardwareItem) {
    // 丰富 Prompt，确保 AI 能够完整识别面板参数与节点代码
    val prompt = "请详细分析并说明该手机${item.category.displayName}的硬件厂商、规格型号代号与性能技术参数。\n底层硬件参数与代码：\n${item.rawCode.trim()}"

    // 1. 将分析 Prompt 自动复制到剪贴板
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("AI Hardware Query", prompt)
    clipboard.setPrimaryClip(clip)

    val hasProxy = isProxyOrVpnActive(context)

    // AI 应用候选池
    val chatGpt = AiAppCandidate("ChatGPT", listOf("com.openai.chatgpt"))
    val gemini = AiAppCandidate("Gemini", listOf("com.google.android.apps.bard", "com.google.android.googlequicksearchbox"))
    val deepseek = AiAppCandidate("DeepSeek", listOf("com.deepseek.chat"))
    val qwen = AiAppCandidate("通义千问", listOf("com.aliyun.tongyi", "com.alibaba.tongyi"))
    val doubao = AiAppCandidate("豆包", listOf("com.larus.nova", "com.volcengine.doubao"))

    // 未识别到代理时，直接舍弃国外 AI (ChatGPT/Gemini)，仅保留国产 AI 候选池
    val priorityList = if (hasProxy) {
        listOf(chatGpt, gemini, deepseek, qwen, doubao)
    } else {
        listOf(deepseek, qwen, doubao)
    }

    // 查找并调起已安装的 AI APP
    var launchedApp: AiAppCandidate? = null
    for (candidate in priorityList) {
        for (pkg in candidate.packages) {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                    launchedApp = candidate
                    break
                } catch (_: Exception) {}
            }
        }
        if (launchedApp != null) break
    }

    if (launchedApp != null) {
        val proxyStatusText = if (hasProxy) "已开启代理" else "国内网络"
        Toast.makeText(context, "[$proxyStatusText] 已跳转 ${launchedApp.name}，硬件参数已复制到剪贴板！", Toast.LENGTH_LONG).show()
    } else {
        // 无可用 AI App，提示用户，不打开网页
        Toast.makeText(context, "未检测到兼容的 AI 应用，硬件参数已复制到剪贴板，请安装 AI 软件后使用！", Toast.LENGTH_LONG).show()
    }
}
