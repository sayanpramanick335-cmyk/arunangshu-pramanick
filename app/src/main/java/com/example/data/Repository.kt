package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class MoodPinRepository(private val db: AppDatabase) {

    val userDao = db.userDao()
    val imageDao = db.generatedImageDao()

    val primaryUser: Flow<UserEntity?> = userDao.getPrimaryUser()
    val allHistory: Flow<List<GeneratedImageEntity>> = imageDao.getAllHistory()
    val favorites: Flow<List<GeneratedImageEntity>> = imageDao.getFavorites()
    val trending: Flow<List<GeneratedImageEntity>> = imageDao.getTrending()
    val dailySuggestions: Flow<List<GeneratedImageEntity>> = imageDao.getDailySuggestions()

    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun insertImage(image: GeneratedImageEntity) = imageDao.insertImage(image)
    suspend fun updateImage(image: GeneratedImageEntity) = imageDao.updateImage(image)
    suspend fun deleteImage(image: GeneratedImageEntity) = imageDao.deleteImage(image)
    suspend fun clearHistory() = imageDao.clearUserHistory()

    suspend fun seedIfNeeded() {
        // Seed default user if not exists
        val user = primaryUser.first()
        if (user == null) {
            userDao.insertUser(
                UserEntity(
                    id = "default_user",
                    username = "CreativeSoul",
                    email = "creator@moodpic.ai",
                    phone = "+1 555-0199",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                    bio = "Aesthetic curator | Seeking cozy shadows & warm film grain ✨",
                    isLoggedIn = true,
                    isPremium = false
                )
            )
        }

        // Seed default trending items and daily recommendations
        val existingHistory = allHistory.first()
        val existingTrending = trending.first()
        if (existingTrending.isEmpty()) {
            val seedItems = listOf(
                // Trending Prompt Ideas
                GeneratedImageEntity(
                    id = "trend_1",
                    userIdea = "seen but no reply",
                    improvedPrompt = "Minimalist dark room at midnight, the soft blue glow of a phone screen illuminating a cozy pillow, grainy Polaroid photo style, low contrast, deep emotional shadows, vintage 35mm lens.",
                    imageUrl = "img_feed_study", // fallbacks to local study image or keyword
                    style = "Dark aesthetic",
                    size = "9:16",
                    textOverlay = "seen but no reply.",
                    isFavorite = false,
                    isTrending = true,
                    isDailySuggestion = false,
                    creatorName = "AestheticMuse"
                ),
                GeneratedImageEntity(
                    id = "trend_2",
                    userIdea = "heavy barbell under spotlight",
                    improvedPrompt = "Atmospheric luxury private gym, single heavy barbell laying on rough floor, single dynamic spotlight from above, floating chalk particles, cinematic high-contrast shadows.",
                    imageUrl = "img_feed_gym",
                    style = "Gym motivation",
                    size = "1:1",
                    textOverlay = "discipline over desire",
                    isFavorite = false,
                    isTrending = true,
                    isDailySuggestion = false,
                    creatorName = "GritAndGrowth"
                ),
                GeneratedImageEntity(
                    id = "trend_3",
                    userIdea = "neutral blazer capsule look",
                    improvedPrompt = "Aesthetic flatlay of modern minimalist capsule wardrobe, warm sand linen shirt, beige wool coat, gold vintage watch, soft lighting, cream background.",
                    imageUrl = "img_feed_fashion",
                    style = "Pinterest-style",
                    size = "9:16",
                    textOverlay = "neutral codes.",
                    isFavorite = false,
                    isTrending = true,
                    isDailySuggestion = false,
                    creatorName = "VogueVibe"
                ),
                GeneratedImageEntity(
                    id = "trend_4",
                    userIdea = "pastel sunset over coast",
                    improvedPrompt = "Dreamy aesthetic travel photo of Amalfi cliffside during lavender dusk, pastel pink clouds kissing the ocean wave crests, cinematic retro film look.",
                    imageUrl = "img_feed_travel",
                    style = "Cinematic",
                    size = "16:9",
                    textOverlay = "quiet shores.",
                    isFavorite = false,
                    isTrending = true,
                    isDailySuggestion = false,
                    creatorName = "Wanderlust"
                ),
                // Daily Suggestions
                GeneratedImageEntity(
                    id = "daily_1",
                    userIdea = "midnight hot tea study",
                    improvedPrompt = "Dark academia cozy desktop, antique oak table, glowing warm table lamp, open handwritten diary, aromatic steam rising from a clay mug, soft rain outside the window pane.",
                    imageUrl = "https://images.unsplash.com/photo-1515003197210-e0cd71810b5f?w=600",
                    style = "Realistic",
                    size = "9:16",
                    textOverlay = "midnight studies",
                    isFavorite = false,
                    isTrending = false,
                    isDailySuggestion = true,
                    creatorName = "CozyWriter"
                ),
                GeneratedImageEntity(
                    id = "daily_2",
                    userIdea = "aesthetic pastel room setup",
                    improvedPrompt = "Bright modern loft interior, cream boucle chairs, oversized monstera leaf shadows cast on a sand colored plaster wall, warm late afternoon sunlight beams, minimal design.",
                    imageUrl = "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=600",
                    style = "Pinterest-style",
                    size = "1:1",
                    textOverlay = "golden hour.",
                    isFavorite = false,
                    isTrending = false,
                    isDailySuggestion = true,
                    creatorName = "DesignAura"
                ),
                GeneratedImageEntity(
                    id = "daily_3",
                    userIdea = "cyberpunk rainy alleys",
                    improvedPrompt = "Stunning cinematic anime background of a neon-drenched futuristic Tokyo alleyway under soft purple drizzle, glowing storefront signs, gorgeous digital paint style.",
                    imageUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600",
                    style = "Anime",
                    size = "16:9",
                    textOverlay = "future memories",
                    isFavorite = false,
                    isTrending = false,
                    isDailySuggestion = true,
                    creatorName = "NeonPixel"
                )
            )
            seedItems.forEach { imageDao.insertImage(it) }
        }
    }
}
