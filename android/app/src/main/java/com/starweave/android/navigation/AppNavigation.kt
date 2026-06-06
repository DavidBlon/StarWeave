package com.starweave.android.navigation

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
    val authState by authVm.state.collectAsState()
    val launchState by launchVm.state.collectAsState()
    val catchState by catchVm.state.collectAsState()
    val starMapState by starMapVm.state.collectAsState()
    val profileState by profileVm.state.collectAsState()
    val adminState by adminVm.state.collectAsState()
    val context = LocalContext.current

    var activeTab by remember { mutableStateOf("launch") }
    var viewingMeteorId by remember { mutableStateOf<Long?>(null) }
    var showLegalType by remember { mutableStateOf<String?>(null) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var starPaused by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(StarColors.BgDeep)) {
        // Background stars (always rendered)
        StarFieldCanvas(paused = starPaused)

        if (!authState.isLoggedIn) {
            // Auth screen
            AuthScreen(
                isLoading = authState.isLoading,
                error = authState.error,
                onLogin = { u, p -> authVm.login(u, p) },
                onRegister = { u, n, p -> authVm.register(u, n, p) },
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
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    if (inputStream != null) {
                                        val bytes = inputStream.readBytes()
                                        inputStream.close()
                                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                                        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
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
        val currentToast = toastMessage ?: launchState.toastMessage ?: catchState.toastMessage
        ?: profileState.toastMessage ?: adminState.toastMessage
        currentToast?.let { msg ->
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
