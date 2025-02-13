package com.skorch.roundinterview.data.remote

import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val imageApi: ImageApi) {
    suspend fun fetchImages(): List<ImageRemoteData> = imageApi.fetchImages()
}