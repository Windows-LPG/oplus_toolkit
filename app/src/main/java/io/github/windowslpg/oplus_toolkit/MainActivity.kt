package io.github.windowslpg.oplus_toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.windowslpg.oplus_toolkit.ui.screen.AboutScreen
import io.github.windowslpg.oplus_toolkit.ui.screen.MainScaffold
import io.github.windowslpg.oplus_toolkit.ui.screen.MainViewModel
import io.github.windowslpg.oplus_toolkit.ui.screen.NonRootDetectionScreen
import io.github.windowslpg.oplus_toolkit.ui.screen.OnlineUpdateScreen
import io.github.windowslpg.oplus_toolkit.ui.screen.RootDetectionScreen
import io.github.windowslpg.oplus_toolkit.ui.screen.ScreenDetectionScreen
import io.github.windowslpg.oplus_toolkit.ui.screen.ThemeSettingsScreen
import io.github.windowslpg.oplus_toolkit.ui.theme.OplusToolkitTheme
import io.github.windowslpg.oplus_toolkit.util.ThemeManager
import io.github.windowslpg.oplus_toolkit.util.ThemeMode

enum class AppScreen {
    SCAFFOLD,
    ROOT_DETECTION,
    NON_ROOT_DETECTION,
    SCREEN_DETECTION,
    THEME_SETTINGS,
    ONLINE_UPDATE,
    ABOUT
}

private const val TRANSITION_DURATION = 280

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val themeManager = remember { ThemeManager.getInstance(context) }
            val themeState by themeManager.themeState.collectAsState()

            val isDarkTheme = when (themeState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            OplusToolkitTheme(
                darkTheme = isDarkTheme,
                dynamicColor = themeState.dynamicColor
            ) {
                MainAppNav()
            }
        }
    }
}

@Composable
fun MainAppNav() {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.SCAFFOLD) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val mainViewModel: MainViewModel = viewModel()

    val onBackToScaffold: () -> Unit = {
        if (currentScreen != AppScreen.SCAFFOLD) {
            currentScreen = AppScreen.SCAFFOLD
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (initialState == AppScreen.SCAFFOLD && targetState != AppScreen.SCAFFOLD) {
                // 打开二级页面：标准右侧滑入
                (slideInHorizontally(
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                ) { fullWidth -> fullWidth } + fadeIn(
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                )) togetherWith (slideOutHorizontally(
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                ) { fullWidth -> -fullWidth } + fadeOut(
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                ))
            } else if (initialState != AppScreen.SCAFFOLD && targetState == AppScreen.SCAFFOLD) {
                // 关闭二级页面（返回）：标准向右滑出
                (slideInHorizontally(
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                ) { fullWidth -> -fullWidth } + fadeIn(
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                )) togetherWith (slideOutHorizontally(
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                ) { fullWidth -> fullWidth } + fadeOut(
                    animationSpec = tween(durationMillis = TRANSITION_DURATION)
                ))
            } else {
                fadeIn(animationSpec = tween(200)).togetherWith(fadeOut(animationSpec = tween(200)))
            }
        },
        label = "AppScreenTransition"
    ) { screen ->
        when (screen) {
            AppScreen.SCAFFOLD -> {
                MainScaffold(
                    viewModel = mainViewModel,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onNavigateToRoot = { currentScreen = AppScreen.ROOT_DETECTION },
                    onNavigateToNonRoot = { currentScreen = AppScreen.NON_ROOT_DETECTION },
                    onNavigateToScreenDetection = { currentScreen = AppScreen.SCREEN_DETECTION },
                    onNavigateToThemeSettings = { currentScreen = AppScreen.THEME_SETTINGS },
                    onNavigateToOnlineUpdate = { currentScreen = AppScreen.ONLINE_UPDATE },
                    onNavigateToAbout = { currentScreen = AppScreen.ABOUT }
                )
            }
            AppScreen.ROOT_DETECTION -> {
                RootDetectionScreen(
                    viewModel = mainViewModel,
                    onBack = onBackToScaffold
                )
            }
            AppScreen.NON_ROOT_DETECTION -> {
                NonRootDetectionScreen(
                    onBack = onBackToScaffold
                )
            }
            AppScreen.SCREEN_DETECTION -> {
                ScreenDetectionScreen(
                    viewModel = mainViewModel,
                    onBack = onBackToScaffold
                )
            }
            AppScreen.THEME_SETTINGS -> {
                ThemeSettingsScreen(
                    onBack = onBackToScaffold
                )
            }
            AppScreen.ONLINE_UPDATE -> {
                OnlineUpdateScreen(
                    onBack = onBackToScaffold,
                    onRulesUpdated = {
                        mainViewModel.refreshAll()
                        onBackToScaffold()
                    }
                )
            }
            AppScreen.ABOUT -> {
                AboutScreen(
                    onBack = onBackToScaffold
                )
            }
        }
    }
}
