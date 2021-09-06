package pl.webnec.securenotes.utilities

import android.content.ContentValues.TAG
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import pl.webnec.securenotes.R

object BiometricPromptUtils {

    fun getPrompt(
        activity: FragmentActivity,
        callbackFunction: (isSuccess: Boolean) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                callbackFunction(false)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                callbackFunction(false)

            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                callbackFunction(true)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    fun getPromptInfo(activity: FragmentActivity): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(activity.getString(R.string.prompt_info_title))
            setSubtitle(activity.getString(R.string.prompt_info_subtitle))
            setDescription(activity.getString(R.string.prompt_info_description))
            setConfirmationRequired(false)
            setNegativeButtonText(activity.getString(R.string.prompt_info_cancel))
        }.build()
}