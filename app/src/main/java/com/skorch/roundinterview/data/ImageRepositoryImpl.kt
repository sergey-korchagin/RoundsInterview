package com.skorch.roundinterview.data

import com.skorch.roundinterview.data.local.LocalDataSource
import com.skorch.roundinterview.data.local.db.ImageEntity
import com.skorch.roundinterview.data.remote.RemoteDataSource
import com.skorch.roundinterview.domain.ImageData
import com.skorch.roundinterview.domain.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : ImageRepository {

    override suspend fun getImages(): Flow<List<ImageData>> = flow {
        // logic - if data exists in database emit else get from remote, can be also logic to update data base but for example I choose this logic
        val localData = localDataSource.getImages().first()
        if (localData.isEmpty()) {
            val remoteData = remoteDataSource.fetchImages()
            val entityList = remoteData.map { ImageEntity(it.id, it.imageUrl) }
            localDataSource.saveImages(entityList)
            emit(remoteData.map { ImageData(it.id, it.imageUrl) })
        } else {
            emit(localData.map { ImageData(it.id, it.imageUrl) })
        }
    }.flowOn(Dispatchers.IO)
}