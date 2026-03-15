package com.sirius.firegov.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.sirius.firegov.ui.screens.history.HistoryScreen
import com.sirius.firegov.ui.screens.news.NewsScreen
import com.sirius.firegov.ui.screens.profile.ProfileScreen
import com.sirius.firegov.ui.screens.report.ReportScreen
import com.sirius.firegov.ui.theme.FiregovTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    profileViewModel: com.sirius.firegov.ui.screens.profile.ProfileViewModel = hiltViewModel()
) {
    val user by profileViewModel.user.collectAsState()
    val isProfileComplete = remember(user) {
        user != null && user?.name?.isNotBlank() == true && user?.phoneNumber?.isNotBlank() == true
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    HomeScreenContent(
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        onNavigateToDetail = onNavigateToDetail,
        isProfileComplete = isProfileComplete,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    onLogout: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    isProfileComplete: Boolean = true,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {}
) {
    // Force Profile tab if profile is incomplete
    LaunchedEffect(isProfileComplete) {
        if (!isProfileComplete) {
            onTabSelected(3)
        }
    }

    val tabs = listOf("Report", "History", "News", "Profile")
    val icons = listOf(Icons.Default.Report, Icons.Default.History, Icons.Default.Newspaper, Icons.Default.Person)
    val isPreview = LocalInspectionMode.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FireGov") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    val isEnabled = isProfileComplete || index == 3
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                icons[index], 
                                contentDescription = title,
                                tint = if (isEnabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.3f)
                            ) 
                        },
                        label = { 
                            Text(
                                title,
                                color = if (isEnabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.3f)
                            ) 
                        },
                        selected = selectedTab == index,
                        onClick = { if (isEnabled) onTabSelected(index) },
                        enabled = isEnabled
                    )
                }
            }
        }
    ) { innerPadding ->
        if (isPreview) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("${tabs[selectedTab]} Screen Preview")
            }
        } else {
            when (selectedTab) {
                0 -> ReportScreen(
                    onSuccess = { onTabSelected(1) },
                    modifier = Modifier.padding(innerPadding)
                )
                1 -> HistoryScreen(onNavigateToDetail = onNavigateToDetail, modifier = Modifier.padding(innerPadding))
                2 -> NewsScreen(modifier = Modifier.padding(innerPadding))
                3 -> ProfileScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FiregovTheme {
        HomeScreenContent(onLogout = {}, onNavigateToDetail = {})
    }
}
