package io.github.windowslpg.oplus_toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.windowslpg.oplus_toolkit.ui.screen.MainScreen
import io.github.windowslpg.oplus_toolkit.ui.screen.MainViewModel
import io.github.windowslpg.oplus_toolkit.ui.screen.OnlineUpdateScreen
import io.github.windowslpg.oplus_toolkit.ui.theme.OplusToolkitTheme

enum class Screen {
    MAIN,
    ONLINE_UPDATE
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OplusToolkitTheme {
                MainAppNav()
            }
        }
    }
}

@Composable
fun MainAppNav() {
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }
    val mainViewModel: MainViewModel = viewModel()

    when (currentScreen) {
        Screen.MAIN -> {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToOnlineUpdate = { currentScreen = Screen.ONLINE_UPDATE }
            )
        }
        Screen.ONLINE_UPDATE -> {
            OnlineUpdateScreen(
                onBack = { currentScreen = Screen.MAIN },
                onRulesUpdated = {
                    mainViewModel.refreshAll()
                    currentScreen = Screen.MAIN
                }
            )
        }
    }
}
