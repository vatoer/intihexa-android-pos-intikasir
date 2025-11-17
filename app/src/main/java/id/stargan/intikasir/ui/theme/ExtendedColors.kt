package id.stargan.intikasir.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended Color Scheme untuk IntiKasir
 * Menambahkan semantic colors dan special purpose colors
 */
data class ExtendedColorScheme(
    // Semantic Colors
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,

    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,

    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,

    // Transaction Status
    val pendingColor: Color,
    val paidColor: Color,
    val canceledColor: Color,
    val refundedColor: Color,

    // Category Colors
    val categoryFood: Color,
    val categoryDrink: Color,
    val categorySnack: Color,
    val categoryOther: Color,

    // Cash Flow
    val incomeColor: Color,
    val expenseColor: Color,
    val netProfitColor: Color
)

val LightExtendedColorScheme = ExtendedColorScheme(
    success = Success,
    onSuccess = OnSuccess,
    successContainer = SuccessContainer,
    onSuccessContainer = OnSuccessContainer,

    warning = Warning,
    onWarning = OnWarning,
    warningContainer = WarningContainer,
    onWarningContainer = OnWarningContainer,

    info = Info,
    onInfo = OnInfo,
    infoContainer = InfoContainer,
    onInfoContainer = OnInfoContainer,

    pendingColor = PendingColor,
    paidColor = PaidColor,
    canceledColor = CanceledColor,
    refundedColor = RefundedColor,

    categoryFood = CategoryFood,
    categoryDrink = CategoryDrink,
    categorySnack = CategorySnack,
    categoryOther = CategoryOther,

    incomeColor = IncomeColor,
    expenseColor = ExpenseColor,
    netProfitColor = NetProfitColor
)

val DarkExtendedColorScheme = ExtendedColorScheme(
    // Menggunakan warna yang sama (sudah disesuaikan contrast untuk dark mode)
    success = Success,
    onSuccess = OnSuccess,
    successContainer = SuccessContainer,
    onSuccessContainer = OnSuccessContainer,

    warning = Warning,
    onWarning = OnWarning,
    warningContainer = WarningContainer,
    onWarningContainer = OnWarningContainer,

    info = Info,
    onInfo = OnInfo,
    infoContainer = InfoContainer,
    onInfoContainer = OnInfoContainer,

    pendingColor = PendingColor,
    paidColor = PaidColor,
    canceledColor = CanceledColor,
    refundedColor = RefundedColor,

    categoryFood = CategoryFood,
    categoryDrink = CategoryDrink,
    categorySnack = CategorySnack,
    categoryOther = CategoryOther,

    incomeColor = IncomeColor,
    expenseColor = ExpenseColor,
    netProfitColor = NetProfitColor
)

val LocalExtendedColorScheme = staticCompositionLocalOf { LightExtendedColorScheme }

/**
 * Extension property untuk mengakses extended colors dari MaterialTheme
 *
 * Usage:
 * ```
 * Card(
 *     colors = CardDefaults.cardColors(
 *         containerColor = MaterialTheme.colorScheme.extendedColors.successContainer
 *     )
 * )
 * ```
 */
val ColorScheme.extendedColors: ExtendedColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColorScheme.current

