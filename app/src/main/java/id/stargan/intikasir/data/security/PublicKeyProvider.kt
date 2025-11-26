package id.stargan.intikasir.data.security

/**
 * Centralized public key provider used by SignatureVerifier and RsaEncryption.
 * Keep the key here so it only needs to be changed in one place.
 */
object PublicKeyProvider {
    // Public key dalam Base64 (contoh RSA 2048-bit)
    // GANTI dengan public key yang sesuai dengan private key di server
    // Ini adalah contoh public key untuk demo
    const val PUBLIC_KEY_BASE64: String = """
MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAqlfLZ3qDEWmMQ7ahMcNR
RaN6q7bCZfLpA7w+X0moBC1JYtIeXdJRMx+tn/jrrBUkIUcjJy2Jq5AveIWRzKRH
w4GfVIhanUR4zWmBrRZuchJAbrYKBC2euQqDvjHv8FQaMAZezy7wNQwlCEyZE8hC
n+pc4N/5VJhtyzoreCW/En9ES94FmzaZbhT7UD6h0HCRPXgnTSLp5VxwP6VImP/W
B5pWu1jkyfxJ0c+MJA3LIDFZbBcnyQgCWgqD7x+lk1lJV4lUOStZTWYTiIWzJ2Gk
XtAGHwzgTvLE4bn0ErI6S+yU3uCMhasOtHFCKO1SMqLNpncr4A+gmPzl/mgkwoZm
gAg0rcg+F47VwEtOfMQiX5Xu+pFuMTVgGQBZNx17a3UdMObXR3Efs31vEXCdlaUQ
kABxIjI1bYv9kOafxXKTnbrMdQnBj2PmsnwvXtqMzRwUNWuyOFNHdu6Mh3uz8sWH
tmFOqIDZ8dXcFu0l1HkYxb3mZqQo6Jc6qQcNkYX89Rhl74hJ47hWb64UzOuvfr+S
6YJV8uTbJaYLC9xbiGwoBOQwsWxll8xVcaq81gcsDrm+/pvuQq5tzJSK8cSnIAZd
tmHsG9DV2si3hBYK7aMBInljySpqMjcSgSMCjjpHAs0nsQ/JODHDahzDyNuKRWVv
8NEkBMeyNMCWaiMQTDcriM8CAwEAAQ==
"""
}

