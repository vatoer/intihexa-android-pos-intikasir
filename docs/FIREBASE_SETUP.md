# Firebase Configuration

## Setup Instructions

1. **Buat Firebase Project**
   - Buka [Firebase Console](https://console.firebase.google.com)
   - Klik "Add Project"
   - Ikuti wizard setup

2. **Tambahkan Android App**
   - Klik "Add App" → Android
   - Package name: `id.stargan.intikasir`
   - Download `google-services.json`

3. **Enable Firestore**
   - Pergi ke Firestore Database
   - Klik "Create Database"
   - Pilih mode: Start in **test mode** (untuk development)

4. **Enable Authentication** (Optional)
   - Pergi ke Authentication
   - Enable Email/Password atau Anonymous

5. **Setup Security Rules**

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // License collection (read-only for clients)
    match /licenses/{licenseId} {
      allow read: if request.auth != null;
      allow write: if false; // Only via Firebase Functions
    }
    
    // Store data (authenticated users only)
    match /stores/{storeId} {
      allow read, write: if request.auth != null;
      
      // Sub-collections
      match /products/{productId} {
        allow read, write: if request.auth != null;
      }
      
      match /categories/{categoryId} {
        allow read, write: if request.auth != null;
      }
      
      match /transactions/{transactionId} {
        allow read, write: if request.auth != null;
        
        match /items/{itemId} {
          allow read, write: if request.auth != null;
        }
      }
      
      match /users/{userId} {
        allow read, write: if request.auth != null;
      }
    }
  }
}
```

### Firestore Collections Structure

```
/licenses/{licenseCode}
  - code: string
  - isActive: boolean
  - deviceId: string | null
  - activatedAt: timestamp
  - expiresAt: timestamp | null
  - createdAt: timestamp

/stores/{storeId}
  - storeName: string
  - ownerId: string
  - licenseCode: string
  - createdAt: timestamp
  
  /stores/{storeId}/products/{productId}
    - name: string
    - price: number
    - stock: number
    - ...
  
  /stores/{storeId}/categories/{categoryId}
    - name: string
    - ...
  
  /stores/{storeId}/transactions/{transactionId}
    - transactionNumber: string
    - total: number
    - ...
    
    /stores/{storeId}/transactions/{transactionId}/items/{itemId}
      - productId: string
      - quantity: number
      - ...
  
  /stores/{storeId}/users/{userId}
    - name: string
    - role: string
    - ...
```

## Firebase Functions (License Validation)

### Install Firebase CLI

```bash
npm install -g firebase-tools
firebase login
firebase init functions
```

### Function: Activate License

```javascript
// functions/index.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.activateLicense = functions.https.onCall(async (data, context) => {
  const { licenseCode, deviceId } = data;
  
  // Validate input
  if (!licenseCode || !deviceId) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'License code and device ID are required'
    );
  }
  
  const db = admin.firestore();
  const licenseRef = db.collection('licenses').doc(licenseCode);
  
  try {
    const licenseDoc = await licenseRef.get();
    
    // Check if license exists
    if (!licenseDoc.exists) {
      throw new functions.https.HttpsError(
        'not-found',
        'Kode lisensi tidak ditemukan'
      );
    }
    
    const license = licenseDoc.data();
    
    // Check if license is active
    if (!license.isActive) {
      throw new functions.https.HttpsError(
        'permission-denied',
        'Kode lisensi tidak aktif'
      );
    }
    
    // Check if already activated on another device
    if (license.deviceId && license.deviceId !== deviceId) {
      throw new functions.https.HttpsError(
        'already-exists',
        'Kode lisensi sudah digunakan pada perangkat lain'
      );
    }
    
    // Check if expired
    if (license.expiresAt && license.expiresAt.toDate() < new Date()) {
      throw new functions.https.HttpsError(
        'permission-denied',
        'Kode lisensi sudah kadaluarsa'
      );
    }
    
    // Activate license
    await licenseRef.update({
      deviceId: deviceId,
      activatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    return {
      success: true,
      message: 'Aktivasi berhasil',
      expiresAt: license.expiresAt
    };
    
  } catch (error) {
    console.error('Activation error:', error);
    throw error;
  }
});
```

### Deploy Functions

```bash
firebase deploy --only functions
```

## Alternative: REST API

Jika tidak menggunakan Firebase Functions, buat REST API dengan endpoint:

### POST /api/licenses/activate

**Request:**
```json
{
  "code": "XXXX-XXXX-XXXX-XXXX",
  "deviceId": "android-device-id"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Aktivasi berhasil",
  "expiresAt": "2025-12-31T23:59:59Z"
}
```

**Response (Error):**
```json
{
  "success": false,
  "message": "Kode lisensi tidak valid"
}
```

## Environment Variables

Create `local.properties` file:

```properties
# Firebase
firebase.api.key=YOUR_API_KEY
firebase.project.id=YOUR_PROJECT_ID

# License API (if using REST)
license.api.url=https://api.intikasir.com
license.api.key=YOUR_API_KEY
```

## Testing

### Create Test License

```javascript
// Run in Firebase Console or Functions
db.collection('licenses').doc('TEST-1111-2222-3333').set({
  code: 'TEST-1111-2222-3333',
  isActive: true,
  deviceId: null,
  activatedAt: null,
  expiresAt: null, // No expiration for test
  createdAt: admin.firestore.FieldValue.serverTimestamp()
});
```

### Test Activation

```kotlin
// In Android app
val result = licenseRepository.activateLicense(
  code = "TEST-1111-2222-3333",
  deviceId = Settings.Secure.getString(
    contentResolver,
    Settings.Secure.ANDROID_ID
  )
)
```

---

**IMPORTANT:** 
- Jangan commit `google-services.json` ke Git
- Tambahkan ke `.gitignore`
- Gunakan environment-specific config untuk production

