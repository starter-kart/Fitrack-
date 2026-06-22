package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.data.AuthManager
import com.example.data.FirestoreRepository
import com.example.network.GeminiHelper
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.MainAppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.sensor.StepSensorManager
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.delay
import org.json.JSONObject
import android.widget.Toast

class MainActivity : ComponentActivity(), PaymentResultListener {
    lateinit var viewModel: MainViewModel
    private lateinit var stepSensorManager: StepSensorManager

    private var sedentaryJob: kotlinx.coroutines.Job? = null
    private var lastSedentaryAlertTime = 0L
    private var lastWaterReminderTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Checkout.preload(applicationContext)
        enableEdgeToEdge()
        
        com.example.notification.NotificationHelper.createNotificationChannels(this)

        val authManager = AuthManager(this)
        val firestoreRepo = FirestoreRepository()
        val geminiHelper = GeminiHelper()
        val factory = MainViewModelFactory(authManager, firestoreRepo, geminiHelper)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        stepSensorManager = StepSensorManager(this) { deltaSteps ->
            if (::viewModel.isInitialized && viewModel.state.value.isAuthenticated) {
                viewModel.addStepsAutomatically(deltaSteps)
            }
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    
                    LaunchedEffect(Unit) {
                        delay(2200)
                        showSplash = false
                    }
                    
                    if (showSplash) {
                        SplashScreen()
                    } else {
                        val state by viewModel.state.collectAsState()
                        if (state.isAuthenticated) {
                            LaunchedEffect(Unit) {
                                requestPermissionsIfNeeded()
                            }
                            MainAppNavigation(viewModel)
                        } else {
                            com.example.ui.screens.AuthScreen(viewModel)
                        }
                    }
                }
            }
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissions.add(android.Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val ungranted = permissions.filter { checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
        if (ungranted.isNotEmpty()) {
            requestPermissions(ungranted.toTypedArray(), 202)
        }
    }

    private fun startBackgroundTrackingAlerts() {
        sedentaryJob?.cancel()
        sedentaryJob = lifecycleScope.launch {
            while (true) {
                delay(12000) // check health metrics every 12 seconds for responsive triggers
                if (!::viewModel.isInitialized) continue
                val state = viewModel.state.value
                if (!state.isAuthenticated || !state.profile.subscriptionActive) continue

                val profile = state.profile
                val currentTime = System.currentTimeMillis()

                // 1. Sedentary Alert Check
                if (profile.sedentaryAlertEnabled) {
                    val thresholdMillis = profile.sedentaryAlertThresholdMinutes * 60 * 1000L
                    val timeSitting = currentTime - stepSensorManager.lastActiveTimestamp
                    if (timeSitting >= thresholdMillis) {
                        if (currentTime - lastSedentaryAlertTime >= thresholdMillis) {
                            com.example.notification.NotificationHelper.showSedentaryNotification(
                                this@MainActivity,
                                profile.sedentaryAlertThresholdMinutes
                            )
                            lastSedentaryAlertTime = currentTime
                        }
                    }
                }

                // 2. Hydration Reminder Check
                if (profile.waterReminderEnabled) {
                    val intervalMillis = profile.waterReminderIntervalMinutes * 60 * 1000L
                    if (currentTime - lastWaterReminderTime >= intervalMillis) {
                        com.example.notification.NotificationHelper.showWaterReminderNotification(
                            this@MainActivity,
                            profile.waterGoalMl,
                            state.todayLog.waterIntakeMl
                        )
                        lastWaterReminderTime = currentTime
                    }
                }
            }
        }
    }

    fun triggerSedentarySimulation() {
        if (::viewModel.isInitialized) {
            val threshold = viewModel.state.value.profile.sedentaryAlertThresholdMinutes
            com.example.notification.NotificationHelper.showSedentaryNotification(this, threshold)
            Toast.makeText(this, "Sedentary reminder notification triggered!", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerWaterReminderSimulation() {
        if (::viewModel.isInitialized) {
            val state = viewModel.state.value
            com.example.notification.NotificationHelper.showWaterReminderNotification(
                this,
                state.profile.waterGoalMl,
                state.todayLog.waterIntakeMl
            )
            Toast.makeText(this, "Hydration reminder notification triggered!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::stepSensorManager.isInitialized) {
            stepSensorManager.startTracking()
        }
        startBackgroundTrackingAlerts()
    }

    override fun onPause() {
        super.onPause()
        if (::stepSensorManager.isInitialized) {
            stepSensorManager.stopTracking()
        }
        sedentaryJob?.cancel()
    }

    fun startRazorpayPayment(amountRupees: Int = 199) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_T2N2UTSqVhASIV")
        
        try {
            val options = JSONObject()
            options.put("name", "Fitrack Pro")
            options.put("description", "Fitness tracking and meal planning Lifetime Pro features")
            options.put("theme.color", "#1F3B4D")
            options.put("currency", "INR")
            options.put("amount", amountRupees * 100) // amount in paisa (199 * 100)
            
            val prefill = JSONObject()
            val userEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
            prefill.put("email", if (userEmail.isNullOrBlank()) "guest@fitrack.com" else userEmail)
            prefill.put("contact", "9999999999")
            options.put("prefill", prefill)

            val retryOpts = JSONObject()
            retryOpts.put("enabled", true)
            retryOpts.put("max_count", 4)
            options.put("retry", retryOpts)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting Razorpay checkout: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Toast.makeText(this, "Subscribed successfully! Transaction ID: $razorpayPaymentId", Toast.LENGTH_LONG).show()
        if (::viewModel.isInitialized) {
            viewModel.activateSubscription()
        }
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Subscription payment failed (code $code): $response", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFF0F1113)),
        contentAlignment = Alignment.Center
    ) {
        // High quality background artwork
        Image(
            painter = painterResource(id = R.drawable.img_splash_art_1781626508921),
            contentDescription = "Splash art background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )
        
        // Gradient overlay for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color(0xFF0F1113).copy(alpha = 0.85f),
                            androidx.compose.ui.graphics.Color(0xFF0F1113)
                        )
                    )
                )
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.weight(1.5f))
            
            // Sleek branding card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(96.dp)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_1781626463398),
                        contentDescription = "Fitrack logo",
                        modifier = Modifier.size(72.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Fitrack",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Track your fitness, achieve your goals.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(1.5f))
            
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Removed old LoginScreen in favor of AuthScreen
