package mobappdev.example.nback_cimpl.data

/**
 * Represents the current state of the game
 * Part of the Model layer in MVVM
 */
data class GameState(
    val gameType: GameType = GameType.Visual,
    val eventValue: Int = -1,
    val audioValue: String? = null,
    val currentIndex: Int = 0,
    val isGameRunning: Boolean = false,
    val totalEvents: Int = 0,
    val lastResponseCorrect: Boolean? = null,
    val canRespond: Boolean = false
)

enum class GameType {
    Audio,
    Visual,
    AudioVisual
}