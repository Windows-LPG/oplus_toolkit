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
        // 内置 GitHub 远程规则 JSON 链接（可以在此处填入你的 GitHub 仓库 Raw JSON 链接）
        const val DEFAULT_ONLINE_RULES_URL = "test_URL"
    }

    private val gson = Gson()
    private val customRulesFile = File(context.filesDir, "custom_rules.json")

    suspend fun loadRules(): RuleDictionary = withContext(Dispatchers.IO) {
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
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadRulesFromUrl(urlStr: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr)
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
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isCustomRuleActive(): Boolean = withContext(Dispatchers.IO) {
        customRulesFile.exists()
    }

    suspend fun resetToDefaultRules(): Boolean = withContext(Dispatchers.IO) {
        if (customRulesFile.exists()) {
            customRulesFile.delete()
        } else {
            true
        }
    }
}
