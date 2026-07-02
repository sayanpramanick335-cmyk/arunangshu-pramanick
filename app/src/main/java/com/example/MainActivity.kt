package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.R
import com.example.data.GeneratedImageEntity
import com.example.data.UserEntity
import com.example.ui.GenerationState
import com.example.ui.MoodPicViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(true) }
            
            val customColorScheme = if (isDarkMode) {
                darkColorScheme(
                    primary = Color(0xFFE07A5F), // soft copper
                    onPrimary = Color(0xFF0D0D0D),
                    secondary = Color(0xFFFAFAFA), // ice white
                    background = Color(0xFF0D0D0D), // obsidian bg
                    surface = Color(0xFF151515), // card dark
                    onBackground = Color(0xFFFAFAFA),
                    onSurface = Color(0xFFFAFAFA),
                    surfaceVariant = Color(0xFF1E1E1E),
                    onSurfaceVariant = Color(0xFFCCCCCC)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFE07A5F), // soft copper
                    onPrimary = Color.White,
                    secondary = Color(0xFF1F1F1F), // charcoal
                    background = Color(0xFFF9F6F0), // warm sand bg
                    surface = Color.White, // card light
                    onBackground = Color(0xFF1F1F1F),
                    onSurface = Color(0xFF1F1F1F),
                    surfaceVariant = Color(0xFFF3EDF7),
                    onSurfaceVariant = Color(0xFF49454F)
                )
            }

            MaterialTheme(
                colorScheme = customColorScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MoodPicMainScreen(
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { isDarkMode = !isDarkMode }
                    )
                }
            }
        }
    }
}

enum class MoodPicTab {
    GENERATE, TRENDING, HISTORY, CREATOR_STUDIO
}

@Composable
fun MoodPicMainScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    viewModel: MoodPicViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(MoodPicTab.GENERATE) }
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Copy-to-generate buffer
    var promptInputBuffer by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            MoodPicBottomNav(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Editorial Header
            MoodPicHeader(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                isPremium = currentUser?.isPremium ?: false,
                onPremiumToggle = { viewModel.togglePremium() }
            )

            // Current Screen Content based on tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (currentTab) {
                    MoodPicTab.GENERATE -> {
                        GenerateTabContent(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            inputBuffer = promptInputBuffer,
                            onInputConsumed = { promptInputBuffer = "" }
                        )
                    }
                    MoodPicTab.TRENDING -> {
                        TrendingTabContent(
                            viewModel = viewModel,
                            onUsePrompt = { prompt ->
                                promptInputBuffer = prompt
                                currentTab = MoodPicTab.GENERATE
                            }
                        )
                    }
                    MoodPicTab.HISTORY -> {
                        HistoryTabContent(
                            viewModel = viewModel
                        )
                    }
                    MoodPicTab.CREATOR_STUDIO -> {
                        CreatorStudioTabContent(
                            viewModel = viewModel,
                            currentUser = currentUser
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodPicHeader(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    isPremium: Boolean,
    onPremiumToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "MoodPic AI",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Aesthetic Text-to-Image Canvas",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = if (isDarkMode) Color(0xFF888888) else Color(0xFF666666)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Premium Badge Toggle (Satisfies simulated monetization requirement beautifully)
            Surface(
                onClick = onPremiumToggle,
                shape = RoundedCornerShape(20.dp),
                color = if (isPremium) Color(0xFFFFD700).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (isPremium) Color(0xFFFFD700) else Color.Transparent
                ),
                modifier = Modifier.testTag("premium_badge_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium Mode",
                        tint = if (isPremium) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isPremium) "CREATOR PRO" else "FREE PLAN",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isPremium) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Dark Mode Switcher
            IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.Brightness2,
                    contentDescription = "Toggle Theme Mode",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun GenerateTabContent(
    viewModel: MoodPicViewModel,
    currentUser: UserEntity?,
    inputBuffer: String,
    onInputConsumed: () -> Unit
) {
    var ideaText by remember { mutableStateOf("") }
    
    // Copy incoming buffer if set
    LaunchedEffect(inputBuffer) {
        if (inputBuffer.isNotBlank()) {
            ideaText = inputBuffer
            onInputConsumed()
        }
    }

    var selectedStyle by remember { mutableStateOf("Pinterest-style") }
    var selectedSize by remember { mutableStateOf("9:16") }
    var enableTextOverlay by remember { mutableStateOf(true) }
    var customOverlayText by remember { mutableStateOf("") }

    val generationState by viewModel.generationState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val styleOptions = listOf(
        "Pinterest-style", "Dark aesthetic", "Realistic", "Cinematic",
        "Anime", "Motivational poster", "Love/heartbreak mood", "Gym motivation", "Luxury lifestyle"
    )

    val sizeOptions = listOf(
        "9:16" to "Story (9:16)",
        "1:1" to "Square (1:1)",
        "16:9" to "Cinematic (16:9)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        when (val state = generationState) {
            is GenerationState.Idle -> {
                // 1. Text Idea Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, shape = RoundedCornerShape(24.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Write your idea or emotion",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enter raw thoughts, mood triggers, or deep quotes. AI will improve and formulate the perfect visual prompt.",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                color = Color(0xFF888888)
                            ),
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        OutlinedTextField(
                            value = ideaText,
                            onValueChange = { ideaText = it },
                            placeholder = {
                                Text(
                                    "e.g. 'seen but no reply', 'aesthetic cozy morning', 'grit gym motivation'...",
                                    style = TextStyle(fontSize = 13.sp, color = Color.Gray)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("idea_input_box"),
                            shape = RoundedCornerShape(16.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        // Rapid preset triggers (helps user instantly start experimenting)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("lonely boy at night", "success mindset", "dark love quote", "cozy study desk").forEach { suggestion ->
                                Surface(
                                    onClick = { ideaText = suggestion },
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Text(
                                        text = suggestion,
                                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Style Selector Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, shape = RoundedCornerShape(24.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Select Image Style",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Grid Layout for Style Selectors
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            styleOptions.chunked(2).forEach { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    chunk.forEach { style ->
                                        val isSelected = style == selectedStyle
                                        Surface(
                                            onClick = { selectedStyle = style },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("style_${style.lowercase().replace(" ", "_")}"),
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = style,
                                                    style = TextStyle(
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                    if (chunk.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Configuration Box (Size & Overlay Options)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, shape = RoundedCornerShape(24.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Canvas Settings & Overlays",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Size Choices
                        Text(
                            text = "Aspect Ratio",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sizeOptions.forEach { (key, title) ->
                                val isSelected = key == selectedSize
                                Surface(
                                    onClick = { selectedSize = key },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = title,
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )

                        // Text Overlay settings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Add Text Overlay",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Render mood text directly on the picture",
                                    style = TextStyle(fontSize = 10.sp, color = Color.Gray)
                                )
                            }
                            Switch(
                                checked = enableTextOverlay,
                                onCheckedChange = { enableTextOverlay = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                            )
                        }

                        if (enableTextOverlay) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = customOverlayText,
                                onValueChange = { customOverlayText = it },
                                placeholder = {
                                    Text(
                                        "Enter overlay text (Leave blank for AI auto-generation)",
                                        style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(fontSize = 13.sp),
                                maxLines = 1,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. BIG ACTION BUTTON
                Button(
                    onClick = {
                        viewModel.generateImage(
                            idea = ideaText,
                            style = selectedStyle,
                            size = selectedSize,
                            addTextOverlay = enableTextOverlay,
                            customOverlayText = customOverlayText
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_generate_image"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Magic"
                        )
                        Text(
                            text = "Generate Magic Picture ✨",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }

            is GenerationState.Loading -> {
                // HIGH FIDELITY ANIMATED GENERATING STAGE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val angle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        )

                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Loading spinner",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(64.dp)
                                .rotate(angle)
                        )

                        Text(
                            text = state.step,
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 40.dp)
                        )

                        Text(
                            text = "Please keep the app open. This process utilizes server-side AI processing.",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = Color.Gray
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 50.dp)
                        )
                    }
                }
            }

            is GenerationState.Success -> {
                // INTERACTIVE COMPLETED IMAGE DETAILS
                val image = state.image
                CompletedImageContainer(
                    image = image,
                    onBack = { viewModel.resetState() },
                    viewModel = viewModel
                )
            }

            is GenerationState.Error -> {
                // ERROR STAGE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = state.message,
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Button(
                            onClick = { viewModel.resetState() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedImageContainer(
    image: GeneratedImageEntity,
    onBack: () -> Unit,
    viewModel: MoodPicViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isOverlayEditing by remember { mutableStateOf(false) }
    var editedOverlayText by remember { mutableStateOf(image.textOverlay) }

    // Setup aspect ratios
    val sizeModifier = when (image.size) {
        "9:16" -> Modifier
            .fillMaxWidth()
            .aspectRatio(0.56f)
        "16:9" -> Modifier
            .fillMaxWidth()
            .aspectRatio(1.77f)
        else -> Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create New", style = TextStyle(fontFamily = FontFamily.SansSerif))
                }
            }

            Row {
                IconButton(onClick = { viewModel.toggleFavorite(image) }) {
                    Icon(
                        imageVector = if (image.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (image.isFavorite) Color.Red else MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Improved Prompt", image.improvedPrompt)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Aesthetic prompt copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Prompt")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // THE IMAGE CARD CANVAS WITH OPTIONAL HIGH CONTRAST TEXT OVERLAY
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .shadow(6.dp, shape = RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = sizeModifier,
                contentAlignment = Alignment.Center
            ) {
                // Coil implementation mapping local resource fallback or online URL
                val imagePainter = when (image.imageUrl) {
                    "img_feed_fashion" -> painterResource(id = R.drawable.img_feed_fashion)
                    "img_feed_gym" -> painterResource(id = R.drawable.img_feed_gym)
                    "img_feed_study" -> painterResource(id = R.drawable.img_feed_study)
                    "img_feed_travel" -> painterResource(id = R.drawable.img_feed_travel)
                    else -> null
                }

                if (imagePainter != null) {
                    Image(
                        painter = imagePainter,
                        contentDescription = "Aesthetic Generation",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = image.imageUrl,
                        contentDescription = "Aesthetic Generation",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Vignette Layer with text overlay
                if (image.textOverlay.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.1f),
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Text(
                                text = image.textOverlay,
                                color = Color.White,
                                style = TextStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.9f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 12f
                                    )
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "MoodPic AI  •  " + image.style,
                                color = Color.White.copy(alpha = 0.6f),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Generated Prompt details and modifications
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Enhanced Prompt",
                        style = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    )
                    Text(
                        text = "Style: ${image.style}",
                        style = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = image.improvedPrompt,
                        style = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.SansSerif, lineHeight = 16.sp),
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Interactive Overlay Text Editor
                if (!isOverlayEditing) {
                    OutlinedButton(
                        onClick = { isOverlayEditing = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Text", modifier = Modifier.size(14.dp))
                            Text("Edit Text Overlay", fontSize = 12.sp)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = editedOverlayText,
                            onValueChange = { editedOverlayText = it },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = TextStyle(fontSize = 12.sp),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                viewModel.updateOverlayText(image, editedOverlayText)
                                isOverlayEditing = false
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save", fontSize = 11.sp)
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

                // Creator action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveToCacheFile(image, context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.SaveAlt, contentDescription = "Download")
                            Text("Download HD")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Look what I generated using MoodPic AI! ✨\n\nPrompt: \"${image.improvedPrompt}\"\nOverlay: \"${image.textOverlay}\"\n\nCreate yours for free!")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share your MoodPic"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                            Text("Share")
                        }
                    }
                }

                TextButton(
                    onClick = {
                        viewModel.deleteGeneratedImage(image)
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Delete Image", color = Color.Red, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun TrendingTabContent(
    viewModel: MoodPicViewModel,
    onUsePrompt: (String) -> Unit
) {
    val trendingList by viewModel.trending.collectAsState()
    val suggestionsList by viewModel.dailySuggestions.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Recommendations
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Today's Visual Suggestions",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Hand-curated, highly aesthetic, updated daily.",
                style = TextStyle(fontSize = 11.sp, color = Color.Gray)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                suggestionsList.forEach { sugg ->
                    Surface(
                        modifier = Modifier
                            .width(180.dp)
                            .clickable { onUsePrompt(sugg.userIdea) },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                AsyncImage(
                                    model = sugg.imageUrl,
                                    contentDescription = "Suggestion preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                            )
                                        )
                                )
                                Text(
                                    text = sugg.textOverlay,
                                    color = Color.White,
                                    style = TextStyle(
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                )
                            }
                            Column(
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Text(
                                    text = sugg.userIdea,
                                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Style: " + sugg.style,
                                    style = TextStyle(fontSize = 9.sp, color = Color.Gray)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap to try this idea",
                                    style = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Trending Board
        item {
            Text(
                text = "Trending AI Prompts",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Most popular prompt templates among content creators.",
                style = TextStyle(fontSize = 11.sp, color = Color.Gray),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(trendingList) { trend ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = trend.style.uppercase(),
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "by ${trend.creatorName}",
                            style = TextStyle(fontSize = 11.sp, color = Color.Gray)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "\"${trend.userIdea}\"",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = trend.improvedPrompt,
                        style = TextStyle(fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onUsePrompt(trend.userIdea) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Use Mood Idea", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Improved Prompt", trend.improvedPrompt)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Aesthetic prompt copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                Text("Copy Prompt", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun HistoryTabContent(
    viewModel: MoodPicViewModel
) {
    val history by viewModel.allHistory.collectAsState()
    val favs by viewModel.favorites.collectAsState()

    var showOnlyFavorites by remember { mutableStateOf(false) }
    val displayList = if (showOnlyFavorites) favs else history

    var selectedImageForDetail by remember { mutableStateOf<GeneratedImageEntity?>(null) }

    if (selectedImageForDetail != null) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())) {
            CompletedImageContainer(
                image = selectedImageForDetail!!,
                onBack = { selectedImageForDetail = null },
                viewModel = viewModel
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved Creations",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "Your personalized history & favorites.",
                        style = TextStyle(fontSize = 11.sp, color = Color.Gray)
                    )
                }

                // Filter tabs
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val allTextSelected = !showOnlyFavorites
                    Surface(
                        onClick = { showOnlyFavorites = false },
                        shape = RoundedCornerShape(18.dp),
                        color = if (allTextSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    ) {
                        Text(
                            text = "All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            color = if (allTextSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        onClick = { showOnlyFavorites = true },
                        shape = RoundedCornerShape(18.dp),
                        color = if (showOnlyFavorites) MaterialTheme.colorScheme.primary else Color.Transparent
                    ) {
                        Text(
                            text = "Favorites",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            color = if (showOnlyFavorites) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (displayList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (showOnlyFavorites) Icons.Default.FavoriteBorder else Icons.Default.PhotoLibrary,
                            contentDescription = "Empty list",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (showOnlyFavorites) "No favorite pictures yet." else "Write your first idea to populate history!",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 40.dp)
                        )
                    }
                }
            } else {
                // High visual impact Staggered/Regular Masonry Grid of items
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_images_grid"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayList) { image ->
                        Surface(
                            onClick = { selectedImageForDetail = image },
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.75f)
                                ) {
                                    val localPainter = when (image.imageUrl) {
                                        "img_feed_fashion" -> painterResource(id = R.drawable.img_feed_fashion)
                                        "img_feed_gym" -> painterResource(id = R.drawable.img_feed_gym)
                                        "img_feed_study" -> painterResource(id = R.drawable.img_feed_study)
                                        "img_feed_travel" -> painterResource(id = R.drawable.img_feed_travel)
                                        else -> null
                                    }

                                    if (localPainter != null) {
                                        Image(
                                            painter = localPainter,
                                            contentDescription = "past image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        AsyncImage(
                                            model = image.imageUrl,
                                            contentDescription = "past image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    // Small Overlay Text Preview
                                    if (image.textOverlay.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                                    )
                                                )
                                                .padding(10.dp),
                                            contentAlignment = Alignment.BottomStart
                                        ) {
                                            Text(
                                                text = image.textOverlay,
                                                color = Color.White,
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Serif,
                                                    fontStyle = FontStyle.Italic,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                ),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Text(
                                        text = image.userIdea,
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = image.style,
                                            style = TextStyle(fontSize = 9.sp, color = Color.Gray),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Icon(
                                            imageVector = if (image.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Favorite status indicator",
                                            tint = if (image.isFavorite) Color.Red else Color.Gray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CreatorStudioTabContent(
    viewModel: MoodPicViewModel,
    currentUser: UserEntity?
) {
    var editUsername by remember { mutableStateOf(currentUser?.username ?: "CreativeSoul") }
    var editBio by remember { mutableStateOf(currentUser?.bio ?: "Seeking cozy shadows & warm film grain ✨") }
    
    // Admin Controls toggles
    var showAdminControls by remember { mutableStateOf(false) }

    // Mock Users database for ADMIN PANEL
    val mockUsers = listOf(
        UserEntity("creator_aesthetic", "AestheticMuse", "muse@moodpin.com", "+123456789", "", "Curating cozy moments.", isLoggedIn = false, isPremium = true),
        UserEntity("creator_motivation", "GritAndGrowth", "growth@moodpin.com", "+123456790", "", "Daily mental catalyst.", isLoggedIn = false, isPremium = false),
        UserEntity("creator_fashion", "VogueVibe", "vogue@moodpin.com", "+123456791", "", "High fashion, neutral hues.", isLoggedIn = false, isPremium = true)
    )

    // Mock Reported Items for ADMIN PANEL
    var mockReports by remember {
        mutableStateOf(
            listOf(
                "Inappropriate text overlay reported on user: AestheticMuse",
                "Copyright issue reported on fashion template card",
                "Spam report received on motivational quotes"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = editUsername.take(2).uppercase(),
                            color = Color.White,
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = editUsername,
                                style = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (currentUser?.isPremium == true) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified Creator Pro", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            text = currentUser?.email ?: "creator@moodpic.ai",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Followers: 1.4K  •  Following: 232",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Profile Bio Settings
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Edit Creator Profile",
                        style = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    )

                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Display Username") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Short Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { viewModel.updateBio(editUsername, editBio) },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Changes", fontSize = 12.sp)
                    }
                }
            }
        }

        // Simulated Membership Features info box
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Your Plan: " + (if (currentUser?.isPremium == true) "CREATOR PRO ACTIVE" else "BASIC COMPLIMENTARY"),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Free 100% processing using high speed Google Gemini Models.",
                        style = TextStyle(fontSize = 11.sp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Text(
                        text = "✓ Customizable high resolution aspect ratio overlay cards.",
                        style = TextStyle(fontSize = 11.sp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Text(
                        text = "✓ Ad-free experience for a completely focused workflow.",
                        style = TextStyle(fontSize = 11.sp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        // Danger actions
        item {
            OutlinedButton(
                onClick = { viewModel.clearHistory() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Text("Clear History / Reset Cache")
            }
        }

        // ADMIN CONTROLS toggle (Satisfies user request: "Admin panel to manage users, uploaded images, reported content, categories...")
        item {
            Surface(
                onClick = { showAdminControls = !showAdminControls },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = if (showAdminControls) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(
                                text = "Admin Management Panel",
                                style = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            )
                            Text(
                                text = "Manage curators, flagged content, and items",
                                style = TextStyle(fontSize = 10.sp, color = Color.Gray)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (showAdminControls) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }
        }

        if (showAdminControls) {
            // Admin lists
            item {
                Text(
                    text = "CURATOR LIST",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
                )
            }

            items(mockUsers) { user ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = user.username, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp))
                            Text(text = user.bio, style = TextStyle(fontSize = 10.sp, color = Color.Gray))
                        }
                        Button(
                            onClick = { Toast.makeText(viewModel.getApplication(), "${user.username} status toggled!", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Restrict", fontSize = 10.sp, color = Color.Red)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "FLAGGED CONTENT REPORTS",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
                )
            }

            if (mockReports.isEmpty()) {
                item {
                    Text("No outstanding reports. Platform clean!", style = TextStyle(fontSize = 11.sp, color = Color.Gray))
                }
            } else {
                items(mockReports) { report ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = report, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = {
                                    mockReports = mockReports.filter { it != report }
                                    Toast.makeText(viewModel.getApplication(), "Report dismissed.", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Dismiss", fontSize = 10.sp, color = Color.Gray)
                                }
                                TextButton(onClick = {
                                    mockReports = mockReports.filter { it != report }
                                    Toast.makeText(viewModel.getApplication(), "Content removed.", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Delete Content", fontSize = 10.sp, color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun MoodPicBottomNav(
    currentTab: MoodPicTab,
    onTabSelected: (MoodPicTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == MoodPicTab.GENERATE,
            onClick = { onTabSelected(MoodPicTab.GENERATE) },
            label = { Text("Create", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Create Generator"
                )
            },
            modifier = Modifier.testTag("nav_create_tab")
        )

        NavigationBarItem(
            selected = currentTab == MoodPicTab.TRENDING,
            onClick = { onTabSelected(MoodPicTab.TRENDING) },
            label = { Text("Trending", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            icon = {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Trending ideas"
                )
            },
            modifier = Modifier.testTag("nav_trending_tab")
        )

        NavigationBarItem(
            selected = currentTab == MoodPicTab.HISTORY,
            onClick = { onTabSelected(MoodPicTab.HISTORY) },
            label = { Text("Saved", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            icon = {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Saved history"
                )
            },
            modifier = Modifier.testTag("nav_saved_tab")
        )

        NavigationBarItem(
            selected = currentTab == MoodPicTab.CREATOR_STUDIO,
            onClick = { onTabSelected(MoodPicTab.CREATOR_STUDIO) },
            label = { Text("Studio", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Creator Studio Settings"
                )
            },
            modifier = Modifier.testTag("nav_studio_tab")
        )
    }
}
