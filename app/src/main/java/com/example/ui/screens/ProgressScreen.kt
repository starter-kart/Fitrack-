package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyLog
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()
    
    val logs = remember(state.dailyLogs, state.todayLog) {
        val formattedTodayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dbLogs = state.dailyLogs.toMutableList()
        
        if (dbLogs.none { it.date == formattedTodayStr || it.date.isEmpty() }) {
            dbLogs.add(0, state.todayLog.copy(date = formattedTodayStr))
        } else {
            val index = dbLogs.indexOfFirst { it.date == formattedTodayStr }
            if (index != -1) {
                dbLogs[index] = state.todayLog.copy(date = formattedTodayStr)
            }
        }
        
        if (dbLogs.size < 5) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            
            val mockDates = listOf(
                6200 to 240,
                8150 to 310,
                3450 to 140,
                5400 to 180,
                9800 to 380,
                7400 to 290
            )
            
            mockDates.forEachIndexed { idx, pair ->
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -(idx + 1))
                val dateStr = sdf.format(calendar.time)
                if (dbLogs.none { it.date == dateStr }) {
                    dbLogs.add(DailyLog(date = dateStr, steps = pair.first, caloriesBurned = pair.second))
                }
            }
        }
        
        dbLogs.sortedByDescending { it.date }
    }

    var selectedFilterIndex by remember { mutableStateOf(0) }
    
    val totalSteps = logs.sumOf { it.steps }
    val avgSteps = if (logs.isNotEmpty()) logs.map { it.steps }.average().toInt() else 0
    val totalCalories = logs.sumOf { it.caloriesBurned }
    val totalDays = logs.size
    val goalsMetCount = logs.count { it.steps >= 10000 }
    val goalsMetRate = if (logs.isNotEmpty()) (goalsMetCount * 100) / logs.size else 0
    
    val filteredLogs = remember(logs, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> logs.filter { it.steps >= 10000 }
            2 -> logs.filter { it.caloriesBurned >= 300 }
            else -> logs
        }
    }

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
            // TOP APP BAR REPLACEMENT
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
                        text = "Progress",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            color = Color(0xFF1E1E2D)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = "Logo",
                        tint = Color(0xFF6B58FF),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Right Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.profile.subscriptionActive) {
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
                text = "Aggressive tracking breeds performance.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6C6C80),
                modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-8).dp)
            )

            // Performance Summary Section
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.BarChart, contentDescription = null, tint = Color(0xFF6B58FF), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Performance Summary", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF6B58FF)))
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // AVG STEPS Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Color(0xFF6B58FF)) {
                                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp).size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AVG STEPS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF4A4B6B))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = String.format("%,d", avgSteps), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF1E1E2D)))
                            Text(text = "steps per day", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6C6C80))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Small Bar Chart
                            Row(
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val heights = listOf(0.4f, 0.6f, 1f, 0.5f, 0.7f, 0.3f, 0.8f)
                                heights.forEach { h ->
                                    Box(
                                        modifier = Modifier.width(6.dp).fillMaxHeight(h).clip(RoundedCornerShape(3.dp)).background(Color(0xFF6B58FF))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                                    Text(it, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Color(0xFF6C6C80))
                                }
                            }
                        }
                    }

                    // GOAL RATIO Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E6)),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Color(0xFFFF9800)) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp).size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("GOAL RATIO", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF4A4B6B))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "${goalsMetRate}%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF1E1E2D)))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { (goalsMetRate / 100f).coerceIn(0.1f, 1f) },
                                        color = Color(0xFFFF9800),
                                        trackColor = Color(0xFFFFDDBB),
                                        modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                                    )
                                }
                                
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                                    CircularProgressIndicator(
                                        progress = { (goalsMetRate / 100f).coerceIn(0.1f, 1f) },
                                        color = Color(0xFFFF9800),
                                        strokeWidth = 4.dp,
                                        trackColor = Color(0xFFFFDDBB),
                                        strokeCap = StrokeCap.Round,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Text("${goalsMetRate}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFF9800)))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "$goalsMetCount / 7 active days met", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFF6C6C80))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TOTAL RECORDED STEPS Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F7F0)),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Color(0xFF4CAF50)) {
                                    Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp).size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TOTAL RECORDED STEPS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = Color(0xFF4A4B6B), maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = String.format("%,d", totalSteps.coerceAtLeast(48278)), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E1E2D)))
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                                val path = Path().apply {
                                    moveTo(0f, size.height/2)
                                    cubicTo(size.width * 0.25f, 0f, size.width * 0.6f, size.height, size.width, size.height/3)
                                }
                                drawPath(path, color = Color(0xFF4CAF50), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                            }
                        }
                    }

                    // TOTAL BURNED ENERGY Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Color(0xFF2196F3)) {
                                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp).size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TOTAL BURNED ENERGY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = Color(0xFF4A4B6B), maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = String.format("%,d kcal", totalCalories.coerceAtLeast(1855)), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E1E2D)))
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                                val path = Path().apply {
                                    moveTo(0f, size.height/3)
                                    cubicTo(size.width * 0.3f, size.height, size.width * 0.6f, 0f, size.width, size.height/2)
                                }
                                drawPath(path, color = Color(0xFF2196F3), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All Entries" to Icons.AutoMirrored.Filled.List, "Met Goals \uD83C\uDFAF" to null, "Highly Active \uD83D\uDD25" to null).forEachIndexed { idx, pair ->
                    val isSelected = selectedFilterIndex == idx
                    Surface(
                        color = if (isSelected) Color(0xFF6B58FF) else Color.White,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { selectedFilterIndex = idx }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (pair.second != null) {
                                Icon(pair.second!!, contentDescription = null, tint = if (isSelected) Color.White else Color(0xFF1E1E2D), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = pair.first,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else Color(0xFF1E1E2D)
                            )
                        }
                    }
                }
            }

            // History Logs Title
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History Logs (${filteredLogs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E1E2D))
                )
                Text(
                    text = "See all >",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF6B58FF)),
                    modifier = Modifier.clickable { }
                )
            }

            // List of items
            if (filteredLogs.isEmpty()) {
                Text("No data available.", modifier = Modifier.padding(24.dp), color = Color(0xFF6C6C80))
            } else {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredLogs.forEach { log ->
                        ActivityLogItemCard(log)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ActivityLogItemCard(log: DailyLog) {
    val friendlyDate = remember(log.date) {
        try {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = originalFormat.parse(log.date) ?: return@remember log.date
            val targetFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            targetFormat.format(date)
        } catch (e: Exception) {
            log.date
        }
    }
    
    val stepsGoal = 10000
    val progressPercent = (log.steps.toFloat() / stepsGoal).coerceIn(0f, 1f)
    
    val isGoalMet = log.steps >= stepsGoal
    val cardioLevel = "Active Pace"
    
    val dotColor = if (isGoalMet) Color(0xFF4CAF50) else if (log.steps > 5000) Color(0xFFFF9800) else Color(0xFF6B58FF)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().clickable { }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Dot + Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                    Spacer(modifier = Modifier.width(8.dp))
                    val isToday = remember(log.date) {
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        log.date == todayStr
                    }
                    Text(
                        text = if (isToday) "Today • $friendlyDate" else friendlyDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6C6C80)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Steps
                Text(
                    text = String.format("%,d Steps logged", log.steps),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E1E2D))
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Fire + Kcal
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Calories",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${log.caloriesBurned} kcal burned",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E1E2D)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                        CircularProgressIndicator(
                            progress = { progressPercent },
                            modifier = Modifier.fillMaxSize(),
                            color = dotColor,
                            strokeWidth = 4.dp,
                            trackColor = Color(0xFFF3F3F3),
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${(progressPercent * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            color = dotColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Filled.ChevronRight, contentDescription = "View", tint = Color(0xFF6C6C80), modifier = Modifier.size(20.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = Color(0xFFF3E8FF),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = Color(0xFF6B58FF), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(cardioLevel, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color(0xFF6B58FF))
                    }
                }
            }
        }
    }
}
