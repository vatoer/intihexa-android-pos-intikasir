package id.stargan.intikasir.feature.history.ui

/**
 * History Module Index
 *
 * This module provides transaction history features including:
 * - Transaction list with filtering and export
 * - Transaction detail view with printing capabilities
 * - Reusable components for transaction display
 *
 * ## Main Screens
 *
 * ### HistoryScreen (List View)
 * ```kotlin
 * import id.stargan.intikasir.feature.history.ui.screens.HistoryScreen
 *
 * HistoryScreen(
 *     onBack = { navController.navigateUp() },
 *     onOpenDetail = { txId -> navController.navigate("detail/$txId") }
 * )
 * ```
 *
 * ### HistoryDetailScreen (Detail View)
 * ```kotlin
 * import id.stargan.intikasir.feature.history.ui.components.HistoryDetailScreen
 *
 * HistoryDetailScreen(
 *     transactionId = txId,
 *     onBack = { navController.navigateUp() },
 *     onPrint = { tx -> /* print receipt */ },
 *     // ... other callbacks
 * )
 * ```
 *
 * ## Reusable Components
 *
 * - **HistoryFilterBar**: Filter transactions by date range and status
 * - **TransactionRow**: Single transaction list item
 * - **ItemDetailRow**: Transaction item detail row
 * - **DateRange**: Filter date range options
 *
 * @see id.stargan.intikasir.feature.history.ui.screens.HistoryScreen
 * @see id.stargan.intikasir.feature.history.ui.components.HistoryDetailScreen
 * @see id.stargan.intikasir.feature.history.ui.components.HistoryFilterBar
 * @see id.stargan.intikasir.feature.history.ui.components.TransactionRow
 * @see id.stargan.intikasir.feature.history.ui.components.ItemDetailRow
 */

