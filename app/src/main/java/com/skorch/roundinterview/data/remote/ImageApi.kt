package com.skorch.roundinterview.data.remote

import retrofit2.http.GET

interface ImageApi {
    @GET("image_list.json")
    suspend fun fetchImages(): List<ImageRemoteData>
}