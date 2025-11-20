package id.stargan.intikasir.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Helper to share a PDF file stored in the app cache (or files) using FileProvider.
 * Usage: sharePdfFromCache(context, "Receipt-TX-20251120-0015-58mm.pdf")
 */
object ShareUtils {
    /**
     * Share a PDF file located in the app cache directory using FileProvider (content:// URI).
     * Returns false if the file does not exist.
     */
    fun sharePdfFromCache(context: Context, fileName: String): Boolean {
        val file = File(context.cacheDir, fileName)
        if (!file.exists()) return false

        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, "Receipt", uri)
        }

        val chooser = Intent.createChooser(shareIntent, "Bagikan Struk")

        // Grant URI permission to all resolved activities before launching chooser
        val resInfos = context.packageManager.queryIntentActivities(chooser, 0)
        resInfos.forEach { resolveInfo ->
            context.grantUriPermission(
                resolveInfo.activityInfo.packageName,
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        context.startActivity(chooser)
        return true
    }

    /**
     * Share any file using FileProvider (cache/files). Caller must supply the File and mimeType.
     */
    fun shareFile(context: Context, file: File, mimeType: String = "application/octet-stream"): Boolean {
        if (!file.exists()) return false
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, "file", uri)
        }

        val chooser = Intent.createChooser(shareIntent, "Bagikan file")
        val resInfos = context.packageManager.queryIntentActivities(chooser, 0)
        resInfos.forEach { resolveInfo ->
            context.grantUriPermission(
                resolveInfo.activityInfo.packageName,
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        context.startActivity(chooser)
        return true
    }

    /**
     * Share an existing content:// or file:// Uri. If it's a file:// Uri, it will be converted to content:// via FileProvider.
     * Grants read permission to the receiving activities.
     */
    fun shareUri(context: Context, uri: Uri, mimeType: String = "application/pdf", title: String = "Bagikan") : Boolean {
        var shareUri = uri
        try {
            if (uri.scheme == "file") {
                val file = File(uri.path ?: return false)
                if (!file.exists()) return false
                shareUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newUri(context.contentResolver, "file", shareUri)
            }

            val chooser = Intent.createChooser(intent, title)
            val resInfos = context.packageManager.queryIntentActivities(chooser, 0)
            resInfos.forEach { resolveInfo ->
                context.grantUriPermission(resolveInfo.activityInfo.packageName, shareUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(chooser)
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
