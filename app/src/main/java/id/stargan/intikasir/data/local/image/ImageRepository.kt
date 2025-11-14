package id.stargan.intikasir.data.local.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import javax.inject.Inject

interface ImageRepository {
    suspend fun saveImage(source: Uri, compressionQuality: Int = 85): String // returns local path
    suspend fun deleteImage(path: String)
}

class ImageRepositoryImpl @Inject constructor(
    private val context: Context
) : ImageRepository {
    override suspend fun saveImage(source: Uri, compressionQuality: Int): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
        val file = File(dir, "img_${System.currentTimeMillis()}.jpg")

        val bitmap: Bitmap = when (source.scheme) {
            "file" -> {
                // Direct file path
                FileInputStream(source.path!!).use { fis -> BitmapFactory.decodeStream(fis) }
            }
            else -> {
                context.contentResolver.openInputStream(source)?.use { input ->
                    BitmapFactory.decodeStream(input)
                } ?: throw IllegalArgumentException("Cannot open image source: ${source}")
            }
        }

        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, output)
        }
        bitmap.recycle()
        return@withContext file.absolutePath
    }

    override suspend fun deleteImage(path: String) {
        withContext(Dispatchers.IO) {
            runCatching { File(path).delete() }
        }
    }
}
