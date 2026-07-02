package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Double? = 0.7,
    @Json(name = "responseMimeType") val responseMimeType: String? = "application/json"
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

// Strong response container for our UI
data class AiPromptResult(
    val imagePrompt: String,
    val caption: String,
    val textOverlay: String
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun generateAestheticPrompt(emotion: String): AiPromptResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // High-fidelity fallback mock generator if API key is unconfigured
            return getFallbackPromptResult(emotion)
        }

        val promptText = """
            The user typed this feeling or emotion: "$emotion".
            Generate a Pinterest-inspired aesthetic vision and asset idea.
            Return a JSON object with exactly three string fields:
            1. "imagePrompt": A detailed stable diffusion / generative image prompt describing an aesthetic scene reflecting this mood. Keep it detailed, specifying style (e.g. film grain, 35mm photography, warm vintage glow), composition, subject, and color palette.
            2. "caption": A catchy, aesthetic, or poetic caption for the post. Include subtle emojis.
            3. "textOverlay": A short, deep, or relatable text overlay phrase (max 4-5 words) that can be written directly on the image.

            Example Output Format:
            {
              "imagePrompt": "Warm retro bedroom at night, lonely glow of smartphone, neon blue screen reflection on a fluffy pillow, vaporwave aesthetic, 35mm film grain, moody photography",
              "caption": "Left in the digital ether. 💬✨",
              "textOverlay": "seen but no reply"
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = promptText)))
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.8, responseMimeType = "application/json")
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseJsonResult(jsonText, emotion)
        } catch (e: Exception) {
            getFallbackPromptResult(emotion)
        }
    }

    private fun parseJsonResult(jsonText: String, emotion: String): AiPromptResult {
        return try {
            // Simple manual parser to avoid extra library dependencies and handle any malformed outputs
            val imagePrompt = extractField(jsonText, "imagePrompt")
            val caption = extractField(jsonText, "caption")
            val textOverlay = extractField(jsonText, "textOverlay")
            if (imagePrompt.isNotEmpty() && caption.isNotEmpty() && textOverlay.isNotEmpty()) {
                AiPromptResult(imagePrompt, caption, textOverlay)
            } else {
                getFallbackPromptResult(emotion)
            }
        } catch (e: Exception) {
            getFallbackPromptResult(emotion)
        }
    }

    private fun extractField(json: String, field: String): String {
        val pattern = "\"$field\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1) ?: ""
    }

    private fun getFallbackPromptResult(emotion: String): AiPromptResult {
        return when (emotion.lowercase().trim()) {
            "seen but no reply", "heartbreak", "sad" -> AiPromptResult(
                imagePrompt = "A cozy, dimly-lit bedroom at 2 AM, the lonely blue glow of a smartphone screen reflecting off a pillow, moody retro color grading, soft focus, film grain, vintage photography, 35mm lens.",
                caption = "Echoes in the digital silence. Some replies are never sent. 💬🍂",
                textOverlay = "seen but no reply."
            )
            "gym", "fitness", "motivation" -> AiPromptResult(
                imagePrompt = "An atmospheric modern luxury gym under a single dramatic spotlight, close-up on heavy steel plates and chalk dust on hands, cinematic high contrast, deep shadows, raw strength aesthetic.",
                caption = "The body achieves what the mind believes. Build the steel habit. ⚡️🏋️",
                textOverlay = "discipline over desire"
            )
            "study", "focus", "business" -> AiPromptResult(
                imagePrompt = "A dark academia desk layout, vintage brass lamp lighting up an open notebook, steaming cup of coffee next to a black laptop, soft shadows, warm cinematic aesthetic, cozy workspace.",
                caption = "Building dreams in silence, one page at a time. ☕️📚",
                textOverlay = "silent growth"
            )
            else -> AiPromptResult(
                imagePrompt = "A dreamy minimalist scene of a pastel sunset over quiet rolling waves, gentle pink and gold clouds reflecting on wet sand, nostalgic cinematic lighting, 35mm film aesthetic.",
                caption = "Finding beauty in the quiet states of mind. ✨🌊",
                textOverlay = "cozy states: $emotion"
            )
        }
    }
}
