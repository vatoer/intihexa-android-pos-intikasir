package id.stargan.intikasir.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * IntiKasir Color Palette - "Fresh Commerce"
 * Design System untuk aplikasi PoS yang profesional, segar, dan nyaman
 *
 * Philosophy:
 * - Primary (Teal): Trust, Professional, Fresh
 * - Secondary (Orange): Energy, Urgency, Appetite
 * - Tertiary (Purple): Premium, Analytics
 * - Neutrals: Eye-friendly untuk penggunaan 8+ jam
 */

// ============================================
// PRIMARY COLORS - Teal Spectrum
// ============================================
// Energetic Teal - representing growth & successful transactions
val Primary = Color(0xFF00897B)           // Teal 600 - Confident, Fresh
val OnPrimary = Color(0xFFFFFFFF)         // White - High contrast
val PrimaryContainer = Color(0xFFB2DFDB) // Teal 100 - Soft background
val OnPrimaryContainer = Color(0xFF004D40) // Teal 900 - Dark text

// Primary Variants (for gradients & highlights)
val PrimaryLight = Color(0xFF4DB6AC)      // Teal 300 - Lighter variant
val PrimaryDark = Color(0xFF00695C)       // Teal 800 - Deeper shade

// ============================================
// SECONDARY COLORS - Warm Amber/Orange
// ============================================
// Warm Orange - for promotions, discounts, highlights
val Secondary = Color(0xFFFF6F00)         // Orange 900 - Bold accent
val OnSecondary = Color(0xFFFFFFFF)       // White
val SecondaryContainer = Color(0xFFFFE0B2) // Orange 100 - Soft highlight
val OnSecondaryContainer = Color(0xFFE65100) // Orange 900 dark

// ============================================
// TERTIARY COLORS - Deep Purple
// ============================================
// Deep Purple - for premium/admin features
val Tertiary = Color(0xFF5E35B1)          // Deep Purple 600
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFD1C4E9) // Deep Purple 100
val OnTertiaryContainer = Color(0xFF311B92) // Deep Purple 900

// ============================================
// NEUTRAL COLORS - Gray Spectrum
// ============================================
// Background & Surface - Warm off-white untuk kenyamanan mata
val Background = Color(0xFFFAFAFA)        // Gray 50 - Soft white
val OnBackground = Color(0xFF1C1B1F)      // Almost black (bukan pure black)

val Surface = Color(0xFFFFFFFF)           // Pure white untuk cards
val OnSurface = Color(0xFF1C1B1F)         // Dark gray
val SurfaceVariant = Color(0xFFE7E0EC)    // Light purple-gray
val OnSurfaceVariant = Color(0xFF49454F)  // Medium gray

// Surface Tonal Variants (untuk depth & hierarchy)
val SurfaceDim = Color(0xFFDED8E1)        // Dimmed surface
val SurfaceBright = Color(0xFFFEF7FF)     // Brightest surface
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF7F2FA)
val SurfaceContainer = Color(0xFFF3EDF7)
val SurfaceContainerHigh = Color(0xFFECE6F0)
val SurfaceContainerHighest = Color(0xFFE6E0E9)

// Outline & Borders
val Outline = Color(0xFF79747E)           // Border & dividers
val OutlineVariant = Color(0xFFCAC4D0)    // Subtle dividers

// ============================================
// SEMANTIC COLORS - Status & Feedback
// ============================================
// Error - Red (gentle, not aggressive)
val Error = Color(0xFFD32F2F)             // Red 700 - Clear but not harsh
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFCDD2)    // Red 100
val OnErrorContainer = Color(0xFFB71C1C)  // Red 900

// Success - Green (confirmation, completed)
val Success = Color(0xFF388E3C)           // Green 700
val OnSuccess = Color(0xFFFFFFFF)
val SuccessContainer = Color(0xFFC8E6C9) // Green 100
val OnSuccessContainer = Color(0xFF1B5E20) // Green 900

// Warning - Amber (caution, low stock)
val Warning = Color(0xFFFF8F00)           // Amber 700
val OnWarning = Color(0xFF000000)
val WarningContainer = Color(0xFFFFECB3) // Amber 100
val OnWarningContainer = Color(0xFFFF6F00) // Amber 900

// Info - Blue (informational, neutral)
val Info = Color(0xFF1976D2)              // Blue 700
val OnInfo = Color(0xFFFFFFFF)
val InfoContainer = Color(0xFFBBDEFB)     // Blue 100
val OnInfoContainer = Color(0xFF0D47A1)   // Blue 900

// ============================================
// SPECIAL PURPOSE COLORS
// ============================================
// Transaction Status Colors
val PendingColor = Color(0xFFFFA726)      // Orange 400 - Pending
val PaidColor = Color(0xFF66BB6A)         // Green 400 - Paid
val CanceledColor = Color(0xFFEF5350)     // Red 400 - Canceled
val RefundedColor = Color(0xFF9575CD)     // Purple 300 - Refunded

// Category Colors (untuk badge kategori produk)
val CategoryFood = Color(0xFFFF7043)      // Deep Orange 400
val CategoryDrink = Color(0xFF42A5F5)     // Blue 400
val CategorySnack = Color(0xFFFFA726)     // Orange 400
val CategoryOther = Color(0xFF78909C)     // Blue Gray 400

// Cash Flow Colors (untuk laporan keuangan)
val IncomeColor = Color(0xFF66BB6A)       // Green - Income
val ExpenseColor = Color(0xFFEF5350)      // Red - Expense
val NetProfitColor = Color(0xFF29B6F6)    // Light Blue - Net

// Scrim & Overlays
val Scrim = Color(0x99000000)             // 60% black overlay
val ScrimLight = Color(0x4D000000)        // 30% black overlay

// ============================================
// DARK THEME COLORS
// ============================================
// Dark Theme Palette (softer colors untuk dark mode)
val DarkPrimary = Color(0xFF80CBC4)           // Teal 200 - Softer untuk dark
val DarkOnPrimary = Color(0xFF00363A)         // Dark teal
val DarkPrimaryContainer = Color(0xFF004D40)  // Teal 900
val DarkOnPrimaryContainer = Color(0xFFB2DFDB) // Teal 100

val DarkSecondary = Color(0xFFFFCC80)         // Orange 200
val DarkOnSecondary = Color(0xFF4D2C00)
val DarkSecondaryContainer = Color(0xFFE65100)
val DarkOnSecondaryContainer = Color(0xFFFFE0B2)

val DarkTertiary = Color(0xFFB39DDB)          // Deep Purple 200
val DarkOnTertiary = Color(0xFF2A1A4D)
val DarkTertiaryContainer = Color(0xFF311B92)
val DarkOnTertiaryContainer = Color(0xFFD1C4E9)

val DarkBackground = Color(0xFF1C1B1F)        // Dark gray (bukan pure black)
val DarkOnBackground = Color(0xFFE6E1E5)
val DarkSurface = Color(0xFF1C1B1F)
val DarkOnSurface = Color(0xFFE6E1E5)
val DarkSurfaceVariant = Color(0xFF49454F)
val DarkOnSurfaceVariant = Color(0xFFCAC4D0)

val DarkSurfaceContainerLowest = Color(0xFF0F0D13)
val DarkSurfaceContainerLow = Color(0xFF1D1B20)
val DarkSurfaceContainer = Color(0xFF211F26)
val DarkSurfaceContainerHigh = Color(0xFF2B2930)
val DarkSurfaceContainerHighest = Color(0xFF36343B)

val DarkOutline = Color(0xFF938F99)
val DarkOutlineVariant = Color(0xFF49454F)

val DarkError = Color(0xFFEF5350)             // Red 400 - Softer untuk dark
val DarkOnError = Color(0xFF370B1E)
val DarkErrorContainer = Color(0xFF8C1D18)
val DarkOnErrorContainer = Color(0xFFFFCDD2)
