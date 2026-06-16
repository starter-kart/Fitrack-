package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    var stepsInput by remember { mutableStateOf(state.todayLog.steps.toString()) }
    var caloriesInput by remember { mutableStateOf(state.todayLog.caloriesBurned.toString()) }

    var ageInput by remember { mutableStateOf(state.profile.age.toString()) }
    var heightInput by remember { mutableStateOf(state.profile.height.toString()) }
    var weightInput by remember { mutableStateOf(state.profile.weight.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Update Log") })
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
            Text("Daily Activity", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = stepsInput,
                onValueChange = { stepsInput = it },
                label = { Text("Steps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = caloriesInput,
                onValueChange = { caloriesInput = it },
                label = { Text("Calories Burned") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val stp = stepsInput.toIntOrNull() ?: 0
                    val cal = caloriesInput.toIntOrNull() ?: 0
                    viewModel.updateDailyLog(stp, cal)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Daily Activity")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Profile & BMI", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = ageInput,
                onValueChange = { ageInput = it },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = heightInput,
                onValueChange = { heightInput = it },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = {
                    val age = ageInput.toIntOrNull() ?: 0
                    val h = heightInput.toFloatOrNull() ?: 0f
                    val w = weightInput.toFloatOrNull() ?: 0f
                    viewModel.updateProfile(age, h, w, state.profile.goals)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Profile")
            }
        }
    }
}
