package com.skorch.imageloader.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val MAX_SIZE = 50
private const val MAX_DURATION_HOURS = 4L

internal class ImageCache(context: Context) {
    private val diskCacheDir: File = File(context.cacheDir, "image_cache")
    private val memoryCache = object : LruCache<String, Bitmap>(MAX_SIZE) {} // ram cache while app is opened for not read from file , optimization
    private val cacheExpiration = mutableMapOf<String, Long>()
    private val cacheDuration = TimeUnit.HOURS.toMillis(MAX_DURATION_HOURS)
    private val sharedPreferences =
        context.getSharedPreferences("image_cache_prefs", Context.MODE_PRIVATE)

    init {
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs()
        }
        loadCacheExpiration()
    }

    fun put(url: String, bitmap: Bitmap) {
        memoryCache.put(url, bitmap)
        cacheExpiration[url] = System.currentTimeMillis()
        saveCacheExpiration()
        CoroutineScope(Dispatchers.IO).launch {
            saveBitmapToDisk(url, bitmap)
        }
    }

    suspend fun get(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            val expirationTime = cacheExpiration[url] ?: return@withContext null

            if (currentTime - expirationTime >= cacheDuration) {
                Log.d("ImageCache", "url :$url cache expired")
                remove(url)
                return@withContext null
            }

            memoryCache.get(url)?.let { return@withContext it }
            getBitmapFromDisk(url)?.let { return@withContext it }

            return@withContext null
        }
    }

    fun clear() {
        memoryCache.evictAll()
        cacheExpiration.clear()
        saveCacheExpiration()
        diskCacheDir.listFiles()?.let { files ->
            if (files.isNotEmpty()) {
                files.forEach { it.delete() }
            }
        }
    }

    private fun saveBitmapToDisk(url: String, bitmap: Bitmap) {
        val file = File(diskCacheDir, url.hashCode().toString())
        try {
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        } catch (e: IOException) {
            Log.e("ImageCache", "Failed to save bitmap to disk: ${e.message}")
        }
    }

    private fun getBitmapFromDisk(url: String): Bitmap? {
        val file = File(diskCacheDir, url.hashCode().toString())
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    @Synchronized
    private fun remove(url: String) {
        memoryCache.remove(url)
        cacheExpiration.remove(url)
        saveCacheExpiration()
        val file = File(diskCacheDir, url.hashCode().toString())
        if (file.exists()) {
            file.delete()
        }
    }

    @Synchronized
    private fun saveCacheExpiration() {
        try {
            val editor = sharedPreferences.edit()
            cacheExpiration.forEach { (key, value) ->
                editor.putLong(key, value)
            }
            editor.apply()
        } catch (e: Exception) {
            Log.e("ImageCache", "Failed to save cache expiration: ${e.message}")
        }
    }

    private fun loadCacheExpiration() {
        try {
            cacheExpiration.clear()
            sharedPreferences.all.forEach { (key, value) ->
                if (value is Long) {
                    cacheExpiration[key] = value
                }
            }
        } catch (e: Exception) {
            Log.e("ImageCache", "Failed to load cache expiration: ${e.message}")
        }
    }
}