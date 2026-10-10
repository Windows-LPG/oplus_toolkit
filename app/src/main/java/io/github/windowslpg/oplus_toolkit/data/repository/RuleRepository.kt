package io.github.windowslpg.oplus_toolkit.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import io.github.windowslpg.oplus_toolkit.data.model.RuleDictionary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class RuleRepository(private val context: Context) {

    companion object {
        // 默认 GitHub 规则链接
        const val DEFAULT_ONLINE_RULES_URL = "https://github.com/Windows-LPG/oplus_toolkit/blob/master/rule.json"
        private const val PREFS_NAME = "oplus_toolkit_prefs"
        private const val KEY_SAVED_VERSION_CODE = "saved_version_code"
        private const val KEY_IS_MANUALLY_IMPORTED = "is_manually_imported"
    }

    private val gson = Gson()
    private val customRulesFile = File(context.filesDir, "custom_rules.json")
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun checkAndUpdateCache() = withContext(Dispatchers.IO) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersion = androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(packageInfo)
            val savedVersion = prefs.getLong(KEY_SAVED_VERSION_CODE, 0L)
            
            if (currentVersion > savedVersion) {
                // 如果是 App 升级且没有被用户手动导入本地文件锁定
                if (!prefs.getBoolean(KEY_IS_MANUALLY_IMPORTED, false)) {
                    if (customRulesFile.exists()) {
                        customRulesFile.delete()
                    }
                }
                prefs.edit().putLong(KEY_SAVED_VERSION_CODE, currentVersion).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadRules(): RuleDictionary = withContext(Dispatchers.IO) {
        checkAndUpdateCache()

        try {
            if (customRulesFile.exists()) {
                val json = customRulesFile.readText()
                val dict = gson.fromJson(json, RuleDictionary::class.java)
                if (dict != null) return@withContext dict
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 读取 assets 默认规则
        try {
            val json = context.assets.open("hardware_rules.json").bufferedReader().use { it.readText() }
            val dict = gson.fromJson(json, RuleDictionary::class.java)
            return@withContext dict ?: RuleDictionary()
        } catch (e: Exception) {
            e.printStackTrace()
            RuleDictionary()
        }
    }

    suspend fun importRulesFromUri(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().use { it.readText() }
            } ?: return@withContext Result.failure(Exception("读取文件失败，读取到的内容为空"))

            // 验证 JSON 解析
            val dict = gson.fromJson(jsonString, RuleDictionary::class.java)
                ?: return@withContext Result.failure(Exception("解析 JSON 失败：格式不匹配"))

            // 持有写入本地 custom_rules.json
            customRulesFile.writeText(jsonString)
            prefs.edit().putBoolean(KEY_IS_MANUALLY_IMPORTED, true).apply()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadRulesFromUrl(urlStr: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            var actualUrl = urlStr.trim()
            // 自动将 GitHub blob 链接转换为 raw.githubusercontent.com 原始文件链接
            if (actualUrl.contains("github.com") && actualUrl.contains("/blob/")) {
                actualUrl = actualUrl
                    .replace("github.com", "raw.githubusercontent.com")
                    .replace("/blob/", "/")
            }

            val url = URL(actualUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.requestMethod = "GET"

            if (connection.responseCode != 200) {
                return@withContext Result.failure(Exception("HTTP 请求失败，状态码: ${connection.responseCode}"))
            }

            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            if (jsonString.isBlank()) {
                return@withContext Result.failure(Exception("下载的内容为空"))
            }

            // 验证规则 JSON 格式
            val dict = gson.fromJson(jsonString, RuleDictionary::class.java)
                ?: return@withContext Result.failure(Exception("解析 JSON 失败：格式不匹配"))

            // 写入本地自定义规则
            customRulesFile.writeText(jsonString)
            prefs.edit().putBoolean(KEY_IS_MANUALLY_IMPORTED, false).apply()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isCustomRuleActive(): Boolean = withContext(Dispatchers.IO) {
        customRulesFile.exists()
    }

    suspend fun resetToDefaultRules(): Boolean = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_IS_MANUALLY_IMPORTED, false).apply()
        if (customRulesFile.exists()) {
            customRulesFile.delete()
        } else {
            true
        }
    }
}
