# Quick Color Usage Guide - IntiKasir

## 🎨 Cara Menggunakan Warna Baru

### 1. Akses Warna Utama dari MaterialTheme

```kotlin
@Composable
fun MyScreen() {
    // Primary color
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    
    // Secondary color
    val secondary = MaterialTheme.colorScheme.secondary
    
    // Surface & Background
    val surface = MaterialTheme.colorScheme.surface
    val background = MaterialTheme.colorScheme.background
}
```

### 2. Akses Extended Colors (Success, Warning, Info)

```kotlin
@Composable
fun MyScreen() {
    // Success colors
    val success = MaterialTheme.colorScheme.extendedColors.success
    val successContainer = MaterialTheme.colorScheme.extendedColors.successContainer
    
    // Warning colors
    val warning = MaterialTheme.colorScheme.extendedColors.warning
    val warningContainer = MaterialTheme.colorScheme.extendedColors.warningContainer
    
    // Info colors
    val info = MaterialTheme.colorScheme.extendedColors.info
    
    // Transaction status
    val paidColor = MaterialTheme.colorScheme.extendedColors.paidColor
    val pendingColor = MaterialTheme.colorScheme.extendedColors.pendingColor
}
```

## 📦 Contoh Implementasi

### TopAppBar dengan Warna Baru

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar() {
    TopAppBar(
        title = { Text("IntiKasir") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
```

### Button dengan Warna Primary

```kotlin
@Composable
fun PrimaryButton() {
    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text("Bayar")
    }
}
```

### Card dengan Success/Warning/Error State

```kotlin
@Composable
fun StatusCard(status: String) {
    val (containerColor, contentColor) = when (status) {
        "success" -> Pair(
            MaterialTheme.colorScheme.extendedColors.successContainer,
            MaterialTheme.colorScheme.extendedColors.onSuccessContainer
        )
        "warning" -> Pair(
            MaterialTheme.colorScheme.extendedColors.warningContainer,
            MaterialTheme.colorScheme.extendedColors.onWarningContainer
        )
        else -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text("Status: $status")
    }
}
```

### FAB (Floating Action Button)

```kotlin
@Composable
fun AddProductFAB() {
    FloatingActionButton(
        onClick = { },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Icon(Icons.Default.Add, contentDescription = "Tambah")
    }
}
```

### Discount Badge (menggunakan Secondary)

```kotlin
@Composable
fun DiscountBadge(discount: Int) {
    AssistChip(
        onClick = { },
        label = { Text("Diskon $discount%") },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}
```

### Transaction Status Badge

```kotlin
@Composable
fun TransactionStatusBadge(status: TransactionStatus) {
    val (backgroundColor, textColor) = when (status) {
        TransactionStatus.PAID -> Pair(
            MaterialTheme.colorScheme.extendedColors.paidColor,
            Color.White
        )
        TransactionStatus.PENDING -> Pair(
            MaterialTheme.colorScheme.extendedColors.pendingColor,
            Color.White
        )
        TransactionStatus.CANCELED -> Pair(
            MaterialTheme.colorScheme.extendedColors.canceledColor,
            Color.White
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.name,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
```

### Filter Chip (Active/Inactive)

```kotlin
@Composable
fun CategoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
```

### Stock Status Indicator

```kotlin
@Composable
fun StockStatusIndicator(stock: Int, lowThreshold: Int) {
    val (color, text) = when {
        stock == 0 -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            "Habis"
        )
        stock <= lowThreshold -> Pair(
            MaterialTheme.colorScheme.extendedColors.warningContainer,
            "Stok Menipis ($stock)"
        )
        else -> Pair(
            MaterialTheme.colorScheme.extendedColors.successContainer,
            "Tersedia ($stock)"
        )
    }
    
    AssistChip(
        onClick = { },
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color
        )
    )
}
```

### Home Screen Menu Card dengan Gradient

```kotlin
@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
```

### Total Revenue Card (dengan Primary Container)

```kotlin
@Composable
fun TotalRevenueCard(amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Total Pendapatan Hari Ini",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rp ${formatCurrency(amount)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

### Expense Summary Card (Error Container untuk pengeluaran)

```kotlin
@Composable
fun ExpenseSummaryCard(amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Total Pengeluaran",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rp ${formatCurrency(amount)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

## 🎯 Referensi Cepat Warna per Konteks

| Konteks | Warna yang Digunakan |
|---------|---------------------|
| **CTA Utama** | `primary` / `primaryContainer` |
| **Aksen/Promo** | `secondary` / `secondaryContainer` |
| **Admin/Premium** | `tertiary` / `tertiaryContainer` |
| **Sukses** | `extendedColors.success` / `successContainer` |
| **Peringatan** | `extendedColors.warning` / `warningContainer` |
| **Error** | `error` / `errorContainer` |
| **Informasi** | `extendedColors.info` / `infoContainer` |
| **Background** | `background` atau `surface` |
| **Card** | `surface` atau `surfaceContainerLow` |
| **Border** | `outline` atau `outlineVariant` |

## ⚡ Tips

1. **Gunakan container colors** untuk background yang subtle
2. **On colors** selalu untuk text di atas container
3. **Surface variants** untuk depth & hierarchy
4. **Outline variant** untuk divider yang halus
5. **Extended colors** untuk semantic meanings

## 🔍 Testing Checklist

- [ ] Cek contrast ratio text (min 4.5:1)
- [ ] Test di brightness tinggi (outdoor)
- [ ] Test di mode gelap
- [ ] Test dengan color blind simulator
- [ ] Test penggunaan 2+ jam (eye strain?)

---

Dokumentasi lengkap: `/docs/ai-color-guidance.md`

