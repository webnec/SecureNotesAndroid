package pl.webnec.securenotes.utilities

import android.content.Context
import androidx.biometric.BiometricManager
import pl.webnec.securenotes.R

object BiometricManagerUtils {

    fun getAvailabilityCode(applicationContext: Context): Int {
        return BiometricManager.from(applicationContext).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )
    }

    fun isBiometricAvailable(availabilityCode: Int): Boolean {
        return availabilityCode == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun getUnavailabilityInformation(biometricUnavailableCode: Int, applicationContext: Context): String {
        return when(biometricUnavailableCode){
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> applicationContext.getString(
                R.string.biometric_error_hardware_unavailable
            )
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> applicationContext.getString(
                R.string.biometric_error_none_enrolled
            )
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> applicationContext.getString(
                R.string.biometric_error_no_hardware
            )
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> applicationContext.getString(
                R.string.biometric_error_security_update_required
            )
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> applicationContext.getString(
                R.string.biometric_error_unsupported
            )
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> applicationContext.getString(
                R.string.biometric_error_unknown
            )
            else -> applicationContext.getString(
                R.string.biometric_error_unknown
            )
        }
    }

}