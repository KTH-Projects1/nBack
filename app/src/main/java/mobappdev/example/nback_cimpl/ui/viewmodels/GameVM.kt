package mobappdev.example.nback_cimpl.ui.viewmodels

import mobappdev.example.nback_cimpl.GameApplication
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.data.GameRepository
import mobappdev.example.nback_cimpl.data.GameSettings
import mobappdev.example.nback_cimpl.data.GameState
import mobappdev.example.nback_cimpl.data.GameType


class GameVM(
    private val repository: GameRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState()) // Privat & Muterbar (föränderlig): *Endast* VM:n kan ändra detta state.
    val gameState: StateFlow<GameState> = _gameState.asStateFlow() // Publik & Oföränderlig (read-only): Exponerar statet till Vyn (t.ex. GameScreen) för observation.

    private val _score = MutableStateFlow(0) // Logik: Håller den nuvarande poängen för denna omgång.
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _highscore = MutableStateFlow(0) // Logik: Håller det högsta sparade poängvärdet.
    val highscore: StateFlow<Int> = _highscore.asStateFlow()

    private val _settings = MutableStateFlow(GameSettings()) // Logik: Håller de nuvarande inställningarna (N-värde, hastighet, etc.).
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private var visualSequence = intArrayOf() // Logik: Tomma arrayer som kommer hålla stimuli-sekvenserna (siffror/bokstäver) från C-koden.
    private var audioSequence = arrayOf<String>()
    private var gameJob: Job? = null

    private val respondedEvents = mutableSetOf<Int>() // Logik: Ett Set för att logga vilka events användaren redan svarat på (förhindrar dubbelsvar).
    private var correctResponses = 0
    private var totalResponses = 0

    init { // Logik: Detta körs *en* gång, direkt när GameVM skapas.

        viewModelScope.launch {
            repository.getHighScore().collect { highScore ->
                _highscore.value = highScore
            }
        }


        viewModelScope.launch {
            repository.getSettings().collect { gameSettings ->
                _settings.value = gameSettings
                Log.d("GameVM", "Settings updated: $gameSettings")
            }
        }
    }

    fun setGameType(gameType: GameType) {
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    fun startGame() {
        gameJob?.cancel()
        resetGameState()

        val currentSettings = _settings.value

        when (_gameState.value.gameType) {
            GameType.Visual -> {
                visualSequence = repository.generateVisualSequence( // VM:n ber Repositoryn om data, utan att veta *hur* den skapas.
                    size = currentSettings.numberOfEvents,
                    gridSize = currentSettings.gridSize,
                    percentMatch = 30,
                    nBack = currentSettings.nValue
                )
                Log.d("GameVM", "Visual sequence: ${visualSequence.contentToString()}")
            }
            GameType.Audio -> {
                audioSequence = repository.generateAudioSequence(
                    size = currentSettings.numberOfEvents,
                    numberOfLetters = currentSettings.numberOfLetters,
                    percentMatch = 30,
                    nBack = currentSettings.nValue
                )
                Log.d("GameVM", "Audio sequence: ${audioSequence.contentToString()}")
            }
            GameType.AudioVisual -> {  // I `startGame`, om GameType är AudioVisual, genererar vi *båda* sekvenserna.
                visualSequence = repository.generateVisualSequence(
                    size = currentSettings.numberOfEvents,
                    gridSize = currentSettings.gridSize,
                    percentMatch = 30,
                    nBack = currentSettings.nValue
                )
                audioSequence = repository.generateAudioSequence(
                    size = currentSettings.numberOfEvents,
                    numberOfLetters = currentSettings.numberOfLetters,
                    percentMatch = 30,
                    nBack = currentSettings.nValue
                )
                Log.d("GameVM", "Dual sequences generated")
            }
        }
        // Logik: Uppdatera statet för att tala om för UI:t att spelet nu är igång!
        _gameState.value = _gameState.value.copy(
            isGameRunning = true,
            totalEvents = currentSettings.numberOfEvents,
            currentIndex = 0
        )

        startGameLoop()
    }

    fun checkMatch() {
        val currentIndex = _gameState.value.currentIndex
        val nBack = _settings.value.nValue

        if (currentIndex < nBack) {
            Log.d("GameVM", "Too early to check match")
            return
        }

        if (respondedEvents.contains(currentIndex)) {
            Log.d("GameVM", "Already responded")
            return
        }

        respondedEvents.add(currentIndex) // Logik: Registrera att användaren nu svarat på detta event.
        totalResponses++

        var isCorrect = false

        when (_gameState.value.gameType) {
            GameType.Visual -> { // Logik: Jämför event[nu] med event[nu - N].
                isCorrect = repository.isMatch(visualSequence, currentIndex, nBack)
                Log.d("GameVM", "Visual match: $isCorrect")
            }
            GameType.Audio -> {
                isCorrect = audioSequence[currentIndex] == audioSequence[currentIndex - nBack]
                Log.d("GameVM", "Audio match: $isCorrect")
            }
            GameType.AudioVisual -> {
                val visualMatch = repository.isMatch(visualSequence, currentIndex, nBack)
                val audioMatch = audioSequence[currentIndex] == audioSequence[currentIndex - nBack]
                isCorrect = visualMatch || audioMatch // I `checkMatch` är gissningen korrekt om *antingen* visuell *eller* ljud-match stämmer.
                Log.d("GameVM", "Dual match: $isCorrect (V:$visualMatch, A:$audioMatch)")
            }
        }

        if (isCorrect) {
            correctResponses++
            _score.value++ // Uppdatera poäng-statet
        }

        _gameState.value = _gameState.value.copy( // Logik: Uppdatera statet så UI:t vet om svaret var rätt (för grön/röd färg-feedback).
            lastResponseCorrect = isCorrect
        )
    }

    private fun startGameLoop() {
        val currentSettings = _settings.value

        gameJob = viewModelScope.launch { // `viewModelScope` är bundet till VM:ns livscykel, inte Vyns. Denna coroutine överlever rotation.
            for (index in 0 until currentSettings.numberOfEvents) {
                _gameState.value = _gameState.value.copy( // Uppdaterar spelets "state" med det nya eventet (vilken ruta/ljud som visas).
                    currentIndex = index,
                    eventValue = if (_gameState.value.gameType != GameType.Audio) {
                        visualSequence[index]
                    } else -1,
                    audioValue = if (_gameState.value.gameType != GameType.Visual) {
                        audioSequence[index]
                    } else null,
                    canRespond = index >= currentSettings.nValue,
                    lastResponseCorrect = null
                )

                delay(currentSettings.eventInterval) // Pausar coroutinen (spelloopen) asynkront enligt användarens inställning (t.ex. 2 sek).
            }

            endGame()
        }
    }

    private fun endGame() {
        _gameState.value = _gameState.value.copy(isGameRunning = false)

        viewModelScope.launch { // Logik: Starta en separat "fire-and-forget"-coroutine för att spara highscore
            if (_score.value > _highscore.value) {
                repository.saveHighScore(_score.value) // 2. (ViewModel) VM:n har logiken och ber Repositoryn att spara datan.
            }
        }

        Log.d("GameVM", "Game ended - Score: ${_score.value}")
    }

    private fun resetGameState() {
        respondedEvents.clear()
        correctResponses = 0
        totalResponses = 0
        _score.value = 0
        _gameState.value = GameState(gameType = _gameState.value.gameType)
    }

    fun updateSettings(newSettings: GameSettings) { // VM:n tar emot de nya inställningarna och startar en coroutine för att spara dem.
        viewModelScope.launch {
            // VM:n ber Repositoryn att *permanent* spara (persistera) inställningarna (till DataStore).
            repository.saveSettings(newSettings)
            Log.d("GameVM", "Settings saved: $newSettings")
        }
    }

    override fun onCleared() {
        super.onCleared() // Logik (Städning): När VM:n förstörs (appen stängs), avbryt spelloopen.
        gameJob?.cancel()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM( // Logik: Hämta Application-kontexten.
                    repository = GameRepository(
                        userPreferencesRepository = application.userPreferencesRespository,
                        settingsRepository = application.settingsRepository
                        // "Dependency Injection": Vi *ger* GameRepository sina beroenden (andra repos) när den skapas.
                    )
                )
            }
        }
    }
}