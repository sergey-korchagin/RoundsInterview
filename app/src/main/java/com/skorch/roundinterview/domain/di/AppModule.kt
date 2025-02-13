package com.skorch.roundinterview.domain.di

import android.app.Application
import androidx.room.Room
import com.skorch.roundinterview.data.ImageRepositoryImpl
import com.skorch.roundinterview.data.local.LocalDataSource
import com.skorch.roundinterview.data.local.db.AppDatabase
import com.skorch.roundinterview.data.local.db.ImageDao
import com.skorch.roundinterview.data.remote.ImageApi
import com.skorch.roundinterview.data.remote.RemoteDataSource
import com.skorch.roundinterview.domain.ImageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, "image_db").build()

    @Provides
    fun provideDao(db: AppDatabase): ImageDao = db.imageDao()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://zipoapps-storage-test.nyc3.digitaloceanspaces.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    fun provideApi(retrofit: Retrofit): ImageApi = retrofit.create(ImageApi::class.java)

    @Provides
    @Singleton
    fun provideImageRepository(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource
    ): ImageRepository = ImageRepositoryImpl(remoteDataSource, localDataSource)
}