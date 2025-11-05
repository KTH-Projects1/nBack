package mobappdev.example.nback_cimpl.ui.viewmodels

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
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.data.GameRepository
import mobappdev.example.nback_cimpl.data.GameState
import mobappdev.example.nback_cimpl.data.GameType

class GameVM(
    private val repository: GameRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _highscore = MutableStateFlow(0)
    val highscore: StateFlow<Int> = _highscore.asStateFlow()

    val nBack: Int = 2
    private val eventInterval: Long = 2000L
    private val numberOfEvents: Int = 20
    private val gridSize: Int = 3
    private val numberOfLetters: Int = 8

    private var visualSequence = intArrayOf()
    private var audioSequence = arrayOf<String>()
    private var gameJob: Job? = null

    private val respondedEvents = mutableSetOf<Int>()
    private var correctResponses = 0
    private var totalResponses = 0

    init {
        viewModelScope.launch {
            repository.getHighScore().collect { highScore ->
                _highscore.value = highScore
            }
        }
    }

    fun setGameType(gameType: GameType) {
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    fun startGame() {
        gameJob?.cancel()
        resetGameState()

        when (_gameState.value.gameType) {
            GameType.Visual -> {
                visualSequence = repository.generateVisualSequence(
                    size = numberOfEvents,
                    gridSize = gridSize,
                    percentMatch = 30,
                    nBack = nBack
                )
                Log.d("GameVM", "Visual sequence: ${visualSequence.contentToString()}")
            }
            GameType.Audio -> {
                audioSequence = repository.generateAudioSequence(
                    size = numberOfEvents,
                    numberOfLetters = numberOfLetters,
                    percentMatch = 30,
                    nBack = nBack
                )
                Log.d("GameVM", "Audio sequence: ${audioSequence.contentToString()}")
            }
            GameType.AudioVisual -> {
                visualSequence = repository.generateVisualSequence(
                    size = numberOfEvents,
                    gridSize = gridSize,
                    percentMatch = 30,
                    nBack = nBack
                )
                audioSequence = repository.generateAudioSequence(
                    size = numberOfEvents,
                    numberOfLetters = numberOfLetters,
                    percentMatch = 30,
                    nBack = nBack
                )
                Log.d("GameVM", "Dual sequences generated")
            }
        }

        _gameState.value = _gameState.value.copy(
            isGameRunning = true,
            totalEvents = numberOfEvents,
            currentIndex = 0
        )

        startGameLoop()
    }

    fun checkMatch() {
        val currentIndex = _gameState.value.currentIndex

        if (currentIndex < nBack) {
            Log.d("GameVM", "Too early to check match")
            return
        }

        if (respondedEvents.contains(currentIndex)) {
            Log.d("GameVM", "Already responded")
            return
        }

        respondedEvents.add(currentIndex)
        totalResponses++

        var isCorrect = false

        when (_gameState.value.gameType) {
            GameType.Visual -> {
                isCorrect = repository.isMatch(visualSequence, currentIndex, nBack)
            }
            GameType.Audio -> {
                isCorrect = audioSequence[currentIndex] == audioSequence[currentIndex - nBack]
            }
            GameType.AudioVisual -> {
                val visualMatch = repository.isMatch(visualSequence, currentIndex, nBack)
                val audioMatch = audioSequence[currentIndex] == audioSequence[currentIndex - nBack]
                isCorrect = visualMatch || audioMatch
            }
        }

        if (isCorrect) {
            correctResponses++
            _score.value++
        }

        _gameState.value = _gameState.value.copy(
            lastResponseCorrect = isCorrect
        )
    }

    private fun startGameLoop() {
        gameJob = viewModelScope.launch {
            for (index in 0 until numberOfEvents) {
                _gameState.value = _gameState.value.copy(
                    currentIndex = index,
                    eventValue = if (_gameState.value.gameType != GameType.Audio) {
                        visualSequence[index]
                    } else -1,
                    audioValue = if (_gameState.value.gameType != GameType.Visual) {
                        audioSequence[index]
                    } else null,
                    canRespond = index >= nBack,
                    lastResponseCorrect = null
                )

                delay(eventInterval)
            }

            endGame()
        }
    }

    private fun endGame() {
        _gameState.value = _gameState.value.copy(isGameRunning = false)

        viewModelScope.launch {
            if (_score.value > _highscore.value) {
                repository.saveHighScore(_score.value)
            }
        }
    }

    private fun resetGameState() {
        respondedEvents.clear()
        correctResponses = 0
        totalResponses = 0
        _score.value = 0
        _gameState.value = GameState(gameType = _gameState.value.gameType)
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(
                    repository = GameRepository(application.userPreferencesRespository)
                )
            }
        }
    }
}