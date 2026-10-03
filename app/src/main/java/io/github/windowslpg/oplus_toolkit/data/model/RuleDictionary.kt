package io.github.windowslpg.oplus_toolkit.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RuleDictionary(
    @SerializedName("lcd") val lcd: Map<String, String> = emptyMap(),
    @SerializedName("ddr") val ddr: Map<String, String> = emptyMap(),
    @SerializedName("ufs") val ufs: Map<String, String> = emptyMap()
)
