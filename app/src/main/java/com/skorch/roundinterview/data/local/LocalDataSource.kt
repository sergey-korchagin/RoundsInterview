package com.skorch.roundinterview.data.local

import com.skorch.roundinterview.data.local.db.ImageDao
import com.skorch.roundinterview.data.local.db.ImageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(private val dao: ImageDao) {
    fun getImages(): Flow<List<ImageEntity>> = dao.getAllImages()
    suspend fun saveImages(images: List<ImageEntity>) = dao.insertImages(images)
}