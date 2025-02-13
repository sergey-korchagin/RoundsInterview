package com.skorch.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.skorch.imageloader.data.local.ImageCache
import com.skorch.imageloader.data.remote.ImageDownloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// for use with java -   ImageLoader.Companion.getInstance().load("url", view, R.drawable.placeholder);
// for see logs please use "Image" as tag
class ImageLoader private constructor(context: Context) {
    private val imageCache = ImageCache(context)
    private val imageDownloader = ImageDownloader()

    companion object {
        @Volatile
        private var INSTANCE: ImageLoader? = null

        fun with(context: Context): ImageLoader {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageLoader(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun clearCache() {
            INSTANCE?.imageCache?.clear() ?: Log.w(
                "ImageLoader",
                "Not initialized, cache cannot be cleared."
            )
        }
    }

    fun load(url: String, imageView: ImageView, @DrawableRes placeholder: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val cachedBitmap = imageCache.get(url)
            if (cachedBitmap != null) {
                setImageBitmap(bitmap = cachedBitmap, imageView = imageView)
                Log.d("ImageLoader", "Image loaded from cache")
                return@launch
            }

            imageView.setImageResource(placeholder)

            val result = withContext(Dispatchers.IO) { imageDownloader.download(url) }
            result.fold(
                onSuccess = { bitmap ->
                    try {
                        imageCache.put(url, bitmap)
                        withContext(Dispatchers.Main) {
                            Log.d("ImageLoader", "Image loaded from remote")
                            try {
                                setImageBitmap(bitmap = bitmap, imageView = imageView)
                            } catch (e: OutOfMemoryError) {
                                Log.e("ImageLoader", "Bitmap too large to display: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ImageLoader", "Image view failed: ${e.message}")
                    }
                },
                onFailure = {
                    Log.e("ImageLoader", "Error loading image: ${it.message}")
                }
            )
        }
    }

    private fun setImageBitmap(bitmap: Bitmap, imageView: ImageView) {
        val resizedBitmap = resizeBitmapIfNeeded(bitmap, imageView.width, imageView.height)
        imageView.setImageBitmap(resizedBitmap)
    }

    // optimization for fast loading and for prevent out of memory
    private fun resizeBitmapIfNeeded(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        if (targetWidth <= 0 || targetHeight <= 0) return bitmap
        return if (bitmap.width > targetWidth || bitmap.height > targetHeight) {
            Bitmap.createScaledBitmap(bitmap, maxOf(targetWidth, 1), maxOf(targetHeight, 1), true)
        } else {
            bitmap
        }
    }

}
