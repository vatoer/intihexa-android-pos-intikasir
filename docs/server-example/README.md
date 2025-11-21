# IntiKasir Activation Server

Production-ready activation server untuk sistem aktivasi IntiKasir POS.

## Features

- ✅ RSA 2048-bit signature verification
- ✅ PostgreSQL database
- ✅ Rate limiting (5 requests/hour per IP)
- ✅ Security headers (Helmet.js)
- ✅ Audit logging
- ✅ CORS support
- ✅ Graceful shutdown
- ✅ Error handling
- ✅ Admin endpoints

## Prerequisites

- Node.js 16+ 
- PostgreSQL 12+
- OpenSSL (for key generation)

## Setup

### 1. Install Dependencies

```bash
npm install
```

### 2. Generate RSA Keys

```bash
cd ../
chmod +x generate-keys.sh
./generate-keys.sh
```

Salin `private_key.pem` ke folder `keys/`:
```bash
mkdir -p server-example/keys
cp activation-keys/private_key.pem server-example/keys/
```

⚠️ **IMPORTANT**: Never commit `private_key.pem` to git!

### 3. Setup Database

```bash
# Login ke PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE intikasir_activation;

# Connect to database
\c intikasir_activation

# Run schema
\i schema.sql
```

### 4. Configure Environment

```bash
cp .env.example .env
nano .env
```

Update configuration:
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=intikasir_activation
DB_USER=postgres
DB_PASSWORD=your_secure_password
```

### 5. Start Server

Development:
```bash
npm run dev
```

Production:
```bash
npm start
```

## API Endpoints

### POST /api/activate

Activate a device with serial number.

**Request:**
```json
{
  "serialNumber": "SN-BASIC-DEMO-001",
  "deviceId": "abc123def456"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Aktivasi berhasil! Aplikasi siap digunakan.",
  "signature": "BASE64_ENCODED_SIGNATURE...",
  "expiry": 1735689600000
}
```

**Response (Error):**
```json
{
  "success": false,
  "message": "Serial Number tidak valid",
  "signature": null,
  "expiry": null
}
```

### GET /api/health

Health check endpoint.

**Response:**
```json
{
  "status": "ok",
  "timestamp": 1700000000000,
  "database": "connected"
}
```

### GET /admin/activations

List all activations (last 100).

**Response:**
```json
{
  "total": 10,
  "activations": [
    {
      "id": 1,
      "serial_number": "SN-BASIC-DEMO-001",
      "device_id": "abc123",
      "tier": "basic",
      "activated_at": "2024-01-01T00:00:00Z",
      "expiry_timestamp": 1735689600000,
      "is_active": true,
      "days_remaining": 365
    }
  ]
}
```

### POST /admin/generate-sn

Generate new serial numbers.

**Request:**
```json
{
  "tier": "basic",
  "count": 10,
  "expiryDays": 365
}
```

**Response:**
```json
{
  "success": true,
  "count": 10,
  "generated": [
    {
      "serialNumber": "SN-BASIC-1700000000-A1B2C3D4",
      "tier": "basic",
      "expiryDate": "2025-01-01T00:00:00Z"
    }
  ]
}
```

### POST /admin/deactivate

Deactivate a device.

**Request:**
```json
{
  "deviceId": "abc123",
  "reason": "License violation"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Device berhasil dinonaktifkan"
}
```

## Tiers

- **trial**: 1 month
- **basic**: 12 months
- **pro**: 12 months
- **enterprise**: 24 months

## Rate Limiting

- Activation endpoint: 5 requests per hour per IP
- Other endpoints: No limit (add if needed)

## Database Schema

### Tables

1. **serial_numbers** - Generated serial numbers
2. **activations** - Device activations
3. **activation_audit_logs** - Audit trail

### Views

1. **active_activations** - Currently active devices
2. **available_serial_numbers** - Unused serial numbers
3. **activation_statistics** - Activation stats

## Security

### Headers (Helmet.js)
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Strict-Transport-Security: max-age=31536000

### CORS
Configure allowed origins in `.env`:
```env
ALLOWED_ORIGINS=https://yourdomain.com,https://admin.yourdomain.com
```

### Private Key
- Keep `private_key.pem` secure
- Use environment variables in production
- Rotate keys annually

## Monitoring

### Audit Logs

All activation attempts are logged:
```sql
SELECT * FROM activation_audit_logs 
ORDER BY created_at DESC 
LIMIT 100;
```

### Statistics

```sql
SELECT * FROM activation_statistics;
```

### Cleanup Expired

Run periodically (cron job):
```sql
SELECT cleanup_expired_activations();
```

## Deployment

### Production Checklist

- [ ] Generate production RSA keys
- [ ] Setup PostgreSQL with SSL
- [ ] Configure firewall
- [ ] Setup HTTPS/SSL certificate
- [ ] Configure environment variables
- [ ] Setup process manager (PM2)
- [ ] Setup monitoring (Datadog, etc)
- [ ] Configure backups
- [ ] Test activation flow end-to-end

### Using PM2

```bash
npm install -g pm2

# Start server
pm2 start server.js --name intikasir-activation

# Enable startup script
pm2 startup
pm2 save

# Monitor
pm2 monit

# Logs
pm2 logs intikasir-activation
```

### Nginx Reverse Proxy

```nginx
server {
    listen 80;
    server_name activation.yourdomain.com;
    
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_cache_bypass $http_upgrade;
    }
}
```

## Backup

### Database Backup

Daily backup:
```bash
#!/bin/bash
BACKUP_DIR="/backups/intikasir"
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -U postgres intikasir_activation > $BACKUP_DIR/backup_$DATE.sql
```

### Restore

```bash
psql -U postgres intikasir_activation < backup_20240101_120000.sql
```

## Troubleshooting

### Private key not found
```
✗ Failed to load private key
```
**Solution**: Copy `private_key.pem` to `./keys/` folder

### Database connection error
```
Error: connect ECONNREFUSED
```
**Solution**: Check PostgreSQL is running and credentials are correct

### Rate limit exceeded
```
Terlalu banyak percobaan aktivasi
```
**Solution**: Wait 1 hour or whitelist IP in code

## Support

For issues or questions, contact the development team.

## License

Proprietary - IntiKasir POS System

