package com.example.kutira_kushala.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

object ImageCompressor {
    private const val MAX_EDGE = 1280
    private const val JPEG_QUALITY = 78

    /** Best-effort: some picker URIs allow persisted read access across activity restarts. */
    fun takePersistableReadUri(context: Context, uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: SecurityException) {
        } catch (_: UnsupportedOperationException) {
        }
    }

    suspend fun compressJpeg(context: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        takePersistableReadUri(context, uri)
        decodeViaImageDecoder(context, uri)?.let { bitmap ->
            try {
                val scaled = scaleDown(bitmap, MAX_EDGE)
                if (scaled != bitmap) bitmap.recycle()
                return@withContext jpegBytes(scaled).also { scaled.recycle() }
            } catch (_: OutOfMemoryError) {
                bitmap.recycle()
                System.gc()
            }
        }

        compressViaBitmapFactory(context, uri)
    }

    private fun decodeViaImageDecoder(context: Context, uri: Uri): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val w = info.size.width
                val h = info.size.height
                if (w <= 0 || h <= 0) return@decodeBitmap
                val longest = max(w, h)
                if (longest > MAX_EDGE) {
                    val scale = MAX_EDGE.toFloat() / longest
                    decoder.setTargetSize(
                        max(1, (w * scale).toInt()),
                        max(1, (h * scale).toInt()),
                    )
                }
            }
        } catch (_: OutOfMemoryError) {
            System.gc()
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun compressViaBitmapFactory(context: Context, uri: Uri): ByteArray {
        return try {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return ByteArray(0)

            val sample = inSampleSizeForMaxEdge(bounds.outWidth, bounds.outHeight, MAX_EDGE)
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val decoded = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return ByteArray(0)

            val scaled = scaleDown(decoded, MAX_EDGE)
            if (scaled != decoded) decoded.recycle()
            jpegBytes(scaled).also { scaled.recycle() }
        } catch (_: OutOfMemoryError) {
            System.gc()
            ByteArray(0)
        } catch (_: Exception) {
            ByteArray(0)
        }
    }

    private fun jpegBytes(bitmap: Bitmap): ByteArray =
        ByteArrayOutputStream().use { bos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bos)
            bos.toByteArray()
        }

    private fun inSampleSizeForMaxEdge(width: Int, height: Int, maxEdge: Int): Int {
        val longest = max(width, height)
        var sample = 1
        while (longest / sample > maxEdge) {
            sample *= 2
        }
        return max(1, sample)
    }

    private fun scaleDown(bitmap: Bitmap, maxEdge: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val longest = max(w, h)
        if (longest <= maxEdge) return bitmap
        val scale = maxEdge.toFloat() / longest
        val nw = max(1, (w * scale).toInt())
        val nh = max(1, (h * scale).toInt())
        return Bitmap.createScaledBitmap(bitmap, nw, nh, true)
    }
}
