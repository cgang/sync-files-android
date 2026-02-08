package com.github.cgang.syncfiles.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.github.cgang.syncfiles.constants.Config
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CredentialManager(
    private val context: Context
) {
    private val keyStore = KeyStore.getInstance(Config.KEYSTORE_PROVIDER).apply { load(null) }
    private val prefs = context.getSharedPreferences(Config.PREFERENCES_NAME, Context.MODE_PRIVATE)

    suspend fun saveCredentials(username: String, password: String) {
        val key = getOrCreateSecretKey()

        val cipher = Cipher.getInstance(Config.KEY_ENCRYPTION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val credentials = "$username:$password"
        val encrypted = cipher.doFinal(credentials.toByteArray())

        prefs.edit().apply {
            putString("encrypted_credentials", Base64.encodeToString(encrypted, Base64.DEFAULT))
            putString("iv", Base64.encodeToString(iv, Base64.DEFAULT))
            apply()
        }
    }

    suspend fun getCredentials(): Pair<String, String>? {
        val encrypted = Base64.decode(prefs.getString("encrypted_credentials", null), Base64.DEFAULT)
        val iv = Base64.decode(prefs.getString("iv", null), Base64.DEFAULT)

        return try {
            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(Config.KEY_ENCRYPTION)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            val decrypted = cipher.doFinal(encrypted)
            val text = String(decrypted)

            val parts = text.split(":")
            if (parts.size == 2) parts[0] to parts[1] else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearCredentials() {
        prefs.edit().apply {
            remove("encrypted_credentials")
            remove("iv")
            apply()
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        if (!keyStore.containsAlias(Config.KEY_ALIAS_CREDENTIALS)) {
            createSecretKey()
        }

        return (keyStore.getEntry(Config.KEY_ALIAS_CREDENTIALS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun createSecretKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            Config.KEYSTORE_PROVIDER
        )

        val spec = KeyGenParameterSpec.Builder(
            Config.KEY_ALIAS_CREDENTIALS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }
}
