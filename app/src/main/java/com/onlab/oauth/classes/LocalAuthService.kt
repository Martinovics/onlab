package com.onlab.oauth.classes

import android.app.KeyguardManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.onlab.oauth.interfaces.ICallback
import com.onlab.oauth.viewModels.activities.MainActivity


@RequiresApi(Build.VERSION_CODES.P)
class LocalAuthService(private val context: MainActivity) {

    private val tag = "LocalAuthService"
    private val keyguardManager = getSystemService(context, KeyguardManager::class.java)
    private var authenticationTimestamp = 0

    private val executor = ContextCompat.getMainExecutor(context)
    private val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock with fingerprint")
        .setSubtitle("Use your fingerprint to download the file")
        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        .build()
    private val deviceCredentialPromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock with PIN")
        .setSubtitle("Use your PIN to download the file")
        .setAllowedAuthenticators(DEVICE_CREDENTIAL)
        .build()


    private fun getTimestampSeconds(): Int {
        return (System.currentTimeMillis() / 1000).toInt()
    }

    fun isLocalAuthValid(timeout: Int): Boolean {
        return this.getTimestampSeconds() - this.authenticationTimestamp < timeout
    }

    fun updateAuthenticationTimestamp() {
        this.authenticationTimestamp = this.getTimestampSeconds()
    }


    private fun callBiometricPromptWithCallback(callback: ICallback) {  // fingerprint, iris, or face...
        val biometricPrompt = BiometricPrompt(context, executor, LocalAuthCallback(callback))
        biometricPrompt.authenticate(this.biometricPromptInfo)
    }

    private fun callCredentialPromptWithCallback(callback: ICallback) {  // PIN, pattern, or password...
        val biometricPrompt = BiometricPrompt(context, executor, LocalAuthCallback(callback))
        biometricPrompt.authenticate(this.deviceCredentialPromptInfo)
    }

    private fun callCancelPromptWithCallback(callback: ICallback) {  // PIN, pattern, or password...
        val biometricPrompt = BiometricPrompt(context, executor, LocalAuthCallback(callback))
        biometricPrompt.cancelAuthentication()
    }


    fun showAuthWindow(callback: ICallback) {
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(tag, "App can authenticate using biometrics.")
                callBiometricPromptWithCallback(callback)
            }
            else -> {
                Log.d(tag, "Can't authenticate using biometrics. Checking other credentials.")
                if (keyguardManager?.isKeyguardSecure == true) {
                    Log.d(tag, "App can authenticate using credentials.")
                    callCredentialPromptWithCallback(callback)
                } else {
                    Log.d(tag, "User has not set up any security")
                    callCancelPromptWithCallback(callback)
                }
            }
        }
    }




    inner class LocalAuthCallback(private val callback: ICallback): BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            Log.d(tag, "Authentication succeeded")

            super.onAuthenticationSucceeded(result)
            updateAuthenticationTimestamp()
            callback.onSuccess()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            Log.d(tag, "Authentication error: $errString")

            super.onAuthenticationError(errorCode, errString)
            callback.onFailure()
        }

        override fun onAuthenticationFailed() {
            Log.d(tag, "Authentication failed")

            super.onAuthenticationFailed()
            callback.onFailure()
        }
    }
}
