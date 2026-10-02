package io.github.windowslpg.oplus_toolkit.ui.screen

import io.github.windowslpg.oplus_toolkit.data.model.HardwareItem

data class MainUiState(
    val isLoading: Boolean = false,
    val isRootGranted: Boolean = true,
    val hardwareList: List<HardwareItem> = emptyList(),
    val isUsingCustomRules: Boolean = false,
    val userMessage: String? = null
)
