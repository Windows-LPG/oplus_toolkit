package io.github.windowslpg.oplus_toolkit.data.model

data class RuleDictionary(
    val lcd: Map<String, String> = emptyMap(),
    val ddr: Map<String, String> = emptyMap(),
    val ufs: Map<String, String> = emptyMap()
)
