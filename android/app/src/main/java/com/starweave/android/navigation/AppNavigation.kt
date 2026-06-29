package com.starweave.android.navigation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.starweave.android.api.ApiClient
import com.starweave.android.ui.components.*
import com.starweave.android.ui.effect.LoginEffectCanvas
import com.starweave.android.ui.effect.LogoutEffectCanvas
import com.starweave.android.ui.screen.*
import com.starweave.android.ui.theme.StarColors
import com.starweave.android.viewmodel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

@Composable
fun AppNavigation(
    authVm: AuthViewModel = viewModel(),
    launchVm: LaunchViewModel = viewModel(),
    catchVm: CatchViewModel = viewModel(),
    starMapVm: StarMapViewModel = viewModel(),
    profileVm: ProfileViewModel = viewModel(),
    adminVm: AdminViewModel = viewModel(),
    musicPlaying: Boolean = false,
    onToggleMusic: () -> Unit = {}
) {
    val authState by authVm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var activeTab by remember { mutableStateOf("launch") }
    var viewingMeteorId by remember { mutableStateOf<Long?>(null) }
    var showLegalType by remember { mutableStateOf<String?>(null) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var starPaused by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(StarColors.BgDeep)) {
        val shouldPauseStars = starPaused ||
            activeTab == "admin" ||
            showLegalType != null ||
            showAvatarPicker ||
            authState.showLoginEffect ||
            authState.showLogoutEffect

        // Background stars (always rendered, but throttled/paused on dense overlays)
        StarFieldCanvas(paused = shouldPauseStars)

        if (!authState.isLoggedIn) {
            // Auth screen
            AuthScreen(
                isLoading = authState.isLoading,
                error = authState.error,
                captchaImage = authState.captcha?.image,
                captchaLoading = authState.captchaLoading,
                onRefreshCaptcha = { authVm.refreshCaptcha() },
                onLogin = { u, p, c -> authVm.login(u, p, c) },
                onRegister = { u, n, p, c -> authVm.register(u, n, p, c) },
                onShowLegal = { showLegalType = it },
                onClearError = { authVm.clearError() }
            )
        } else {
            // Main app
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StarColors.BgDeep.copy(alpha = 0.85f))
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("StarWeave", color = StarColors.TextPrimary, fontSize = 20.sp,
                            fontWeight = FontWeight.Thin, letterSpacing = 4.sp)
                        Text("让烦恼化作流星，消失在星河里", color = StarColors.TextTertiary, fontSize = 11.sp, letterSpacing = 1.sp)
                    }
                    // Music toggle button (spinning disc)
                    MusicToggleButton(playing = musicPlaying, onToggle = onToggleMusic)
                }

                // Page content
                Box(modifier = Modifier.weight(1f)) {
                    if (viewingMeteorId != null) {
                        MeteorDetailScreen(
                            meteorId = viewingMeteorId!!,
                            userId = authState.user?.id ?: 0L,
                            onBack = { viewingMeteorId = null }
                        )
                    } else {
                        when (activeTab) {
                            "launch" -> {
                                val user = authState.user
                                if (user != null) {
                                    val launchState by launchVm.state.collectAsStateWithLifecycle()
                                    LaunchedEffect(launchState.toastMessage) {
                                        if (launchState.toastMessage != null) {
                                            toastMessage = launchState.toastMessage
                                        }
                                    }
                                    LaunchScreen(
                                        state = launchState,
                                        userId = user.id,
                                        onPublish = { uid, content -> launchVm.publishMeteor(uid, content) },
                                        onViewMeteor = { viewingMeteorId = it },
                                        onLoadMeteors = { launchVm.loadMyMeteors(it) },
                                        onDismissHealing = { launchVm.dismissHealingEcho() },
                                        onClearError = { launchVm.clearError() }
                                    )
                                }
                            }
                            "catch" -> {
                                val user = authState.user
                                if (user != null) {
                                    val catchState by catchVm.state.collectAsStateWithLifecycle()
                                    LaunchedEffect(catchState.toastMessage) {
                                        if (catchState.toastMessage != null) {
                                            toastMessage = catchState.toastMessage
                                        }
                                    }
                                    CatchScreen(
                                        state = catchState,
                                        userId = user.id,
                                        onCatch = { catchVm.catchRandomMeteor(it) },
                                        onMakeWish = { mid, uid, content -> catchVm.makeWish(mid, uid, content) },
                                        onViewMeteor = { viewingMeteorId = it },
                                        onLoadHistory = { catchVm.loadCatchHistory(it) },
                                        onClearError = { catchVm.clearError() },
                                        onClearToast = { catchVm.clearToast() }
                                    )
                                }
                            }
                            "starmap" -> {
                                val starMapState by starMapVm.state.collectAsStateWithLifecycle()
                                StarMapScreen(
                                    state = starMapState,
                                    onInputChange = { starMapVm.updateInput(it) },
                                    onGenerate = { starMapVm.generate() },
                                    onShuffle = { starMapVm.shuffle() },
                                    onDismissSparkle = { starMapVm.dismissSparkle() },
                                    onRotateQuote = { starMapVm.rotateQuote() }
                                )
                            }
                            "profile" -> {
                                val user = authState.user
                                if (user != null) {
                                    val profileState by profileVm.state.collectAsStateWithLifecycle()
                                    LaunchedEffect(profileState.toastMessage) {
                                        if (profileState.toastMessage != null) {
                                            toastMessage = profileState.toastMessage
                                        }
                                    }
                                    ProfileScreen(
                                        user = user,
                                        state = profileState,
                                        onLoadStats = { profileVm.loadStats(it) },
                                        onUpdateProfile = { uid, nick, bio ->
                                            profileVm.updateProfile(uid, nick, bio) { updated ->
                                                if (updated != null) authVm.updateUser(updated)
                                            }
                                        },
                                        onChangePassword = { uid, old, new -> profileVm.changePassword(uid, old, new) },
                                        onLoadCaught = { profileVm.loadCaughtMeteors(it) },
                                        onLoadPublished = { profileVm.loadPublishedMeteors(it) },
                                        onLoadWishes = { profileVm.loadUserWishes(it) },
                                        onCloseOverlay = { profileVm.closeOverlay() },
                                        onViewMeteor = { viewingMeteorId = it },
                                        onShowLegal = { showLegalType = it },
                                        onLogout = { authVm.logout() },
                                        onShowAvatarPicker = { showAvatarPicker = true },
                                        onClearToast = { profileVm.clearToast() },
                                        onResetPasswordState = { profileVm.resetPasswordState() }
                                    )
                                }
                            }
                            "admin" -> {
                                val user = authState.user
                                if (user != null && user.isAdmin) {
                                    val adminState by adminVm.state.collectAsStateWithLifecycle()
                                    LaunchedEffect(adminState.toastMessage) {
                                        if (adminState.toastMessage != null) {
                                            toastMessage = adminState.toastMessage
                                        }
                                    }
                                    AdminScreen(
                                        state = adminState,
                                        adminId = user.id,
                                        onLoadAll = { adminVm.loadAll(it) },
                                        onLoadAllMeteors = { id, status -> adminVm.loadAllMeteors(id, status) },
                                        onLoadAllWishes = { id, status -> adminVm.loadAllWishes(id, status) },
                                        onLoadUsers = { adminVm.loadUsers(it) },
                                        onSetTab = { adminVm.setTab(it) },
                                        onReviewMeteor = { mid, aid, status, reason -> adminVm.reviewMeteor(mid, aid, status, reason) },
                                        onReviewWish = { wid, aid, status, reason -> adminVm.reviewWish(wid, aid, status, reason) },
                                        onDeleteMeteor = { mid, aid -> adminVm.deleteMeteor(mid, aid) },
                                        onDeleteWish = { wid, aid -> adminVm.deleteWish(wid, aid) },
                                        onDeleteUser = { uid, aid -> adminVm.deleteUser(uid, aid) },
                                        onClearToast = { adminVm.clearToast() },
                                        onClearError = { adminVm.clearError() }
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom tabs
                val locked = !authState.isLoggedIn || authState.showLoginEffect || authState.showLogoutEffect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StarColors.BgDeep.copy(alpha = 0.95f))
                        .padding(WindowInsets.navigationBars.asPaddingValues())
                ) {
                    if (!locked) {
                        BottomNavBar(
                            activeTab = activeTab,
                            onTabSelected = { activeTab = it },
                            showAdmin = authState.user?.isAdmin == true
                        )
                    }
                }
            }
        }

        // Login effect overlay
        if (authState.showLoginEffect) {
            starPaused = true
            LoginEffectCanvas {
                starPaused = false
                authVm.onLoginEffectComplete()
            }
        }

        // Logout effect overlay
        if (authState.showLogoutEffect) {
            starPaused = true
            LogoutEffectCanvas {
                starPaused = false
                authVm.onLogoutEffectComplete()
            }
        }

        // Legal overlay
        showLegalType?.let { type ->
            LegalScreen(initialTab = type, onClose = { showLegalType = null })
        }

        // Avatar picker overlay
        if (showAvatarPicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC0A0A1A))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { showAvatarPicker = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                AvatarPickerSheet(
                    onSelect = { selection ->
                        val user = authState.user ?: return@AvatarPickerSheet
                        if (selection.type == "emoji") {
                            val encoded = "emoji:${selection.char}:${selection.bg}:${selection.borderColor}"
                            // Upload emoji avatar via API
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val resp = ApiClient.getService().setAvatar(user.id, mapOf("avatarUrl" to encoded))
                                    if (resp.isSuccess && resp.data != null) {
                                        authVm.updateUser(resp.data)
                                    }
                                } catch (_: Exception) {}
                            }
                        } else if (selection.imageUri != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val uri = selection.imageUri
                                    val bytes = compressAvatarImage(context, uri)
                                    if (bytes != null) {
                                        val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                                        val part = MultipartBody.Part.createFormData("file", "avatar.jpg", requestBody)
                                        val resp = ApiClient.getService().uploadAvatar(user.id, part)
                                        if (resp.isSuccess && resp.data != null) {
                                            authVm.updateUser(resp.data)
                                        }
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                        showAvatarPicker = false
                    },
                    onDismiss = { showAvatarPicker = false }
                )
            }
        }

        // Toast
        toastMessage?.let { msg ->
            StarToast(message = msg, visible = true, onDismiss = {
                toastMessage = null
                launchVm.clearToast()
                catchVm.clearToast()
                profileVm.clearToast()
                adminVm.clearToast()
            })
        }
    }
}

private fun compressAvatarImage(context: Context, uri: Uri): ByteArray? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }
        val maxSide = 512
        var sampleSize = 1
        while (bounds.outWidth / sampleSize > maxSide || bounds.outHeight / sampleSize > maxSide) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        } ?: return null

        val scale = minOf(
            maxSide.toFloat() / decoded.width.toFloat(),
            maxSide.toFloat() / decoded.height.toFloat(),
            1f
        )
        val outputBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).toInt().coerceAtLeast(1),
                (decoded.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            decoded
        }

        ByteArrayOutputStream().use { out ->
            outputBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.toByteArray()
        }.also {
            if (outputBitmap !== decoded) outputBitmap.recycle()
            decoded.recycle()
        }
    } catch (_: Exception) {
        null
    }
}
