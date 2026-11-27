package io.github.windowslpg.oplus_toolkit.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScaffold(
    viewModel: MainViewModel = viewModel(),
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onNavigateToRoot: () -> Unit = {},
    onNavigateToNonRoot: () -> Unit = {},
    onNavigateToScreenDetection: () -> Unit = {},
    onNavigateToThemeSettings: () -> Unit = {},
    onNavigateToOnlineUpdate: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = selectedTab, pageCount = { 2 })

    // 当外部 selectedTab 改变时（如通过外部导航回到 Tab 1），同步动画滚至对应页面
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    // 当用户手动左右滑动手势翻页时，同步更新 selectedTab 状态
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != selectedTab) {
            onTabSelected(pagerState.currentPage)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "主页") },
                    label = { Text("主页") }
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
                    label = { Text("设置") }
                )
            }
        }
    ) { paddingValues ->
        // 使用与 InstallerX 一致的 HorizontalPager 实现真正的原生跟手翻页动效，冷启动不激进预加载
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 0,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { pageIndex ->
            when (pageIndex) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToRoot = onNavigateToRoot,
                    onNavigateToNonRoot = onNavigateToNonRoot,
                    onNavigateToScreenDetection = onNavigateToScreenDetection
                )
                1 -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToOnlineUpdate = onNavigateToOnlineUpdate,
                    onNavigateToThemeSettings = onNavigateToThemeSettings,
                    onNavigateToAbout = onNavigateToAbout
                )
            }
        }
    }
}
