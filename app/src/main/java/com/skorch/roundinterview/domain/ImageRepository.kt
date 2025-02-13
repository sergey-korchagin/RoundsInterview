package com.skorch.roundinterview.domain

import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    suspend fun getImages(): Flow<List<ImageData>>
}