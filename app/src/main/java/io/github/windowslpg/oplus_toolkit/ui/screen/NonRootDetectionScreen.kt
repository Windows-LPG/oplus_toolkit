package io.github.windowslpg.oplus_toolkit.ui.screen

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import io.github.windowslpg.oplus_toolkit.data.model.HardwareCategory
import io.github.windowslpg.oplus_toolkit.data.model.HardwareItem
import io.github.windowslpg.oplus_toolkit.data.repository.HardwareParserEngine
import io.github.windowslpg.oplus_toolkit.data.repository.RuleRepository
import io.github.windowslpg.oplus_toolkit.data.root.ShizukuCommandRunner
import io.github.windowslpg.oplus_toolkit.ui.components.HardwareCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonRootDetectionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isShizukuReady by remember { mutableStateOf(ShizukuCommandRunner.hasPermission()) }
    var isCapturing by remember { mutableStateOf(false) }
    var captureJob by remember { mutableStateOf<Job?>(null) }
    var hardwareResults by remember { mutableStateOf<List<HardwareItem>>(emptyList()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "免 Root 试验检测",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "实验性",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
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
                    containerColor = MaterialTheme.colorScheme.background
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
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Shizuku 授权状态卡片
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isShizukuReady) Icons.Default.CheckCircle else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (isShizukuReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isShizukuReady) "Shizuku 服务：已授权就绪" else "未获取 Shizuku 授权",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isShizukuReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isShizukuReady) {
                                Text(
                                    text = "Shizuku 授权正常，点击下方按钮即可启动自动抓取与性能日志识别。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = "请确保已在手机上安装并启动 Shizuku 软件，然后点击下方按钮授予本应用 Shizuku 权限。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        ShizukuCommandRunner.requestPermission()
                                        coroutineScope.launch {
                                            delay(1000)
                                            isShizukuReady = ShizukuCommandRunner.hasPermission()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("请求 Shizuku 授权")
                                }
                            }
                        }
                    }
                }

                // 一键启动抓取与结果卡片
                if (isShizukuReady) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "抓取检测",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "点击后系统将自动跳转至【手机管家】，请在手机管家中点击“常规检测”并勾选“性能”，随后启动检测，工具将自动实时回显闪存及内存数据。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (isCapturing) return@Button
                                        isCapturing = true
                                        hardwareResults = emptyList()

                                        captureJob = coroutineScope.launch {
                                            try {
                                                if (!ShizukuCommandRunner.hasPermission()) {
                                                    snackbarHostState.showSnackbar("请先授权 Shizuku 权限！")
                                                    isCapturing = false
                                                    return@launch
                                                }

                                                // 1. 设置系统属性
                                                ShizukuCommandRunner.executeCommand("setprop log.tag.Postman-StorageHealthCheckItem DEBUG")

                                                // 2. 跳转手机管家
                                                val launched = launchPhoneManager(context)
                                                if (launched) {
                                                    Toast.makeText(context, "请在手机管家中开启【性能】常规检测", Toast.LENGTH_LONG).show()
                                                } else {
                                                    snackbarHostState.showSnackbar("未能自动调起手机管家，请手动打开【手机管家】进行常规检测。")
                                                }

                                                // 3. 监听日志
                                                withContext(Dispatchers.IO) {
                                                    val process = ShizukuCommandRunner.executeStream("logcat -v threadtime -s Postman-StorageHealthCheckItem:D *:S")
                                                    if (process != null) {
                                                        val reader = BufferedReader(InputStreamReader(process.inputStream))
                                                        var line: String?
                                                        while (reader.readLine().also { line = it } != null) {
                                                            if (line?.contains("health info map") == true) {
                                                                val mapContent = line.substringAfter("health info map").substringAfter("{").substringBefore("}")
                                                                val pairs = mapContent.split(",").map { it.trim() }
                                                                var ddrRaw = ""
                                                                var ufsRaw = ""

                                                                for (pair in pairs) {
                                                                    if (pair.startsWith("DDRDev=")) {
                                                                        ddrRaw = pair.substringAfter("DDRDev=").trim()
                                                                    } else if (pair.startsWith("MemoryDev=")) {
                                                                        ufsRaw = pair.substringAfter("MemoryDev=").trim()
                                                                    }
                                                                }

                                                                if (ddrRaw.isNotEmpty() || ufsRaw.isNotEmpty()) {
                                                                    val rules = RuleRepository(context).loadRules()
                                                                    val newResults = mutableListOf<HardwareItem>()

                                                                    if (ddrRaw.isNotEmpty()) {
                                                                        val vendor = HardwareParserEngine.parse(HardwareCategory.RAM, ddrRaw, rules)
                                                                        newResults.add(
                                                                            HardwareItem(
                                                                                category = HardwareCategory.RAM,
                                                                                rawCode = ddrRaw,
                                                                                vendorName = vendor,
                                                                                detectionSource = "手机管家日志 (Postman)",
                                                                                warningMessage = "* 实验性日志推测"
                                                                            )
                                                                        )
                                                                    }
                                                                    if (ufsRaw.isNotEmpty()) {
                                                                        val vendor = HardwareParserEngine.parse(HardwareCategory.ROM, ufsRaw, rules)
                                                                        newResults.add(
                                                                            HardwareItem(
                                                                                category = HardwareCategory.ROM,
                                                                                rawCode = ufsRaw,
                                                                                vendorName = vendor,
                                                                                detectionSource = "手机管家日志 (Postman)",
                                                                                warningMessage = "* 实验性日志推测"
                                                                            )
                                                                        )
                                                                    }

                                                                    withContext(Dispatchers.Main) {
                                                                        hardwareResults = newResults
                                                                        isCapturing = false
                                                                    }

                                                                    ShizukuCommandRunner.executeCommand("setprop log.tag.Postman-StorageHealthCheckItem \"\"")
                                                                    process.destroy()
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("抓取中断: ${e.message}")
                                                isCapturing = false
                                            }
                                        }

                                        // 3分钟超时自动释放
                                        coroutineScope.launch {
                                            delay(180_000)
                                            if (isCapturing) {
                                                isCapturing = false
                                                captureJob?.cancel()
                                                if (ShizukuCommandRunner.hasPermission()) {
                                                    ShizukuCommandRunner.executeCommand("setprop log.tag.Postman-StorageHealthCheckItem \"\"")
                                                }
                                                snackbarHostState.showSnackbar("抓取超时，请确认是否勾选并运行了性能检测。")
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCapturing) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    if (isCapturing) {
                                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp).size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSecondary)
                                        Text("后台实时监听抓取中...")
                                    } else {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("一键启动抓取")
                                    }
                                }

                                if (hardwareResults.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        hardwareResults.forEach { item ->
                                            HardwareCard(item = item)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun launchPhoneManager(context: Context): Boolean {
    val packageNames = listOf(
        "com.coloros.phonemanager",
        "com.oplus.phonemanager",
        "com.oneplus.phonemanager",
        "com.oplus.postmanservice"
    )

    for (pkg in packageNames) {
        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
                return true
            } catch (_: Exception) {}
        }
    }
    return false
}
