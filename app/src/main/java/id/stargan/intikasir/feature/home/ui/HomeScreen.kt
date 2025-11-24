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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import android.net.Uri
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.AsyncImage
import id.stargan.intikasir.feature.home.domain.model.MenuItems
import id.stargan.intikasir.feature.reports.domain.model.PeriodType
import id.stargan.intikasir.feature.reports.ui.ReportsEvent
import id.stargan.intikasir.feature.reports.ui.ReportsViewModel
import id.stargan.intikasir.feature.pos.navigation.PosRoutes
import id.stargan.intikasir.feature.settings.ui.StoreSettingsViewModel
import id.stargan.intikasir.feature.home.ui.components.MenuCard
import id.stargan.intikasir.feature.home.ui.components.SalesSummaryCard
import id.stargan.intikasir.feature.security.ui.SecuritySettingsViewModel
import id.stargan.intikasir.feature.security.util.usePermission
import kotlin.math.roundToLong
import kotlin.math.max

/**
 * Stateless HomeScreen content — accepts plain data and callbacks so it can be previewed and tested
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    currentUserName: String?,
    storeName: String?,
    storeLogoUri: Uri?,
    totalSales: Long?,
    transactionCount: Int?,
    netChange: Long?,
    isLoading: Boolean,
    menuList: List<id.stargan.intikasir.feature.home.domain.model.MenuItem>,
    onMenuClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onRetry: () -> Unit,
    onBannerClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            // Keep app identity visible in the top bar. The store identity will be in a banner below.
            TopAppBar(
                title = { Text(text = "IntiKasir", style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
            // App identity in TopBar handled above. Below it, show store identity compactly (logo + store name),
            // then the greeting on its own line, then the floating summary card.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Store identity row immediately under the top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { onBannerClick() },
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        val logoSize = 40.dp
                        if (storeLogoUri != null) {
                            AsyncImage(
                                model = storeLogoUri,
                                contentDescription = storeName ?: "Logo Toko",
                                modifier = Modifier
                                    .size(logoSize)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initial = storeName?.firstOrNull()?.uppercaseChar()?.toString() ?: "T"
                            Box(
                                modifier = Modifier
                                    .size(logoSize)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f)),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text(initial, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = storeName ?: "Nama Toko",
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = currentUserName?.let { "Halo, $it" } ?: "Selamat datang",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                            )
                        }
                    }

                    // Sales summary card: placed fully below the store identity (no overlap)
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        SalesSummaryCard(
                            totalSales = totalSales,
                            transactionCount = transactionCount,
                            netChange = netChange,
                            isLoading = isLoading,
                            onRetry = onRetry,
                            cardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            headlineColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            bodyColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Spacer between the floating summary and the grid
            Spacer(modifier = Modifier.height(12.dp))

            // Menu Grid: ensure a minimum of 3 columns and up to 4 columns depending on screen width
            val configuration = LocalConfiguration.current
            val screenWidthDp = configuration.screenWidthDp
            val minItemSize = 120
            val calculated = remember(screenWidthDp) { (screenWidthDp / minItemSize).coerceIn(1, 4) }
            val columns = max(3, calculated)

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
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
    }
}

/**
 * Stateful wrapper that keeps ViewModel usage and side-effects.
 * Collects state and delegates rendering to HomeScreenContent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMenuClick: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val showLogoutDialog = remember { mutableStateOf(false) }
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()

    // Reports ViewModel to read dashboard summary (if available)
    val reportsViewModel: ReportsViewModel = hiltViewModel()
    val reportsUiState by reportsViewModel.uiState.collectAsState()
    val dashboard = reportsUiState.dashboard

    // Ensure we request "TODAY" dashboard when HomeScreen is shown so card reflects today's sales
    LaunchedEffect(currentUser) {
        val cashierIdToSet = if (currentUser?.role?.name == "ADMIN") null else currentUser?.id
        reportsViewModel.onEvent(ReportsEvent.SetCashierFilter(cashierIdToSet))
        reportsViewModel.onEvent(ReportsEvent.SelectPeriod(PeriodType.TODAY))
    }

    // Permission: include Settings menu for cashier only if cashier has canEditSettings permission
    val securityVm: SecuritySettingsViewModel = hiltViewModel()
    val includeSettingsForCashier = usePermission(securityVm.observePermission("CASHIER") { it.canEditSettings })
    val menuList = MenuItems.items(isAdmin = currentUser?.role?.name == "ADMIN", includeSettingsForCashier = includeSettingsForCashier)

    // Read store settings (name + logo preview) so header shows real data and graceful placeholder
    val storeSettingsVm: StoreSettingsViewModel = hiltViewModel()
    val storeUiState by storeSettingsVm.uiState.collectAsState()
    val storeName = storeUiState.settings?.storeName
    val storeLogoUri = storeUiState.logoPreviewUri

    HomeScreenContent(
        currentUserName = currentUser?.name,
        storeName = storeName,
        storeLogoUri = storeLogoUri,
        totalSales = dashboard?.totalRevenue?.roundToLong(),
        transactionCount = dashboard?.transactionCount,
        netChange = reportsUiState.dashboardRevenueChange?.roundToLong(),
        isLoading = reportsUiState.isLoading,
        menuList = menuList,
        onMenuClick = onMenuClick,
        onLogoutClick = { showLogoutDialog.value = true },
        onRetry = { reportsViewModel.onEvent(ReportsEvent.Refresh) },
        onBannerClick = { onMenuClick("settings") },
        modifier = modifier
    )

    // Logout dialog handled here (stateful behavior)
    if (showLogoutDialog.value) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoggingOut) showLogoutDialog.value = false
            },
            title = { Text("Logout") },
            text = {
                if (isLoggingOut) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
                            showLogoutDialog.value = false
                            onLogout()
                        }
                    },
                    enabled = !isLoggingOut
                ) { Text("Ya") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog.value = false }, enabled = !isLoggingOut) { Text("Batal") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val sampleMenu = MenuItems.items(isAdmin = true)
    MaterialTheme {
        HomeScreenContent(
            currentUserName = "Demo User",
            storeName = "Toko Demo",
            storeLogoUri = null,
            totalSales = 123450L,
            transactionCount = 12,
            netChange = 5000L,
            isLoading = false,
            menuList = sampleMenu,
            onMenuClick = {},
            onLogoutClick = {},
            onRetry = {}
        )
    }
}
