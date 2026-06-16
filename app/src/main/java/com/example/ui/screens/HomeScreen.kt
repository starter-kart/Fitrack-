package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Steps, 1 = Weight
    var activeBarIndex by remember { mutableStateOf(-1) }
    
    // Construct real + mock weekly data based on todayLog and weight
    val stepsGoal = 10000
    val weightTarget = (state.profile.height - 100f) * 0.9f // ideal body weight estimation
    
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    // Smoothly integrate user's current todayLog step values as the final (Sunday) point
    val stepsList = remember(state.todayLog.steps) {
        val baseSteps = listOf(6200, 7800, 5400, 9200, 8100, 11400)
        baseSteps + state.todayLog.steps
    }
    
    // Smoothly integrate user's current weight value as the final (current) weight
    val weightList = remember(state.profile.weight) {
        val baseWeight = if (state.profile.weight > 0) state.profile.weight else 75.0f
        listOf(
            baseWeight + 1.8f,
            baseWeight + 1.2f,
            baseWeight + 1.4f,
            baseWeight + 0.8f,
            baseWeight + 0.5f,
            baseWeight + 0.2f,
            baseWeight
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Dashboard", 
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Welcome back, Champion!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    if (state.profile.subscriptionActive) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "PRO ACTIVE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // KEY STATISTICS ROW (Grid replacement)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Today's Steps Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TODAY STEPS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${state.todayLog.steps}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (state.todayLog.steps.toFloat() / stepsGoal).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(state.todayLog.steps * 100 / stepsGoal)}% of Goal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Calories Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("CALORIES BURNED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${state.todayLog.caloriesBurned} kcal", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Estimated active loss",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // PROFILE OVERVIEW & BMI CARD
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Weight",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (state.profile.weight > 0) "${state.profile.weight} kg" else "-- kg",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Age: ${state.profile.age} | Height: ${state.profile.height} cm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // BMI Visual Badge
                    if (state.profile.weight > 0) {
                        val bmi = state.profile.bmi
                        val bmiCategory = when {
                            bmi < 18.5f -> "Underweight"
                            bmi < 25f -> "Normal"
                            bmi < 30f -> "Overweight"
                            else -> "Obese"
                        }
                        val bmiColor = when {
                            bmi < 18.5f -> MaterialTheme.colorScheme.primary
                            bmi < 25f -> Color(0xFF4CAF50)
                            bmi < 30f -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = bmiColor.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = String.format("BMI: %.1f", bmi),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = bmiColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bmiCategory,
                                style = MaterialTheme.typography.labelSmall,
                                color = bmiColor
                            )
                        }
                    }
                }
            }

            // GORGEOUS INTERACTIVE VISUAL TREND ANALYTICS SECTION
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = AssistChipDefaults.assistChipBorder(enabled = true),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress Analytics (Weekly)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        
                        // Mini Segmented Tabs
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedTab = 0 }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Steps", 
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedTab = 1 }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Weight", 
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // THE DYNAMIC CANVAS TREND CHARTS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        if (selectedTab == 0) {
                            // STEPS BAR CHART
                            val maxSteps = stepsList.maxOrNull()?.toFloat() ?: 12000f
                            val primaryColor = MaterialTheme.colorScheme.primary
                            val accentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val barWidth = 32.dp.toPx()
                                val numBars = stepsList.size
                                val spaceBetween = (canvasWidth - (barWidth * numBars)) / (numBars + 1)

                                // Draw background guidelines
                                for (i in 1..3) {
                                    val yValue = canvasHeight * (i / 4f)
                                    drawLine(
                                        color = Color.Gray.copy(alpha = 0.15f),
                                        start = Offset(0f, yValue),
                                        end = Offset(canvasWidth, yValue),
                                        strokeWidth = 2f
                                    )
                                }

                                // Draw individual steps bars
                                stepsList.forEachIndexed { index, value ->
                                    val xOffset = spaceBetween + index * (barWidth + spaceBetween)
                                    val progressPercentage = (value.toFloat() / maxSteps).coerceIn(0.05f, 1.0f)
                                    val barHeight = canvasHeight * progressPercentage
                                    val yOffset = canvasHeight - barHeight

                                    // Special highlight for Sunday (the active today's entry)
                                    val fillBrush = if (index == stepsList.size - 1) {
                                        Brush.verticalGradient(
                                            colors = listOf(primaryColor, primaryColor.copy(alpha = 0.6f))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(accentColor.copy(alpha = 1.0f), accentColor.copy(alpha = 0.5f))
                                        )
                                    }

                                    drawRoundRect(
                                        brush = fillBrush,
                                        topLeft = Offset(xOffset, yOffset),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                    )
                                }
                            }
                        } else {
                            // WEIGHT PROGRESS LINE CHART
                            val minW = (weightList.minOrNull() ?: 60f) - 2f
                            val maxW = (weightList.maxOrNull() ?: 100f) + 2f
                            val valRange = maxW - minW
                            val lineColor = MaterialTheme.colorScheme.primary
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val numPoints = weightList.size
                                val xSpace = canvasWidth / (numPoints - 1)

                                val points = weightList.mapIndexed { index, value ->
                                    val x = index * xSpace
                                    val y = canvasHeight - ((value - minW) / valRange) * canvasHeight
                                    Offset(x, y)
                                }

                                // 1. Draw smooth bezier path
                                val strokePath = Path().apply {
                                    if (points.isNotEmpty()) {
                                        moveTo(points[0].x, points[0].y)
                                        for (i in 1 until points.size) {
                                            val currentPoint = points[i]
                                            val previousPoint = points[i - 1]
                                            val controlPointX = (previousPoint.x + currentPoint.x) / 2
                                            
                                            cubicTo(
                                                controlPointX, previousPoint.y,
                                                controlPointX, currentPoint.y,
                                                currentPoint.x, currentPoint.y
                                            )
                                        }
                                    }
                                }

                                // 2. Draw Translucent area under line
                                val fillPath = Path().apply {
                                    addPath(strokePath)
                                    if (points.isNotEmpty()) {
                                        lineTo(points.last().x, canvasHeight)
                                        lineTo(points.first().x, canvasHeight)
                                        close()
                                    }
                                }

                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                                        startY = 0f,
                                        endY = canvasHeight
                                    )
                                )

                                drawPath(
                                    path = strokePath,
                                    color = lineColor,
                                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // 3. Draw dots representing active elements
                                points.forEachIndexed { idx, point ->
                                    drawCircle(
                                        color = if (idx == points.size - 1) lineColor else lineColor.copy(alpha = 0.5f),
                                        radius = if (idx == points.size - 1) 7.dp.toPx() else 5.dp.toPx(),
                                        center = point
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Labels below chart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // Trends Insights Breakdown Info Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Analysis Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedTab == 0) {
                                val avg = stepsList.average().toInt()
                                "Weekly average: $avg steps • ${if(avg >= 7000) "Highly active pacing" else "Keep pushing towards 10k!"}"
                            } else {
                                val netWeightChange = weightList.first() - weightList.last()
                                val changePrefix = if (netWeightChange >= 0) "Lost" else "Gained"
                                "Weight trend: $changePrefix ${String.format("%.1f", Math.abs(netWeightChange))} kg across the week."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

