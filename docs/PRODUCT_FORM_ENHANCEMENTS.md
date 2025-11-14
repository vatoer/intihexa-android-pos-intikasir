# Product Form Enhancements

## Summary
This update introduces modular components and improved UX for the Product Form:
- Barcode scan trigger (UI event `ScanBarcode` + `BarcodeScanned` result)
- Indonesian thousand separators for price and cost inputs
- Local image storage with pick, capture, remove, and future crop capability
- ImageRepository abstraction for saving images locally
- Permissions added for CAMERA and reading images

## New Files
- `ImageRepository.kt` / `ImageRepositoryImpl` for local image persistence
- `ImageModule.kt` for DI provisioning
- `ProductFormComponents.kt` reusable composables

## Updated
- `ProductFormUiState` adds rawPrice/rawCost, imagePreviewUri, isImageProcessing
- `ProductFormUiEvent` adds ImagePicked, ImageCropped, RemoveImage, BarcodeScanned, CaptureImage
- `ProductFormViewModel` now handles image save & delete and price formatting
- `AndroidManifest.xml` now includes required permissions

## How to Integrate Barcode Scanner
1. Create a composable `BarcodeScannerSheet` using CameraX Preview + ImageAnalysis.
2. On detection, call `viewModel.onEvent(ProductFormUiEvent.BarcodeScanned(value))` and dismiss sheet.
3. Show sheet when `ScanBarcode` event is received (e.g., hold a local `showScanner` state in the screen).

## How to Integrate Image Picking & Cropping
Use Activity Result APIs in the host Activity/Navigation layer:
- Gallery: `ActivityResultContracts.GetContent()` with `image/*`
- Camera: `ActivityResultContracts.TakePicture()` using a temporary URI
- Cropping: integrate library (e.g., uCrop). After crop result, dispatch `ImageCropped(uri)`.

## Persistence Strategy
- Images saved to internal app storage: `filesDir/images/img_timestamp.jpg`
- Path stored in `Product.imageUrl` (later rename to `imagePath` if desired)
- Deleting/Replacing images cleans up old files

## Price Formatting
- Raw numeric digits stored separately (`rawPrice`, `rawCost`)
- Display uses grouped thousands with `.`; prefix `Rp`
- Validation uses raw values to avoid parsing issues

## Next Steps (Optional)
- Add cropping UI
- Add compression before saving images
- Migrate `imageUrl` column name to `imagePath` (requires Room migration)
- Add barcode duplicate validation
- Add unit tests for ImageRepository

## Example Scanner Flow (Pseudo)
```kotlin
var showScanner by remember { mutableStateOf(false) }
if (showScanner) BarcodeScannerSheet(
    onResult = { code ->
        viewModel.onEvent(ProductFormUiEvent.BarcodeScanned(code))
        showScanner = false
    },
    onClose = { showScanner = false }
)
// In trailing icon click:
IconButton(onClick = { showScanner = true }) { ... }
```

## Edge Cases Handled
- Empty price/cost inputs
- Removing image deletes file
- Replacing image deletes old file
- Invalid numeric conversion guarded with `toDoubleOrNull()`

## Testing Checklist
- Input price: `1234567` displays `1.234.567`
- Save product with image selected: file exists in internal storage
- Remove image: file deleted
- Edit product: existing image loads as preview
- Barcode scan event sets barcode field

---
This follows clean architecture principles and keeps UI logic separated from persistence concerns.

