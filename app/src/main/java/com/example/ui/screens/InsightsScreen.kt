package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.R
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val state by viewModel.state.collectAsState()
    
    var goalsInput by remember { mutableStateOf(if (state.profile.goals.isEmpty()) "gain 5 kg" else state.profile.goals) }

    var sedentaryEnabled by remember(state.profile) { mutableStateOf(state.profile.sedentaryAlertEnabled) }
    var waterGoalInput by remember(state.profile) { mutableStateOf(if (state.profile.waterGoalMl == 0) "2000" else state.profile.waterGoalMl.toString()) }
    var waterReminderEnabled by remember(state.profile) { mutableStateOf(state.profile.waterReminderEnabled) }

    val currentIntake = state.dailyLogs.firstOrNull { it.date == state.todayLog.date }?.waterIntakeMl ?: state.todayLog.waterIntakeMl
    val dailyGoal = waterGoalInput.toIntOrNull() ?: 2000
    val progress = if (dailyGoal > 0) (currentIntake.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f

    Scaffold(
        containerColor = Color(0xFFF7F8FC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TOP APP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pro Insights",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            color = Color(0xFF1E1E2D)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEBE7FF)
                    ) {
                        Text(
                            text = "PRO",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFF6B58FF)
                        )
                    }
                }

                // Right Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEBE7FF),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Pro",
                                tint = Color(0xFF6B58FF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PRO ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color(0xFF6B58FF)
                            )
                        }
                    }
                    
                    Box(modifier = Modifier.clickable { /* Notifications */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF1E1E2D),
                            modifier = Modifier.size(28.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                                .size(10.dp)
                                .background(Color(0xFFFF4B4B), CircleShape)
                        )
                    }
                }
            }

            // Subtitle
            Text(
                text = "Smart tools. Smarter you.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6C6C80),
                modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-12).dp)
            )

            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF1EFFF))
            ) {
                // Background effects
                Image(
                    painter = painterResource(id = R.drawable.fitness_hero),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(180.dp).offset(x = 20.dp)
                )

                Row(modifier = Modifier.fillMaxSize().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp).background(Color.White.copy(alpha=0.6f), CircleShape), contentAlignment = Alignment.Center) {
                         Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Color(0xFF6B58FF), modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.65f), verticalArrangement = Arrangement.Center) {
                        Text("PRO MEMBER STATUS ACTIVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF5948FF))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("You have permanently unlocked all state-of-the-art AI-driven fitness strategy engines, custom hydration triggers, and active posture alerts!", style = MaterialTheme.typography.labelSmall.copy(fontSize=10.sp), color = Color(0xFF4A4B6B), lineHeight = 14.sp)
                    }
                }
            }

            // Hydration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(0xFFE3F2FD)) {
                            Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Hydration Tracker & Logs", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E1E2D)))
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text("Today's Intake", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E1E2D))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$currentIntake ml ", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = Color(0xFF1E1E2D)))
                            Text("/ $dailyGoal ml", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF6C6C80)), modifier = Modifier.padding(bottom = 2.dp))
                        }
                        
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                            CircularProgressIndicator(
                                progress = { progress },
                                color = Color(0xFF2196F3),
                                trackColor = Color(0xFFF3F3F3),
                                strokeCap = StrokeCap.Round,
                                strokeWidth = 6.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF2196F3)))
                                Text("Done", style = MaterialTheme.typography.labelSmall.copy(fontSize=8.sp), color = Color(0xFF6C6C80))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF2196F3),
                        trackColor = Color(0xFFE3F2FD),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("0 ml", style = MaterialTheme.typography.labelSmall.copy(fontSize=8.sp), color = Color(0xFF6C6C80))
                        Text("${dailyGoal / 2} ml", style = MaterialTheme.typography.labelSmall.copy(fontSize=8.sp), color = Color(0xFF6C6C80))
                        Text("$dailyGoal ml", style = MaterialTheme.typography.labelSmall.copy(fontSize=8.sp), color = Color(0xFF6C6C80))
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Log Water Intake", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E1E2D))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(250, 500, 750).forEach { amount ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFF0F7FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha=0.3f)),
                                modifier = Modifier.clickable { viewModel.addWaterIntake(amount) }.weight(1f).padding(horizontal = 4.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+$amount ml", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2196F3))
                                }
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFFF0F0),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4B4B).copy(alpha=0.3f)),
                            modifier = Modifier.clickable { viewModel.resetWaterIntake() }.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color(0xFFFF4B4B), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFF4B4B))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color(0xFFF3F3F3))
                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Configure Hydration Target & Reminders", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E1E2D))
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = waterGoalInput,
                        onValueChange = { waterGoalInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Daily Water Goal (ml)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6C6C80)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Color(0xFF2196F3)) },
                        trailingIcon = { Text("ml", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6C6C80), modifier = Modifier.padding(end=16.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFEBEBEB),
                            focusedBorderColor = Color(0xFF6B58FF),
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Periodic Hydration Reminders", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E1E2D))
                            Text("Receive recurring notification alerts", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6C6C80))
                        }
                        Switch(
                            checked = waterReminderEnabled,
                            onCheckedChange = { waterReminderEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2196F3))
                        )
                    }
                }
            }

            // Posture Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF2FBFA)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color(0xFFE8F7F0)) {
                        Icon(Icons.Filled.DirectionsWalk, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.padding(8.dp).size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Posture & Sedentary Alerts", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E1E2D))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Inactivity Warning Alarm", style = MaterialTheme.typography.labelSmall.copy(fontWeight=FontWeight.Bold), color = Color(0xFF1E1E2D))
                        Text("Notify if remaining in one location longer than limit", style = MaterialTheme.typography.labelSmall.copy(fontSize=10.sp), color = Color(0xFF6C6C80))
                    }
                    Switch(
                        checked = sedentaryEnabled,
                        onCheckedChange = { sedentaryEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF4CAF50))
                    )
                }
            }

            // Interactive Simulator Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(0xFFF3E8FF)) {
                            Icon(Icons.Filled.Campaign, contentDescription = null, tint = Color(0xFF6B58FF), modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Interactive Notification Simulator", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E1E2D))
                            Text("Test notifications without manual waiting directly.\nClick below to verify device alerts immediately!", style = MaterialTheme.typography.labelSmall.copy(fontSize=10.sp), color = Color(0xFF6C6C80), lineHeight=14.sp)
                        }
                        Box(contentAlignment=Alignment.TopEnd) {
                            Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Color(0xFF6B58FF), modifier = Modifier.size(36.dp).padding(4.dp))
                            Box(modifier = Modifier.size(14.dp).background(Color(0xFFFF4B4B), CircleShape), contentAlignment=Alignment.Center) {
                                Text("1", color = Color.White, fontSize = 8.sp, fontWeight=FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { com.example.notification.NotificationHelper.showSedentaryNotification(context, 60) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B58FF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Sedentary", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Button(
                            onClick = { com.example.notification.NotificationHelper.showWaterReminderNotification(context, 2000, 500) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Hydration", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Save Settings Button
            Button(
                onClick = {
                    val wGoal = waterGoalInput.toIntOrNull() ?: 2000
                    viewModel.updateSubscriptionSettings(
                        sedentaryAlertEnabled = sedentaryEnabled,
                        sedentaryAlertMinutes = 60,
                        waterReminderEnabled = waterReminderEnabled,
                        waterReminderMinutes = 60,
                        waterGoalMl = wGoal
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B58FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Premium Settings & Alarm Timers", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }

            // Set Your Current Fitness Goals
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Set Your Current Fitness Goals", style = MaterialTheme.typography.titleMedium.copy(fontWeight=FontWeight.Bold), color = Color(0xFF1E1E2D))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = goalsInput,
                    onValueChange = { goalsInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFEBEBEB),
                        focusedBorderColor = Color(0xFF6B58FF),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    leadingIcon = { Icon(Icons.Filled.TrackChanges, contentDescription = null, tint = Color(0xFF6B58FF)) },
                    trailingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, tint = Color(0xFF6C6C80), modifier = Modifier.size(18.dp)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF1E1E2D))
                )
            }

            // Assemble AI Plan Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF2196F3), Color(0xFF6B58FF))))
                    .clickable {
                        // Action here if any
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Assemble AI Plan & Meal Guides", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
