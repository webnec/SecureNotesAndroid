package pl.webnec.securenotes.utilities

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.File
import java.lang.Exception

object FileCryptographicUtils {

    @Throws(Exception::class)
    fun saveToFile(applicationContext: Context, fileName: String, fileBody: String) {
        if (fileBody.isBlank()) return
        removeFile(applicationContext, fileName)
        val encryptedFile = getEncryptedFile(applicationContext, fileName)
        encryptedFile.openFileOutput().use { output ->
            output.write(fileBody.toByteArray())
        }
    }

    @Throws(Exception::class)
    fun readFromFile(applicationContext: Context, fileName: String): String {
        val encryptedFile = getEncryptedFile(applicationContext, fileName)
        encryptedFile.openFileInput().use { input ->
            return String(input.readBytes(), Charsets.UTF_8)
        }
    }

    private fun getEncryptedFile(applicationContext: Context, fileName: String): EncryptedFile {
        return EncryptedFile.Builder(
            File(applicationContext.filesDir, fileName),
            applicationContext,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    private fun removeFile(applicationContext: Context, fileName: String){
        val file: File = File(applicationContext.filesDir, fileName)
        if(file.exists()){
            file.delete()
        }
    }
}