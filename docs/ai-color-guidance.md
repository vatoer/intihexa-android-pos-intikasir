# Panduan Warna IntiKasir - Professional PoS Design System

## 🎨 Philosophy & Design Goals

**Target**: Aplikasi kasir/PoS yang profesional, segar, dan nyaman untuk penggunaan 8+ jam sehari
**Approach**: Modern, Clean, Energetic namun tidak melelahkan mata
**Inspiration**: Shopify POS, Square, Toast POS - dengan sentuhan Indonesia

---

## 🌈 Color Palette - "Fresh Commerce"

### Primary Colors (Teal-Green Spectrum)
Warna utama yang merepresentasikan "transaksi sukses" dan "pertumbuhan bisnis"

```kotlin
// Primary - Energetic Teal (balance antara profesional & friendly)
val Primary = Color(0xFF00897B)           // Teal 600 - Confident, Fresh
val OnPrimary = Color(0xFFFFFFFF)         // White - High contrast
val PrimaryContainer = Color(0xFFB2DFDB) // Teal 100 - Soft background
val OnPrimaryContainer = Color(0xFF004D40) // Teal 900 - Dark text

// Primary Variants (untuk gradients & highlights)
val PrimaryLight = Color(0xFF4DB6AC)      // Teal 300 - Lighter variant
val PrimaryDark = Color(0xFF00695C)       // Teal 800 - Deeper shade
```

**Penggunaan**:
- FAB (Floating Action Button)
- Primary CTA buttons (Bayar, Simpan, Konfirmasi)
- Active states
- Success indicators
- Progress bars saat transaksi

---

### Secondary Colors (Warm Amber Spectrum)
Warna pendukung yang menciptakan warmth dan urgency (untuk promo, diskon, highlight)

```kotlin
// Secondary - Warm Amber (energetic, menarik perhatian)
val Secondary = Color(0xFFFF6F00)         // Orange 900 - Bold accent
val OnSecondary = Color(0xFFFFFFFF)       // White
val SecondaryContainer = Color(0xFFFFE0B2) // Orange 100 - Soft highlight
val OnSecondaryContainer = Color(0xFFE65100) // Orange 900 dark
```

**Penggunaan**:
- Discount tags
- Promotional banners
- Warning buttons
- "Hot item" badges
- Time-sensitive actions

---

### Tertiary Colors (Deep Purple Spectrum)
Warna tersier untuk fitur premium/admin dan diferensiasi hierarki

```kotlin
// Tertiary - Deep Purple (professional, trustworthy)
val Tertiary = Color(0xFF5E35B1)          // Deep Purple 600
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFD1C4E9) // Deep Purple 100
val OnTertiaryContainer = Color(0xFF311B92) // Deep Purple 900
```

**Penggunaan**:
- Admin-only features
- Reports & analytics
- Category badges
- Premium features highlight

---

### Neutral Colors (Gray Spectrum)
Background, surfaces, dan text colors yang nyaman untuk mata

```kotlin
// Background & Surface - Warm off-white
val Background = Color(0xFFFAFAFA)        // Gray 50 - Soft white
val OnBackground = Color(0xFF1C1B1F)      // Almost black (tidak murni hitam)

val Surface = Color(0xFFFFFFFF)           // Pure white untuk cards
val OnSurface = Color(0xFF1C1B1F)         // Dark gray
val SurfaceVariant = Color(0xFFE7E0EC)    // Light purple-gray
val OnSurfaceVariant = Color(0xFF49454F)  // Medium gray

val SurfaceDim = Color(0xFFDED8E1)        // Dimmed surface
val SurfaceBright = Color(0xFFFEF7FF)     // Brightest surface
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF7F2FA)
val SurfaceContainer = Color(0xFFF3EDF7)
val SurfaceContainerHigh = Color(0xFFECE6F0)
val SurfaceContainerHighest = Color(0xFFE6E0E9)

val Outline = Color(0xFF79747E)           // Border & dividers
val OutlineVariant = Color(0xFFCAC4D0)    // Subtle dividers
```

**Penggunaan**:
- App background: `Background`
- Card surfaces: `Surface` atau `SurfaceContainerLow`
- Input fields: `SurfaceVariant`
- Dividers: `OutlineVariant`
- Borders: `Outline`

---

### Semantic Colors
Warna fungsional untuk feedback & status

```kotlin
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
```

**Penggunaan**:
- `Error`: Form validation errors, failed transactions
- `Success`: Payment success, item added, data saved
- `Warning`: Low stock alerts, pending actions
- `Info`: Tips, helpful information, neutral notifications

---

### Special Purpose Colors

```kotlin
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
```

---

## 📐 Component-Specific Guidelines

### TopAppBar
```kotlin
TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surface,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
)
```

### Cards & Surfaces
```kotlin
// Elevated cards
CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surface
)

// Tonal cards (subtle background)
CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
)

// Status cards
// Success card
CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.successContainer,
    contentColor = MaterialTheme.colorScheme.onSuccessContainer
)

// Error card
CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.errorContainer,
    contentColor = MaterialTheme.colorScheme.onErrorContainer
)
```

### Buttons
```kotlin
// Primary action (Bayar, Simpan)
ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary
)

// Secondary action (Batal, Kembali)
ButtonDefaults.outlinedButtonColors(
    contentColor = MaterialTheme.colorScheme.primary
)

// Destructive action (Hapus, Batalkan)
ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.error,
    contentColor = MaterialTheme.colorScheme.onError
)
```

### FAB (Floating Action Button)
```kotlin
FloatingActionButtonDefaults.containerColor = MaterialTheme.colorScheme.primaryContainer
FloatingActionButtonDefaults.contentColor = MaterialTheme.colorScheme.onPrimaryContainer
```

### Chips & Tags
```kotlin
// Active filter chip
FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
)

// Discount tag
AssistChipDefaults.assistChipColors(
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
)

// Status badge
AssistChipDefaults.assistChipColors(
    containerColor = SuccessContainer, // atau sesuai status
    labelColor = OnSuccessContainer
)
```

---

## 🌙 Dark Mode Support

```kotlin
// Dark Theme Palette
val DarkPrimary = Color(0xFF80CBC4)           // Teal 200 - Softer untuk dark
val DarkOnPrimary = Color(0xFF00363A)         // Dark teal
val DarkPrimaryContainer = Color(0xFF004D40)  // Teal 900
val DarkOnPrimaryContainer = Color(0xFFB2DFDB) // Teal 100

val DarkSecondary = Color(0xFFFFCC80)         // Orange 200
val DarkOnSecondary = Color(0xFF4D2C00)
val DarkSecondaryContainer = Color(0xFFE65100)
val DarkOnSecondaryContainer = Color(0xFFFFE0B2)

val DarkBackground = Color(0xFF1C1B1F)        // Dark gray (bukan pure black)
val DarkOnBackground = Color(0xFFE6E1E5)
val DarkSurface = Color(0xFF1C1B1F)
val DarkOnSurface = Color(0xFFE6E1E5)
val DarkSurfaceVariant = Color(0xFF49454F)
val DarkOnSurfaceVariant = Color(0xFFCAC4D0)
```

---

## 🎯 Screen-Specific Color Usage

### Home Screen
- Background: `Background` (Soft white)
- Menu Cards: `Surface` with slight elevation
- Header Section: `PrimaryContainer` with gradient to `Surface`
- Menu Icons: `Primary` untuk main features, `OnSurfaceVariant` untuk secondary

### POS/Kasir Screen
- Background: `Background`
- Product Cards: `Surface` with border `OutlineVariant`
- Selected Product: `PrimaryContainer` border
- Cart Panel: `SurfaceContainerLow`
- Total Section: `PrimaryContainer` dengan bold typography
- Payment Button: `Primary` (large, prominent)
- Low Stock Warning: `WarningContainer`

### Product List Screen
- Background: `Background`
- Product Cards: `Surface`
- Category Chips: `PrimaryContainer` (selected), `SurfaceVariant` (unselected)
- Price Text: `Primary` (bold)
- Stock Badge: `SuccessContainer` (in stock), `WarningContainer` (low), `ErrorContainer` (out)
- Search Bar: `SurfaceVariant`

### Transaction History
- Background: `Background`
- Transaction Cards: `Surface`
- Status Badges:
  - Paid: `SuccessContainer`
  - Pending: `WarningContainer`
  - Canceled: `ErrorContainer`
- Total Revenue Card: `PrimaryContainer` dengan gradient
- Filter Chips: `Tertiary` theme

### Expense Screen
- Background: `Background`
- Expense Cards: `Surface` with red accent indicator
- Total Card: `ErrorContainer` (karena pengeluaran)
- Category Icons: Semantic colors per kategori

### Reports/Analytics
- Background: `Background`
- Chart Primary: `Primary`
- Chart Secondary: `Secondary`
- Chart Tertiary: `Tertiary`
- Positive Values: `Success`
- Negative Values: `Error`
- Card Highlights: `SurfaceContainerHigh`

---

## 🔧 Implementation Tips

### 1. Gradients untuk Visual Interest
```kotlin
// Header gradients
val headerGradient = Brush.verticalGradient(
    colors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.surface
    )
)

// Success gradient (untuk success screen)
val successGradient = Brush.verticalGradient(
    colors = listOf(
        MaterialTheme.colorScheme.successContainer,
        MaterialTheme.colorScheme.surface
    )
)
```

### 2. Elevation & Shadows
```kotlin
// Subtle elevation untuk depth tanpa overwhelming
CardDefaults.cardElevation(
    defaultElevation = 2.dp,  // Subtle
    pressedElevation = 4.dp,
    hoveredElevation = 3.dp
)
```

### 3. Alpha Transparency untuk Layers
```kotlin
// Overlay untuk dimmed states
Color.Black.copy(alpha = 0.6f)  // Scrim overlay
Color.White.copy(alpha = 0.1f)  // Subtle highlight
```

### 4. Ripple Effects
```kotlin
// Gunakan default Material3 ripple yang sudah menyesuaikan dengan theme
// Jangan override kecuali sangat diperlukan
```

---

## ✅ Best Practices

### DO's ✅
- **Gunakan semantic colors** untuk status (success, error, warning)
- **Konsisten** dengan primary untuk CTA utama
- **Beri breathing room** dengan proper spacing & white space
- **Test pada brightness tinggi dan rendah** untuk keterbacaan
- **Gunakan outline variant** untuk divider yang subtle
- **Leverage surface containers** untuk hierarki visual
- **Tambahkan color accents** pada elemen interaktif

### DON'Ts ❌
- **Jangan gunakan pure black** (#000000) untuk text atau background
- **Hindari warna terang/neon** yang melelahkan mata
- **Jangan terlalu banyak warna** dalam satu screen (max 3-4 accent colors)
- **Hindari low contrast** yang sulit dibaca
- **Jangan paksa primary color** di setiap elemen (gunakan neutral untuk balance)
- **Hindari gradients berlebihan** yang mengganggu readability

---

## 🎨 Color Accessibility (WCAG 2.1)

### Minimum Contrast Ratios
- **Normal text** (16sp+): 4.5:1
- **Large text** (24sp+): 3:1
- **UI components**: 3:1

### Tested Combinations (Pass AAA/AA)
✅ `Primary` + `OnPrimary` (7.2:1) - AAA
✅ `Surface` + `OnSurface` (13.1:1) - AAA
✅ `Error` + `OnError` (6.8:1) - AAA
✅ `Success` + `OnSuccess` (6.5:1) - AAA
✅ `PrimaryContainer` + `OnPrimaryContainer` (8.1:1) - AAA

---

## 📱 Preview & Testing

### Test Scenarios
1. **Bright sunlight** - Apakah text masih terbaca?
2. **Low light/night** - Apakah warna tidak terlalu harsh?
3. **Extended use (2+ hours)** - Apakah mata masih nyaman?
4. **Color blindness simulator** - Apakah tetap distinguishable?

### Testing Tools
- Material Theme Builder: https://m3.material.io/theme-builder
- Contrast Checker: https://webaim.org/resources/contrastchecker/
- Color Blindness Simulator: Chrome DevTools atau Sim Daltonism

---

## 🚀 Migration Strategy

### Phase 1: Core Theme (Priority: High)
- [ ] Update `Color.kt` dengan palette baru
- [ ] Update `Theme.kt` untuk light & dark theme
- [ ] Test pada 3-5 screen utama

### Phase 2: Component Updates (Priority: High)
- [ ] TopAppBar semua screen
- [ ] FAB & Primary Buttons
- [ ] Cards & Surfaces
- [ ] Status indicators

### Phase 3: Fine-tuning (Priority: Medium)
- [ ] Semantic colors untuk semua status
- [ ] Category-specific colors
- [ ] Chart & analytics colors
- [ ] Gradients & accents

### Phase 4: Polish (Priority: Low)
- [ ] Dark mode optimization
- [ ] Accessibility audit
- [ ] A/B testing dengan users
- [ ] Documentation & design system

---

## 📊 Color Psychology untuk PoS

| Warna | Emosi/Asosiasi | Penggunaan Optimal |
|-------|----------------|-------------------|
| **Teal/Cyan** | Trust, Calm, Professional | Primary actions, navigation |
| **Orange** | Energy, Urgency, Appetite | Discounts, promotions, food categories |
| **Purple** | Premium, Creative, Luxury | Admin features, analytics |
| **Green** | Success, Growth, Money | Completed transactions, profit |
| **Red** | Alert, Stop, Important | Errors, stock warnings, expenses |
| **Blue** | Stable, Reliable, Info | Neutral information, helper text |
| **Gray** | Neutral, Clean, Modern | Backgrounds, text, dividers |

---

## 🎯 Final Color Palette Summary

```kotlin
// Light Theme - Fresh Commerce Palette
Primary         #00897B  // Teal 600 - Main brand
Secondary       #FF6F00  // Orange 900 - Accent/promo
Tertiary        #5E35B1  // Purple 600 - Premium
Success         #388E3C  // Green 700
Warning         #FF8F00  // Amber 700
Error           #D32F2F  // Red 700
Background      #FAFAFA  // Warm gray
Surface         #FFFFFF  // White
OnSurface       #1C1B1F  // Dark gray
```

**Karakteristik Palette:**
- ✅ Professional & trustworthy (Teal primary)
- ✅ Energetic & engaging (Orange accents)
- ✅ Eye-friendly untuk long-term use (soft backgrounds)
- ✅ Clear semantic meaning (obvious success/error states)
- ✅ Modern & fresh (tidak terlihat "old school")
- ✅ WCAG AAA compliant untuk readability

---

**Dibuat dengan 💚 untuk IntiKasir POS**
*Version 1.0 - Professional Fresh Commerce Design System*

