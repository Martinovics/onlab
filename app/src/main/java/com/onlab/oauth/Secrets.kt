package com.onlab.oauth

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.*
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec






@RequiresApi(Build.VERSION_CODES.R)
class Secrets {

    private companion object {
        private const val TAG = "Secrets"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

        private const val REMEMBER_AUTH_SECONDS = 5 * 60
    }

    private val key_store = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }


    private fun generate_key(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore")
        val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationParameters(REMEMBER_AUTH_SECONDS, KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL)
            //.setUserPresenceRequired(false)
        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }


    private fun get_key(alias: String): SecretKey {
        val key = this.key_store.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        return key?.secretKey ?: generate_key(alias)
    }


    private fun get_encryption_cipher(alias: String): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, get_key(alias))
        }
    }


    private fun get_decryption_cipher(alias: String, initialization_vector: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, get_key(alias), IvParameterSpec(initialization_vector))
        }
    }


    fun write_file(alias: String, content: ByteArray, outputStream: OutputStream): ByteArray {
        val cipher = get_encryption_cipher(alias)
        val encryptedBytes = cipher.doFinal(content)
        outputStream.use {
            it.write(cipher.iv.size)
            it.write(cipher.iv)
            it.write(encryptedBytes.size)
            it.write(encryptedBytes)
        }
        return encryptedBytes
    }


    fun read_file(alias: String, inputStream: InputStream): ByteArray {
        return inputStream.use {
            val ivSize = it.read()
            val iv = ByteArray(ivSize)
            it.read(iv)

            val encryptedBytesSize = it.read()
            val encryptedBytes = ByteArray(encryptedBytesSize)
            it.read(encryptedBytes)

            val cipher = get_decryption_cipher(alias, iv)
            cipher.doFinal(encryptedBytes)
        }
    }


    fun delete_key(alias: String) {
        try {
            this.key_store.deleteEntry(alias)
        } catch (e: KeyStoreException) {
            Log.e(TAG, "Error deleting key: ${e.message}")
        }
    }


}