package id.stargan.intikasir.feature.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.feature.home.domain.model.MenuItems
import id.stargan.intikasir.feature.security.ui.SecuritySettingsViewModel
import id.stargan.intikasir.feature.security.util.usePermission
import id.stargan.intikasir.feature.home.ui.components.MenuCard
import id.stargan.intikasir.feature.home.ui.components.SalesSummaryCard
import id.stargan.intikasir.feature.reports.domain.model.PeriodType
import id.stargan.intikasir.feature.reports.ui.ReportsEvent
import id.stargan.intikasir.feature.reports.ui.ReportsViewModel
import id.stargan.intikasir.feature.pos.navigation.PosRoutes
import kotlin.math.roundToLong

/**
 * Home Screen dengan menu grid
 * Menampilkan menu utama aplikasi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMenuClick: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()

    // Reports ViewModel to read dashboard summary (if available)
    val reportsViewModel: ReportsViewModel = hiltViewModel()
    val reportsUiState by reportsViewModel.uiState.collectAsState()
    val dashboard = reportsUiState.dashboard

    // Ensure we request "TODAY" dashboard when HomeScreen is shown so card reflects today's sales
    LaunchedEffect(currentUser) {
        // Wait until currentUser is loaded, then set cashier filter accordingly
        val cashierIdToSet = if (currentUser?.role?.name == "ADMIN") null else currentUser?.id
        reportsViewModel.onEvent(ReportsEvent.SetCashierFilter(cashierIdToSet))
        // Request today's data after filter applied
        reportsViewModel.onEvent(ReportsEvent.SelectPeriod(PeriodType.TODAY))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "IntiKasir",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Menu Utama",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    currentUser?.let { user ->
                        Text(
                            text = "Halo, ${user.name} (${user.role.name})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } ?: run {
                        Text(
                            text = "Pilih menu untuk memulai",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sales summary card for today's transactions
                    // Uses ReportsViewModel.dashboard when available
                    SalesSummaryCard(
                        totalSales = dashboard?.totalRevenue?.roundToLong(),
                        transactionCount = dashboard?.transactionCount,
                        netChange = reportsUiState.dashboardRevenueChange?.roundToLong(),
                        isLoading = reportsUiState.isLoading,
                        onRetry = { reportsViewModel.onEvent(ReportsEvent.Refresh) }
                    )
                }
            }

            // Permission: include Settings menu for cashier only if cashier has canEditSettings permission
            val securityVm: SecuritySettingsViewModel = hiltViewModel()
            val includeSettingsForCashier = usePermission(securityVm.observePermission("CASHIER") { it.canEditSettings })
            val menuList = MenuItems.items(isAdmin = currentUser?.role?.name == "ADMIN", includeSettingsForCashier = includeSettingsForCashier)
            // Menu Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(menuList) { menuItem ->
                    MenuCard(
                        menuItem = menuItem,
                        onClick = { 
                            if (menuItem.route == "cashier") {
                                onMenuClick(PosRoutes.POS)
                            } else {
                                onMenuClick(menuItem.route)
                            }
                        }
                    )
                }
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isLoggingOut) {
                        showLogoutDialog = false
                    }
                },
                title = {
                    Text("Logout")
                },
                text = {
                    if (isLoggingOut) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Sedang logout...")
                        }
                    } else {
                        Text("Apakah Anda yakin ingin keluar?")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.logout {
                                showLogoutDialog = false
                                onLogout()
                            }
                        },
                        enabled = !isLoggingOut
                    ) {
                        Text("Ya")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false },
                        enabled = !isLoggingOut
                    ) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onMenuClick = {},
            onLogout = {}
        )
    }
}
