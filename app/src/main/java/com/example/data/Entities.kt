package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val phone: String,
    val avatarUrl: String,
    val bio: String,
    val isLoggedIn: Boolean = false,
    val isPremium: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "generated_images")
data class GeneratedImageEntity(
    @PrimaryKey val id: String,
    val userIdea: String,
    val improvedPrompt: String,
    val imageUrl: String,   // A local asset reference (e.g. "img_feed_fashion") or online keyword URL
    val style: String,      // Realistic, Cinematic, Anime, Dark aesthetic, Pinterest-style, Motivational poster, Love/heartbreak mood, Gym motivation, Luxury lifestyle
    val size: String,       // "9:16", "1:1", "16:9"
    val textOverlay: String,
    val isFavorite: Boolean = false,
    val isTrending: Boolean = false,
    val isDailySuggestion: Boolean = false,
    val creatorName: String = "You",
    val createdAt: Long = System.currentTimeMillis()
)
