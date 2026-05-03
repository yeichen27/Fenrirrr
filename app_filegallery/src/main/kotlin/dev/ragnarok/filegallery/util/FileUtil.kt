package dev.ragnarok.filegallery.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dev.ragnarok.filegallery.Constants
import java.io.File

object FileUtil {
    fun getExportedUriForFile(context: Context, file: File): Uri? {
        try {
            return FileProvider.getUriForFile(context, Constants.FILE_PROVIDER_AUTHORITY, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}