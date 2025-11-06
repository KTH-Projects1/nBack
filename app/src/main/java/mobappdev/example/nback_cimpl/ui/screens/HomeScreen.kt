package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.data.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: GameVM,
    onNavigateToGame: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val highscore by vm.highscore.collectAsState()
    val gameState by vm.gameState.collectAsState()
    val settings by vm.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("N-Back Memory Game") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Test Your Working Memory",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // High Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏆 High Score",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$highscore",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Current Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = onNavigateToSettings) {
                            Text("Change")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Mode: ${gameState.gameType.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• N-Back Level: ${settings.nValue}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Events per Round: ${settings.numberOfEvents}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Time Between Events: ${settings.intervalSeconds}s",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Grid Size: ${settings.gridDimensions}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Audio Letters: ${settings.numberOfLetters} (A-${('A' + settings.numberOfLetters - 1)})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))


            Text(
                text = "Select Game Mode",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedButton(
                    onClick = {
                        vm.setGameType(GameType.Audio)
                        vm.startGame()
                        onNavigateToGame()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.sound_on),
                            contentDescription = "Audio Mode",
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Audio", style = MaterialTheme.typography.titleMedium)
                        Text("Letters", style = MaterialTheme.typography.bodySmall)
                    }
                }

                ElevatedButton(
                    onClick = {
                        vm.setGameType(GameType.Visual)
                        vm.startGame()
                        onNavigateToGame()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.visual),
                            contentDescription = "Visual Mode",
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Visual", style = MaterialTheme.typography.titleMedium)
                        Text("${settings.gridDimensions} Grid", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            FilledTonalButton(
                onClick = {
                    vm.setGameType(GameType.AudioVisual)
                    vm.startGame()
                    onNavigateToGame()
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(100.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎯", style = MaterialTheme.typography.displaySmall)
                    Text("Dual Mode", style = MaterialTheme.typography.titleMedium)
                    Text("Audio + Visual", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}