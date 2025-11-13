package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.data.GameSettings
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentSettings: GameSettings,
    onSettingsChanged: (GameSettings) -> Unit,
    onNavigateBack: () -> Unit
) {

    var nValue by remember { mutableStateOf(currentSettings.nValue) }// Använder `remember` för att hålla sliderns värde *temporärt* i minnet (UI state).
    var numberOfEvents by remember { mutableStateOf(currentSettings.numberOfEvents) }
    var eventInterval by remember { mutableStateOf(currentSettings.intervalSeconds) }
    var gridSize by remember { mutableStateOf(currentSettings.gridSize) }
    var numberOfLetters by remember { mutableStateOf(currentSettings.numberOfLetters) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { // I TopAppBar, när användaren går tillbaka
                    IconButton(onClick = {
                        val newSettings = GameSettings(
                            nValue = nValue,
                            numberOfEvents = numberOfEvents,
                            eventInterval = (eventInterval * 1000).toLong(),
                            gridSize = gridSize,
                            numberOfLetters = numberOfLetters
                        )
                        onSettingsChanged(newSettings) // skapas nya inställningar och skickas till VM:n via denna callback.
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚙️ Game Configuration",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Customize your N-Back experience. Settings are saved automatically when you go back.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }


            SettingSlider(
                title = "N-Back Level",
                description = "How many steps back to remember",
                value = nValue.toFloat(),
                valueRange = 1f..4f,
                steps = 2,
                onValueChange = { nValue = it.roundToInt() },
                valueLabel = "N = $nValue"
            )


            SettingSlider(
                title = "Number of Events",
                description = "Total events per game round",
                value = numberOfEvents.toFloat(),
                valueRange = 10f..50f,
                steps = 7,
                onValueChange = { numberOfEvents = it.roundToInt() },
                valueLabel = "$numberOfEvents events"
            )


            SettingSlider(
                title = "Time Between Events",
                description = "Delay between each stimulus",
                value = eventInterval,
                valueRange = 1f..5f,
                steps = 7,
                onValueChange = { eventInterval = it },
                valueLabel = "${String.format("%.1f", eventInterval)}s"
            )


            SettingSlider(
                title = "Visual Grid Size",
                description = "Size of the position grid",
                value = gridSize.toFloat(),
                valueRange = 3f..5f,
                steps = 1,
                onValueChange = { gridSize = it.roundToInt() },
                valueLabel = "${gridSize}×${gridSize} grid"
            )

            SettingSlider(
                title = "Audio Letters",
                description = "Number of letters to use",
                value = numberOfLetters.toFloat(),
                valueRange = 6f..10f,
                steps = 3,
                onValueChange = { numberOfLetters = it.roundToInt() },
                valueLabel = "$numberOfLetters letters (A-${('A' + numberOfLetters - 1)})"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "💡 Tip: Higher N-back levels and more letters make the game harder!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun SettingSlider(
    title: String,
    description: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    valueLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}