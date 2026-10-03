package io.github.windowslpg.oplus_toolkit.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.windowslpg.oplus_toolkit.data.repository.RuleRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineUpdateScreen(
    onBack: () -> Unit,
    onRulesUpdated: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val ruleRepository = remember { RuleRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var urlText by remember { mutableStateOf(RuleRepository.DEFAULT_ONLINE_RULES_URL) }
    var autoApply by remember { mutableStateOf(true) }
    var isDownloading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "联网更新规则",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "在线获取最新硬件识别字典",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "请输入包含 JSON 格式字典规则的 HTTP/HTTPS 链接，点击下方按钮即可联网下载更新。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 输入框：内置链接可在此处填写/修改
                        OutlinedTextField(
                            value = urlText,
                            onValueChange = { urlText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("规则 JSON 链接") },
                            placeholder = { Text("https://github.com/Windows-LPG/oplus_toolkit/blob/master/rule.json") },
                            singleLine = true,
                            trailingIcon = {
                                if (urlText.isNotEmpty()) {
                                    IconButton(onClick = { urlText = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "清空")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 下方的勾选按钮
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = autoApply,
                                onCheckedChange = { autoApply = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "更新完成后自动应用新规则",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 下载更新按钮
                        Button(
                            onClick = {
                                if (urlText.isBlank()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("请输入有效的下载链接")
                                    }
                                    return@Button
                                }
                                isDownloading = true
                                coroutineScope.launch {
                                    val result = ruleRepository.downloadRulesFromUrl(urlText.trim())
                                    isDownloading = false
                                    result.fold(
                                        onSuccess = {
                                            snackbarHostState.showSnackbar("更新成功！规则已成功应用")
                                            if (autoApply) {
                                                onRulesUpdated()
                                            }
                                        },
                                        onFailure = { error ->
                                            snackbarHostState.showSnackbar("更新失败: ${error.message}")
                                        }
                                    )
                                }
                            },
                            enabled = !isDownloading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text("下载更新中...")
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("开始下载更新")
                            }
                        }
                    }
                }
            }
        }
    }
}
