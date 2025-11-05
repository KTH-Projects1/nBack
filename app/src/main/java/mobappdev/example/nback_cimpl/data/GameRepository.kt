package mobappdev.example.nback_cimpl.data

import mobappdev.example.nback_cimpl.NBackHelper

/**
 * Repository for game data
 * Part of the Model layer in MVVM
 */
class GameRepository(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val nBackHelper = NBackHelper()

    fun generateVisualSequence(
        size: Int,
        gridSize: Int,
        percentMatch: Int,
        nBack: Int
    ): IntArray {
        val combinations = gridSize * gridSize
        return nBackHelper.generateNBackString(size, combinations, percentMatch, nBack)
    }

    fun generateAudioSequence(
        size: Int,
        numberOfLetters: Int,
        percentMatch: Int,
        nBack: Int
    ): Array<String> {
        val numbers = nBackHelper.generateNBackString(size, numberOfLetters, percentMatch, nBack)
        return numbers.map { num ->
            ('A'.code + num - 1).toChar().toString()
        }.toTypedArray()
    }

    fun isMatch(
        sequence: IntArray,
        currentIndex: Int,
        nBack: Int
    ): Boolean {
        if (currentIndex < nBack) return false
        return sequence[currentIndex] == sequence[currentIndex - nBack]
    }

    suspend fun saveHighScore(score: Int) {
        userPreferencesRepository.saveHighScore(score)
    }

    fun getHighScore() = userPreferencesRepository.highscore
}