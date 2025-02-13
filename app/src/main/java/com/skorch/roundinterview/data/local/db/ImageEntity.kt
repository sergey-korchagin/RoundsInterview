package com.skorch.roundinterview.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey val id: Int,
    val imageUrl: String
)