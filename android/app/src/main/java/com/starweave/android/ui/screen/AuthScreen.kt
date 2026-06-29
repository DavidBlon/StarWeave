package com.starweave.android.ui.screen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.ui.theme.StarColors

@Composable
fun AuthScreen(
    isLoading: Boolean,
    error: String?,
    captchaImage: String?,
    captchaLoading: Boolean,
    onRefreshCaptcha: () -> Unit,
    onLogin: (String, String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onShowLegal: (String) -> Unit,
    onClearError: () -> Unit
) {
    var isLogin by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var captcha by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }
    val captchaBitmap = remember(captchaImage) { decodeCaptchaImage(captchaImage) }

    fun submit() {
        if (isLogin) onLogin(username, password, captcha)
        else onRegister(username, nickname.ifBlank { username }, password, captcha)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text("✦", fontSize = 40.sp, color = StarColors.AccentCyan)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "StarWeave",
            fontSize = 28.sp,
            fontWeight = FontWeight.Thin,
            letterSpacing = 6.sp,
            color = StarColors.TextPrimary
        )
        Text(
            "让烦恼化作流星，消失在星河里",
            fontSize = 12.sp,
            color = StarColors.TextSecondary,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StarColors.BgCard, RoundedCornerShape(50.dp))
                .padding(4.dp)
        ) {
            listOf(true to "登录", false to "注册").forEach { (isLoginTab, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (isLogin == isLoginTab) {
                                Brush.linearGradient(listOf(StarColors.GradientPurple, StarColors.GradientCyan))
                            } else {
                                Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .clickable {
                            isLogin = isLoginTab
                            captcha = ""
                            onClearError()
                            onRefreshCaptcha()
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (isLogin == isLoginTab) StarColors.BgDeep else StarColors.TextSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StarColors.BgCard, RoundedCornerShape(18.dp))
                .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            AnimatedVisibility(visible = !isLogin) {
                Column {
                    Text("昵称", color = StarColors.TextTertiary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it.take(20) },
                        placeholder = { Text("给自己取个名字吧", color = StarColors.TextTertiary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = starTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Text("用户名", color = StarColors.TextTertiary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.filter { ch -> ch.isLetterOrDigit() }.take(20) },
                placeholder = { Text("输入用户名", color = StarColors.TextTertiary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                colors = starTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("密码", color = StarColors.TextTertiary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("输入密码", color = StarColors.TextTertiary) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = starTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("验证码", color = StarColors.TextTertiary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = captcha,
                    onValueChange = { captcha = it.take(6) },
                    placeholder = { Text("输入验证码", color = StarColors.TextTertiary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (agreed && username.isNotBlank() && password.length >= 6 && captcha.isNotBlank()) {
                            submit()
                        }
                    }),
                    modifier = Modifier.weight(1f),
                    colors = starTextFieldColors()
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .width(112.dp)
                        .height(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StarColors.BgDeep)
                        .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(10.dp))
                        .clickable(enabled = !captchaLoading) {
                            captcha = ""
                            onRefreshCaptcha()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (captchaLoading) {
                        CircularProgressIndicator(
                            color = StarColors.AccentCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (!captchaImage.isNullOrBlank()) {
                        if (captchaBitmap != null) {
                            Image(
                                bitmap = captchaBitmap,
                                contentDescription = "刷新验证码",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )
                        } else {
                            Text("刷新", color = StarColors.TextSecondary, fontSize = 12.sp)
                        }
                    } else {
                        Text("刷新", color = StarColors.TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = StarColors.DangerRed, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = { agreed = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = StarColors.AccentCyan,
                        uncheckedColor = StarColors.TextTertiary
                    )
                )
                Row {
                    Text("已阅读并同意 ", fontSize = 11.sp, color = StarColors.TextTertiary)
                    Text(
                        "用户协议",
                        fontSize = 11.sp,
                        color = StarColors.AccentCyan,
                        modifier = Modifier.clickable { onShowLegal("agreement") }
                    )
                    Text(" 和 ", fontSize = 11.sp, color = StarColors.TextTertiary)
                    Text(
                        "隐私政策",
                        fontSize = 11.sp,
                        color = StarColors.AccentCyan,
                        modifier = Modifier.clickable { onShowLegal("policy") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val canSubmit = agreed &&
                username.isNotBlank() &&
                password.length >= 6 &&
                captcha.isNotBlank() &&
                !isLoading
            Button(
                onClick = { submit() },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color(0x15FFFFFF)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (canSubmit) {
                                Brush.linearGradient(listOf(StarColors.GradientPurple, StarColors.GradientCyan))
                            } else {
                                Brush.linearGradient(listOf(Color(0x20C9A7FF), Color(0x208BE9FD)))
                            },
                            RoundedCornerShape(50.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = StarColors.BgDeep,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            if (isLogin) "登录" else "注册",
                            color = if (canSubmit) StarColors.BgDeep else StarColors.TextTertiary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

private fun decodeCaptchaImage(value: String?): ImageBitmap? {
    if (value.isNullOrBlank()) return null
    return try {
        val payload = value.substringAfter("base64,", value)
        val bytes = Base64.decode(payload, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun starTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = StarColors.TextPrimary,
        unfocusedTextColor = StarColors.TextPrimary,
        focusedBorderColor = StarColors.AccentCyan.copy(alpha = 0.5f),
        unfocusedBorderColor = Color(0x30FFFFFF),
        cursorColor = StarColors.AccentCyan,
        focusedContainerColor = StarColors.BgDeep,
        unfocusedContainerColor = StarColors.BgDeep
    )
}
