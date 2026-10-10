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
import io.github.windowslpg.oplus_toolkit.ui.screen.AboutScreen
import io.github.windowslpg.oplus_toolkit.ui.screen.MainScaffold
import io.github.windowslpg.oplus_toolkit.ui.screen.MainViewModel
import io.github.windowslpg.oplus_toolkit.ui.screen.NonRootDetectionScreen
import io.github.windowslpg.oplus_toolkit.ui.screen.OnlineUpdateScreen
import io.github.windowslpg.oplus_toolkit.ui.screen.RootDetectionScreen
import io.github.windowslpg.oplus_toolkit.ui.theme.OplusToolkitTheme

enum class AppScreen {
    SCAFFOLD,
    ROOT_DETECTION,
    NON_ROOT_DETECTION,
    ONLINE_UPDATE,
    ABOUT
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
    var currentScreen by remember { mutableStateOf(AppScreen.SCAFFOLD) }
    val mainViewModel: MainViewModel = viewModel()

    when (currentScreen) {
        AppScreen.SCAFFOLD -> {
            MainScaffold(
                viewModel = mainViewModel,
                onNavigateToRoot = { currentScreen = AppScreen.ROOT_DETECTION },
                onNavigateToNonRoot = { currentScreen = AppScreen.NON_ROOT_DETECTION },
                onNavigateToOnlineUpdate = { currentScreen = AppScreen.ONLINE_UPDATE },
                onNavigateToAbout = { currentScreen = AppScreen.ABOUT }
            )
        }
        AppScreen.ROOT_DETECTION -> {
            RootDetectionScreen(
                viewModel = mainViewModel,
                onBack = { currentScreen = AppScreen.SCAFFOLD }
            )
        }
        AppScreen.NON_ROOT_DETECTION -> {
            NonRootDetectionScreen(
                onBack = { currentScreen = AppScreen.SCAFFOLD }
            )
        }
        AppScreen.ONLINE_UPDATE -> {
            OnlineUpdateScreen(
                onBack = { currentScreen = AppScreen.SCAFFOLD },
                onRulesUpdated = {
                    mainViewModel.refreshAll()
                    currentScreen = AppScreen.SCAFFOLD
                }
            )
        }
        AppScreen.ABOUT -> {
            AboutScreen(
                onBack = { currentScreen = AppScreen.SCAFFOLD }
            )
        }
    }
}
