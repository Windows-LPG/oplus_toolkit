package io.github.windowslpg.oplus_toolkit.ui.screen

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.windowslpg.oplus_toolkit.data.model.HardwareCategory
import io.github.windowslpg.oplus_toolkit.data.model.HardwareItem
import io.github.windowslpg.oplus_toolkit.data.repository.HardwareParserEngine
import io.github.windowslpg.oplus_toolkit.data.repository.RuleRepository
import io.github.windowslpg.oplus_toolkit.data.root.DevInfoReader
import io.github.windowslpg.oplus_toolkit.data.root.RootShellExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RuleRepository(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userMessage = null) }

            val hasRoot = RootShellExecutor.isRootAvailable()
            val isCustomRules = repository.isCustomRuleActive()

            if (!hasRoot) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRootGranted = false,
                        isUsingCustomRules = isCustomRules,
                        userMessage = "未获取到 Root 权限，请确保应用已授予 su 权限！"
                    )
                }
                return@launch
            }

            val rules = repository.loadRules()

            val items = HardwareCategory.entries.map { category ->
                val rawCode = DevInfoReader.readRawNode(category)
                val vendorName = HardwareParserEngine.parse(category, rawCode, rules)
                HardwareItem(
                    category = category,
                    rawCode = rawCode,
                    vendorName = vendorName,
                    readSuccess = !rawCode.startsWith("无法获取")
                )
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRootGranted = true,
                    hardwareList = items,
                    isUsingCustomRules = isCustomRules
                )
            }
        }
    }

    fun importRules(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.importRulesFromUri(uri)
            if (result.isSuccess) {
                _uiState.update { it.copy(userMessage = "规则导入成功！") }
            } else {
                _uiState.update { it.copy(userMessage = "规则导入失败: ${result.exceptionOrNull()?.message}") }
            }
            refreshAll()
        }
    }

    fun resetRules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.resetToDefaultRules()
            _uiState.update { it.copy(userMessage = "已重置为内置默认规则") }
            refreshAll()
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
