package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Steps, 1 = Weight
    
    // Construct real weekly data based on dailyLogs and bmiHistory
    val stepsGoal = 10000
    
    val recentLogs = state.dailyLogs.reversed().takeLast(6)
    val stepsList = remember(recentLogs, state.todayLog) {
        val list = recentLogs.filter { it.date != state.todayLog.date }.map { it.steps }.toMutableList()
        list.add(state.todayLog.steps)
        list
    }
    val weekDays = remember(recentLogs, state.todayLog) {
        val list = recentLogs.filter { it.date != state.todayLog.date }.map { it.date.takeLast(5) }.toMutableList()
        list.add("Today")
        list
    }
    
    val recentBmi = state.bmiHistory.reversed().takeLast(7)
    val weightList = remember(recentBmi, state.profile) {
        if (recentBmi.isEmpty()) {
            listOf(if (state.profile.weight > 0) state.profile.weight else 0f)
        } else {
            recentBmi.map { it.weight }
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
            horizontalAlignment = Alignment.CenterHorizontally
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
                        text = "FitTrack",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            color = Color(0xFF1E1E2D)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Timeline,
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
                                    imageVector = Icons.Filled.FlashOn,
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

            // GREETING
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp),
            ) {
                Text(
                    text = "Welcome back, Champion! \uD83D\uDC4B",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6C6C80)
                )
            }

            // HERO BANNER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(240.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFEAE5FF), Color(0xFFF6E2FF), Color(0xFFDDF6F9))
                        )
                    )
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.fitness_hero),
                    contentDescription = "Runner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.55f)
                        .align(Alignment.CenterEnd)
                        .offset(x = 10.dp)
                )

                // Overlay Content (Left side)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 12.dp)
                        .align(Alignment.CenterStart),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = "Steps",
                            tint = Color(0xFF6B58FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Today's Steps",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF4A4B6B)
                        )
                    }

                    Box(
                        modifier = Modifier.size(110.dp).offset(y = (-4).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { (state.todayLog.steps.toFloat() / stepsGoal).coerceIn(0f, 1f) },
                            color = Color(0xFF6B58FF),
                            strokeWidth = 6.dp,
                            trackColor = Color.White.copy(alpha=0.6f),
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.todayLog.steps}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp
                                ),
                                color = Color(0xFF1E1E2D)
                            )
                            Text(
                                text = "Steps",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6C6C80)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${(state.todayLog.steps * 100 / stepsGoal).coerceIn(0, 100)}% of 10,000 Goal",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = Color(0xFF6C6C80)
                            )
                        }
                    }
                    
                    Surface(
                        color = Color(0xFF6B58FF),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.clickable { /* Navigate to insights */ }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("View Insights", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                
                // Top Right Analytics Icon Button over Image
                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = "Analytics",
                        tint = Color(0xFF6B58FF),
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // KEY STATISTICS ROW
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Calories Burned Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFFE8F7F0), shape = RoundedCornerShape(8.dp)) {
                                Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.padding(6.dp).size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calories Burned", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6C6C80))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${state.todayLog.caloriesBurned}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("kcal", style = MaterialTheme.typography.labelMedium, color = Color(0xFF1E1E2D), modifier = Modifier.padding(bottom = 2.dp))
                        }
                        Text("Estimated active loss", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFF6C6C80))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        // Draw a wave
                        Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                            val path = Path().apply {
                                moveTo(0f, size.height/2)
                                cubicTo(size.width * 0.25f, 0f, size.width * 0.75f, size.height, size.width, size.height/2)
                            }
                            drawPath(path, color = Color(0xFF4CAF50), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                            
                            val path2 = Path().apply {
                                moveTo(0f, size.height/2 + 8.dp.toPx())
                                cubicTo(size.width * 0.25f, 8.dp.toPx(), size.width * 0.75f, size.height + 8.dp.toPx(), size.width, size.height/2 + 8.dp.toPx())
                            }
                            drawPath(path2, color = Color(0xFF4CAF50).copy(alpha=0.3f), style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }
                }

                // Goal Progress Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFFFFF1E6), shape = RoundedCornerShape(8.dp)) {
                                Icon(Icons.Outlined.TrackChanges, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.padding(6.dp).size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Goal Progress", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6C6C80))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("${(state.todayLog.steps * 100 / stepsGoal).coerceIn(0, 100)}%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Keep pushing!", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFF6C6C80))
                        
                        // Fills the rest to align bottom items correctly
                        Spacer(modifier = Modifier.weight(1f, fill = false))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LinearProgressIndicator(
                            progress = { (state.todayLog.steps.toFloat() / stepsGoal).coerceIn(0.02f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFF9800),
                            trackColor = Color(0xFFF3F3F3)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CURRENT WEIGHT CARD
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFFF3E8FF), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Outlined.MonitorWeight, contentDescription = null, tint = Color(0xFF6B58FF), modifier = Modifier.padding(12.dp).size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Current Weight", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6C6C80))
                            Text(
                                text = if (state.profile.weight > 0) "${state.profile.weight} kg" else "-- kg", 
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0xFF6B58FF),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { /* Update */ }
                            ) {
                                Text("Update", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                            }
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Accessibility, contentDescription = null, tint = Color(0xFF6B58FF), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Age: ${state.profile.age}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6C6C80))
                            Text("Height: ${state.profile.height} cm", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6C6C80))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PROGRESS ANALYTICS
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Progress Analytics (Weekly)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E1E2D)))
                        
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(30.dp)).background(Color(0xFFF3F3F3)).padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(30.dp)).background(if (selectedTab == 0) Color(0xFF6B58FF) else Color.Transparent).clickable { selectedTab = 0 }.padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("Steps", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (selectedTab == 0) Color.White else Color(0xFF6C6C80))
                            }
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(30.dp)).background(if (selectedTab == 1) Color.White else Color.Transparent).clickable { selectedTab = 1 }.padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("Weight", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (selectedTab == 1) Color(0xFF1E1E2D) else Color(0xFF6C6C80))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Chart
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        if (selectedTab == 0) {
                            val maxSteps = stepsList.maxOrNull()?.toFloat()?.coerceAtLeast(10000f) ?: 10000f
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val barWidth = 8.dp.toPx()
                                val numBars = stepsList.size
                                
                                // Background guidelines & text
                                val textPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#A0A0B0")
                                    textSize = 10.sp.toPx()
                                    isAntiAlias = true
                                }
                                
                                val stepTicks = listOf("10K", "8K", "6K", "4K", "2K", "0")
                                stepTicks.forEachIndexed { idx, tick ->
                                    val yPos = h * (idx / 5f)
                                    if(idx < 5) {
                                        drawLine(
                                            color = Color(0xFFF3F3F3),
                                            start = Offset(24.dp.toPx(), yPos),
                                            end = Offset(w, yPos),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        )
                                    }
                                    drawContext.canvas.nativeCanvas.drawText(tick, 0f, yPos + 4.dp.toPx(), textPaint)
                                }

                                // Bars
                                val startX = 36.dp.toPx()
                                val availableW = w - startX
                                val actualSpace = if (numBars > 1) (availableW - (barWidth * numBars)) / (numBars - 1) else availableW / 2
                                
                                stepsList.forEachIndexed { index, value ->
                                    val xOffset = startX + index * (barWidth + actualSpace)
                                    val progressPercentage = (value.toFloat() / maxSteps).coerceIn(0.01f, 1.0f)
                                    // Height inversion for rendering bottom-up
                                    val barHeight = h * progressPercentage
                                    val yOffset = h - barHeight

                                    val isToday = index == stepsList.size - 1
                                    drawRoundRect(
                                        color = if (isToday) Color(0xFF6B58FF) else Color(0xFFF3F3F3),
                                        topLeft = Offset(xOffset, yOffset),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                    
                                    if (isToday) {
                                        // Draw count above
                                        val valStr = value.toString()
                                        val valPaint = android.graphics.Paint().apply {
                                            color = android.graphics.Color.parseColor("#6B58FF")
                                            textSize = 10.sp.toPx()
                                            textAlign = android.graphics.Paint.Align.CENTER
                                            isAntiAlias = true
                                            isFakeBoldText = true
                                        }
                                        drawContext.canvas.nativeCanvas.drawText(valStr, xOffset + barWidth/2, yOffset - 8.dp.toPx(), valPaint)
                                    }
                                }
                            }
                        } else {
                            val maxW = (weightList.maxOrNull() ?: 100f) + 2f
                            val minW = (weightList.minOrNull() ?: 60f) - 2f
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val numPoints = weightList.size
                                val startX = 36.dp.toPx()
                                val availableW = w - startX
                                val actualSpace = if (numPoints > 1) availableW / (numPoints - 1) else availableW / 2
                                val range = maxW - minW
                                
                                val points = weightList.mapIndexed { index, value ->
                                    val x = startX + index * actualSpace
                                    val y = h - ((value - minW) / range) * h
                                    Offset(x, y)
                                }
                                
                                val strokePath = Path().apply {
                                    if(points.isNotEmpty()){
                                        moveTo(points[0].x, points[0].y)
                                        for(i in 1 until points.size){
                                            val currentPoint = points[i]
                                            val previousPoint = points[i-1]
                                            val controlPointX = (previousPoint.x + currentPoint.x)/2
                                            cubicTo(controlPointX, previousPoint.y, controlPointX, currentPoint.y, currentPoint.x, currentPoint.y)
                                        }
                                    }
                                }
                                
                                drawPath(strokePath, color = Color(0xFF6B58FF), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                                
                                points.forEachIndexed { idx, point ->
                                    drawCircle(
                                        color = if (idx == points.size - 1) Color(0xFF6B58FF) else Color(0xFFEBE7FF),
                                        radius = 6.dp.toPx(),
                                        center = point
                                    )
                                    if(idx == points.size - 1){
                                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { day ->
                            Text(day, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA0A0B0), modifier = Modifier.width(34.dp), textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Surface(
                        color = Color(0xFFF7F8FC),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF6B58FF), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val avgBase = if (selectedTab == 0) {
                                "Weekly average: ${stepsList.average().toInt()} steps • Keep pushing towards 10k!"
                            } else {
                                val diff = weightList.first() - weightList.last()
                                val diffString = String.format("%.1f", Math.abs(diff))
                                "Weekly trend: ${if(diff >= 0) "Lost" else "Gained"} $diffString kg"
                            }
                            Text(avgBase, style = MaterialTheme.typography.labelMedium, color = Color(0xFF4A4B6B))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // GO PREMIUM CARD
            if (!state.profile.subscriptionActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4C6BFF), Color(0xFF8E54E9))
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(color = Color.White.copy(alpha=0.2f), shape = CircleShape) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.padding(10.dp).size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Go Premium", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Unlock advanced insights, custom goals and ad-free experience.", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha=0.8f))
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            modifier = Modifier.clickable { viewModel.activateSubscription() }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Upgrade Now", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF6B58FF))
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF6B58FF), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

