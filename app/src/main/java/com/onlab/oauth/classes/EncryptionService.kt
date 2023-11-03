package com.onlab.oauth.classes

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

@RequiresApi(Build.VERSION_CODES.R)
class EncryptionService(private var alias: String) {

    private companion object {
        private const val TAG = "Secrets"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }

    private val keyStore: KeyStore
        get() {
            return KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        }

    private val secretKey: SecretKey?
        get() {
            return (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey
        }

    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore")
        val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationParameters(180, KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL)
        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }

    private fun getEncryptionCipher(): Cipher {
        val key = secretKey ?: generateKey()
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
    }

    private fun getDecryptionCipher(initializationVector: ByteArray): Cipher? {
        val key = secretKey ?: return null
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, IvParameterSpec(initializationVector))
        }
    }

    /**
     * Encrypts input steam and closes it.
     * Returns encrypted bytes. When encryption fails, returns the original data as a byte array
     */
    suspend fun encrypt(inputStream: InputStream): Pair<Boolean, ByteArray> = withContext(Dispatchers.IO) {
        val originalContent = inputStream.readBytes()
        try {
            val cipher = getEncryptionCipher()
            val encryptedBytes = cipher.doFinal(originalContent)

            val outputStream = ByteArrayOutputStream()
            outputStream.use {
                it.write(cipher.iv.size)
                it.write(cipher.iv)
                it.write(encryptedBytes.size)
                it.write(encryptedBytes)
            }

            return@withContext Pair(true, outputStream.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Pair(false, originalContent)
        }
    }


    /**
     * Decrypts input steam and closes it.
     * Returns decrypted bytes. When decryption fails, returns the original data as a byte array
     */
    suspend fun decrypt(inputStream: InputStream): Pair<Boolean, ByteArray> = withContext(Dispatchers.IO) {
        val originalContent = inputStream.readBytes()
        try {
            val byteArrayInputStream = ByteArrayInputStream(originalContent)
            byteArrayInputStream.use {
                val ivSize = it.read()
                val iv = ByteArray(ivSize)
                it.read(iv)

                val encryptedBytesSize = it.read()
                val encryptedBytes = ByteArray(encryptedBytesSize)
                it.read(encryptedBytes)

                val cipher = getDecryptionCipher(iv) ?: throw Exception("Decryption key not found for $alias")
                return@withContext Pair(true, cipher.doFinal(encryptedBytes))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Pair(false, originalContent)
        }
    }

    fun hasSecretKey(): Boolean {
        return secretKey != null
    }

    fun deleteAlias(): Boolean {
        try {
            keyStore.deleteEntry(alias)
            return true
        } catch (e: KeyStoreException) {
            Log.e(TAG, "Error deleting key: ${e.message}")
            return false
        }
    }

}