package io.github.windowslpg.oplus_toolkit.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScaffold(
    viewModel: MainViewModel = viewModel(),
    onNavigateToRoot: () -> Unit = {},
    onNavigateToNonRoot: () -> Unit = {},
    onNavigateToOnlineUpdate: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "主页") },
                    label = { Text("主页") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
                    label = { Text("设置") }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> HomeScreen(
                viewModel = viewModel,
                onNavigateToRoot = onNavigateToRoot,
                onNavigateToNonRoot = onNavigateToNonRoot,
                modifier = Modifier.padding(paddingValues)
            )
            1 -> SettingsScreen(
                viewModel = viewModel,
                onNavigateToOnlineUpdate = onNavigateToOnlineUpdate,
                onNavigateToAbout = onNavigateToAbout,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
