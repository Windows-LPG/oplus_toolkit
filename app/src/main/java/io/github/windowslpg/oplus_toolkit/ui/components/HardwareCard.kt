package io.github.windowslpg.oplus_toolkit.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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

                            // 一键 Bing 搜索按钮 (智能清洗关键词)
                            IconButton(
                                onClick = {
                                    val query = extractSearchQuery(item)
                                    val searchUrl = "https://www.bing.com/search?q=" + Uri.encode(query)
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "无法打开浏览器: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "一键 Bing 搜索",
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

// 智能清洗搜索关键词，屏幕和 DDR 提取关键型号代号，UFS 保留完整器件型号
private fun extractSearchQuery(item: HardwareItem): String {
    val raw = item.rawCode.trim()
    return when (item.category) {
        HardwareCategory.SCREEN -> {
            // 正则优先提取核心屏幕面板代码段（如 AA590_P_3_A0020, AA607, AA599 等）
            val regex = Regex("""(?:mdss_dsi_panel_|panel_)([A-Za-z0-9_]+)""", RegexOption.IGNORE_CASE)
            val match = regex.find(raw)
            if (match != null) {
                match.groupValues[1]
            } else {
                // 如果是从其他节点读取，过滤并提取包含核心代码的那一行
                raw.lines()
                    .firstOrNull { it.contains("version", ignoreCase = true) || it.contains("manufacture", ignoreCase = true) || it.contains("panel", ignoreCase = true) }
                    ?.replace("Device version:", "")
                    ?.replace("Device manufacture:", "")
                    ?.replace("|", " ")
                    ?.trim() ?: raw.take(60)
            }
        }
        HardwareCategory.RAM -> {
            // DDR 内存：从 Device manufacture: Hynix|D1a|16G 中清洗出 "Hynix D1a 16G" 核心代号
            if (raw.contains("Device manufacture:", ignoreCase = true)) {
                val manufactureLine = raw.lines().firstOrNull { it.contains("manufacture", ignoreCase = true) } ?: ""
                val code = manufactureLine.substringAfter("Device manufacture:").trim()
                code.replace("|", " ")
            } else if (raw.contains("Device version:", ignoreCase = true)) {
                val versionLine = raw.lines().firstOrNull { it.contains("version", ignoreCase = true) } ?: ""
                versionLine.substringAfter("Device version:").trim()
            } else {
                raw.replace("|", " ").take(60)
            }
        }
        HardwareCategory.ROM -> {
            // UFS 闪存：完美提取器件完整型号代码（如 KLUEG4RHHF-FOG1 或 HN8T274EJKX130）
            if (raw.contains("Device version:", ignoreCase = true)) {
                val versionLine = raw.lines().firstOrNull { it.contains("version", ignoreCase = true) } ?: ""
                val code = versionLine.substringAfter("Device version:").trim()
                if (code.isNotBlank()) code else raw
            } else {
                raw
            }
        }
    }
}
