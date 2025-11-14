package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.data.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameVM
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    vm: GameVM,
    onNavigateBack: () -> Unit
) {
    val gameState by vm.gameState.collectAsState()
    val settings by vm.settings.collectAsState()
    val score by vm.score.collectAsState()
    val scope = rememberCoroutineScope()

    val buttonScale = remember { Animatable(1f) }

    DisposableEffect(Unit) {
        onDispose {
            vm.stopGame()
        }
    }

    LaunchedEffect(gameState.lastResponseCorrect) { // Feedback 2 (Animation): Denna effekt körs *bara* när `lastResponseCorrect` ändras.
        gameState.lastResponseCorrect?.let {
            buttonScale.animateTo(1.2f, animationSpec = tween(100))
            buttonScale.animateTo(1f, animationSpec = tween(100))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("N-Back Game (N=${settings.nValue})") },
                navigationIcon = {
                    TextButton(onClick = {
                        vm.stopGame()  // Stoppa spelet innan vi går tillbaka
                        onNavigateBack()
                    }) {
                        Text("← Back")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Score", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "$score",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Event", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${gameState.currentIndex + 1} / ${gameState.totalEvents}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = if (gameState.totalEvents > 0) {
                            (gameState.currentIndex + 1).toFloat() / gameState.totalEvents
                        } else 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Display Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (gameState.gameType) {
                    GameType.Visual -> {
                        VisualGrid(
                            gridSize = settings.gridSize,
                            highlightedPosition = gameState.eventValue,
                            modifier = Modifier.fillMaxSize(0.85f)
                        )
                    }
                    GameType.Audio -> {
                        AudioDisplay(
                            letter = gameState.audioValue,
                            modifier = Modifier.size(250.dp)
                        )
                    }
                    GameType.AudioVisual -> { // I `GameScreen`, om GameType är AudioVisual, ritar vi ut *båda* composables (Grid + Ljud).
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            VisualGrid(
                                gridSize = settings.gridSize,
                                highlightedPosition = gameState.eventValue,
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .aspectRatio(1f)
                            )
                            AudioDisplay(
                                letter = gameState.audioValue,
                                modifier = Modifier.size(120.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Match Button
            Button(
                onClick = { // 1. (View) UI:t skickar en "event" (händelse) till ViewModel:n.
                    vm.checkMatch()
                    scope.launch {
                        buttonScale.animateTo(1.15f, animationSpec = tween(80))
                        buttonScale.animateTo(1f, animationSpec = tween(80))
                    }
                },
                enabled = gameState.isGameRunning && gameState.canRespond,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .scale(buttonScale.value),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (gameState.lastResponseCorrect) {
                        true -> Color(0xFF4CAF50)
                        false -> Color(0xFFF44336)
                        null -> MaterialTheme.colorScheme.primary
                        // Feedback 1 (Färg): `when`-satsen ändrar knappens färg (grön/röd) baserat på statet.
                    }
                )
            ) {
                Text(
                    text = if (gameState.canRespond) "MATCH!" else "Wait...",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status
            if (!gameState.isGameRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉 Game Over!", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Final Score: $score",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisualGrid(
    gridSize: Int,
    highlightedPosition: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in 0 until gridSize) {
            Row(
                modifier = Modifier.weight(1f), // `weight(1f)` gör att alla celler och rader delar lika på utrymmet (responsivt).
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until gridSize) {
                    val position = row * gridSize + col // Räknar ut det "platta" indexet (0-8) från rad- och kolumn-looparna.
                    val isHighlighted = position == highlightedPosition
                    // Jämför cellens index med det aktiva indexet från gameState.

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background( // Styr färgen på Box-composablen baserat på om den ska vara markerad.
                                color = if (isHighlighted) {
                                    Color(0xFF2196F3)
                                } else {
                                    Color(0xFFE0E0E0)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = if (isHighlighted) {
                                    Color(0xFF1976D2)
                                } else {
                                    Color(0xFFBDBDBD)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun AudioDisplay(
    letter: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter ?: "♪",
                style = MaterialTheme.typography.displayLarge,
                color = Color(0xFF1976D2),
                fontSize = MaterialTheme.typography.displayLarge.fontSize * 2
            )
        }
    }
}