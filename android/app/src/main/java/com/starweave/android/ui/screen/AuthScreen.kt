package com.starweave.android.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starweave.android.ui.theme.StarColors

@Composable
fun AuthScreen(
    isLoading: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onShowLegal: (String) -> Unit,
    onClearError: () -> Unit
) {
    var isLogin by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title
        Text("✦", fontSize = 40.sp, color = StarColors.AccentCyan)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "StarWeave", fontSize = 28.sp, fontWeight = FontWeight.Thin,
            letterSpacing = 6.sp, color = StarColors.TextPrimary
        )
        Text(
            "让烦恼化作流星，消失在星河里", fontSize = 12.sp,
            color = StarColors.TextSecondary, letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Tab switcher
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
                            if (isLogin == isLoginTab) Brush.linearGradient(
                                listOf(StarColors.GradientPurple, StarColors.GradientCyan)
                            ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable { isLogin = isLoginTab; onClearError() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (isLogin == isLoginTab) StarColors.BgDeep else StarColors.TextSecondary,
                        fontWeight = FontWeight.Medium, fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Form card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StarColors.BgCard, RoundedCornerShape(18.dp))
                .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            // Nickname (register only)
            AnimatedVisibility(visible = !isLogin) {
                Column {
                    Text("昵称（选填）", color = StarColors.TextTertiary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
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
                onValueChange = { username = it },
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
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (agreed && username.isNotBlank() && password.length >= 6) {
                        if (isLogin) onLogin(username, password)
                        else onRegister(username, nickname.ifBlank { username }, password)
                    }
                }),
                modifier = Modifier.fillMaxWidth(),
                colors = starTextFieldColors()
            )

            // Error
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = StarColors.DangerRed, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Agreement checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = { agreed = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = StarColors.AccentCyan,
                        uncheckedColor = StarColors.TextTertiary
                    )
                )
                Column {
                    Row {
                        Text(
                            "已阅读并同意 ", fontSize = 11.sp,
                            color = StarColors.TextTertiary
                        )
                        Text(
                            "用户协议", fontSize = 11.sp, color = StarColors.AccentCyan,
                            modifier = Modifier.clickable { onShowLegal("agreement") }
                        )
                        Text(" 和 ", fontSize = 11.sp, color = StarColors.TextTertiary)
                        Text(
                            "隐私政策", fontSize = 11.sp, color = StarColors.AccentCyan,
                            modifier = Modifier.clickable { onShowLegal("policy") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Submit button
            val canSubmit = agreed && username.isNotBlank() && password.length >= 6 && !isLoading
            Button(
                onClick = {
                    if (isLogin) onLogin(username, password)
                    else onRegister(username, nickname.ifBlank { username }, password)
                },
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
                            if (canSubmit) Brush.linearGradient(
                                listOf(StarColors.GradientPurple, StarColors.GradientCyan)
                            ) else Brush.linearGradient(
                                listOf(Color(0x20C9A7FF), Color(0x208BE9FD))
                            ),
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
                            fontWeight = FontWeight.Medium, fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
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
