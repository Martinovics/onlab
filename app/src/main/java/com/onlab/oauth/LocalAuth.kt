package com.onlab.oauth

import android.app.KeyguardManager
import android.nfc.Tag
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentActivity


// https://developer.android.com/training/sign-in/biometric-auth
// https://developer.android.com/jetpack/androidx/releases/biometric#declaring_dependencies




@RequiresApi(Build.VERSION_CODES.P)
class BiometricCallback(private val localAuth: LocalAuth) : BiometricPrompt.AuthenticationCallback() {

    private val ctx = localAuth.getContext()


    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        Toast.makeText(this.ctx, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        Toast.makeText(this.ctx, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        Toast.makeText(this.ctx, "Authentication failed", Toast.LENGTH_SHORT).show()
    }
}




@RequiresApi(Build.VERSION_CODES.P)
class LocalAuth(private val ctx: FragmentActivity) {

    companion object {
        const val REMEMBER_AUTH = 5 * 60
    }

    private val TAG = "LocalAuth"

    private val executor = ContextCompat.getMainExecutor(this.ctx)
    private val biometricPrompt = BiometricPrompt(this.ctx, executor, BiometricCallback(this))
    private val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric login for my app")
        .setSubtitle("Log in using your biometric credential")
        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        .build()
    private val deviceCredentialPromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock with PIN")
        .setAllowedAuthenticators(DEVICE_CREDENTIAL)
        .build()


    fun getContext(): FragmentActivity {
        return this.ctx
    }


    private fun callBiometricPrompt() {  // fingerprint, iris, or face...
        this.biometricPrompt.authenticate(this.biometricPromptInfo)
    }

    private fun callCredentialPrompt() {  // PIN, pattern, or password...
        this.biometricPrompt.authenticate(this.deviceCredentialPromptInfo)
    }

    fun callCancelPrompt() {  // PIN, pattern, or password...
        this.biometricPrompt.cancelAuthentication()
    }


    fun showAuthWindow() {
        val biometricManager = BiometricManager.from(this.ctx)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "App can authenticate using biometrics.")
                this.callBiometricPrompt()
            }
            else -> {
                Log.d(TAG, "Cant authenticate using biometrics. Checking other credentials.")

                val keyguardManager = getSystemService(this.ctx, KeyguardManager::class.java)
                if (keyguardManager?.isKeyguardSecure == true) {
                    Log.d(TAG, "App can authenticate using credentials.")
                    this.callCredentialPrompt()
                } else {
                    Log.d(TAG, "User has not set up any security")
                    this.callCancelPrompt()
                }
            }
        }
    }

}