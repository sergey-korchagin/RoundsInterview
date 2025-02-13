package com.skorch.roundinterview.domain.usecase

import com.skorch.roundinterview.domain.ImageData
import com.skorch.roundinterview.domain.ImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetImagesUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend fun execute(): Flow<List<ImageData>> = repository.getImages()
}
