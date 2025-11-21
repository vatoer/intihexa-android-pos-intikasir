#!/bin/bash

# Script untuk Generate RSA Key Pair untuk Sistem Aktivasi IntiKasir
#
# Usage:
#   chmod +x generate-keys.sh
#   ./generate-keys.sh

set -e

echo "======================================================"
echo "  IntiKasir Activation Key Generator"
echo "======================================================"
echo ""

# Create keys directory if not exists
KEYS_DIR="./activation-keys"
if [ ! -d "$KEYS_DIR" ]; then
    mkdir -p "$KEYS_DIR"
    echo "✓ Created directory: $KEYS_DIR"
fi

# Generate private key
PRIVATE_KEY="$KEYS_DIR/private_key.pem"
PUBLIC_KEY="$KEYS_DIR/public_key.pem"
PUBLIC_KEY_BASE64="$KEYS_DIR/public_key_base64.txt"

echo ""
echo "Generating RSA 2048-bit key pair..."
echo ""

# Generate private key (2048-bit)
openssl genrsa -out "$PRIVATE_KEY" 2048 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✓ Private key generated: $PRIVATE_KEY"
else
    echo "✗ Failed to generate private key"
    exit 1
fi

# Extract public key
openssl rsa -in "$PRIVATE_KEY" -pubout -out "$PUBLIC_KEY" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✓ Public key extracted: $PUBLIC_KEY"
else
    echo "✗ Failed to extract public key"
    exit 1
fi

# Convert public key to Base64 (for Android)
grep -v "BEGIN\|END" "$PUBLIC_KEY" | tr -d '\n' > "$PUBLIC_KEY_BASE64"
echo "✓ Public key Base64: $PUBLIC_KEY_BASE64"

echo ""
echo "======================================================"
echo "  Keys Generated Successfully!"
echo "======================================================"
echo ""
echo "Private Key: $PRIVATE_KEY"
echo "  ⚠️  KEEP THIS SECRET! Use on server only"
echo ""
echo "Public Key: $PUBLIC_KEY"
echo "  → Embed this in Android app"
echo ""
echo "Base64 Public Key: $PUBLIC_KEY_BASE64"
echo "  → Copy this to SignatureVerifier.kt"
echo ""
echo "======================================================"
echo ""
echo "Next Steps:"
echo ""
echo "1. Copy Base64 public key to Android:"
echo "   File: app/src/main/java/id/stargan/intikasir/data/security/SignatureVerifier.kt"
echo ""
echo "   private const val PUBLIC_KEY_BASE64 = \"\"\""
cat "$PUBLIC_KEY_BASE64"
echo ""
echo "   \"\"\""
echo ""
echo "2. Copy private key to server:"
echo "   cp $PRIVATE_KEY /path/to/your/server/keys/"
echo ""
echo "3. NEVER commit private key to git!"
echo "   Add to .gitignore:"
echo "   activation-keys/"
echo ""
echo "======================================================"
echo ""

# Create example .gitignore
GITIGNORE="$KEYS_DIR/.gitignore"
cat > "$GITIGNORE" << 'EOF'
# NEVER commit private keys!
*.pem
private_key*
EOF

echo "✓ Created .gitignore in $KEYS_DIR"
echo ""

# Show public key content for easy copy
echo "Copy this to SignatureVerifier.kt:"
echo "======================================================"
echo "private const val PUBLIC_KEY_BASE64 = \"\"\""
cat "$PUBLIC_KEY_BASE64"
echo "\"\"\""
echo "======================================================"

