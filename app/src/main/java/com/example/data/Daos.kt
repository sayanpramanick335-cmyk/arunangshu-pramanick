package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getPrimaryUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface GeneratedImageDao {
    @Query("SELECT * FROM generated_images ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<GeneratedImageEntity>>

    @Query("SELECT * FROM generated_images WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<GeneratedImageEntity>>

    @Query("SELECT * FROM generated_images WHERE isTrending = 1 ORDER BY createdAt DESC")
    fun getTrending(): Flow<List<GeneratedImageEntity>>

    @Query("SELECT * FROM generated_images WHERE isDailySuggestion = 1 ORDER BY createdAt DESC")
    fun getDailySuggestions(): Flow<List<GeneratedImageEntity>>

    @Query("SELECT * FROM generated_images WHERE id = :id")
    suspend fun getImageById(id: String): GeneratedImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: GeneratedImageEntity)

    @Update
    suspend fun updateImage(image: GeneratedImageEntity)

    @Delete
    suspend fun deleteImage(image: GeneratedImageEntity)

    @Query("DELETE FROM generated_images WHERE isTrending = 0 AND isDailySuggestion = 0")
    suspend fun clearUserHistory()
}
