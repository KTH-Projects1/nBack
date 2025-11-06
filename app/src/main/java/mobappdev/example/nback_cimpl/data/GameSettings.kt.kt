package mobappdev.example.nback_cimpl.data

data class GameSettings(
    val nValue: Int = 2,                    // N-back level (1-4)
    val numberOfEvents: Int = 20,            // Number of events per game (10-50)
    val eventInterval: Long = 2000L,         // Time between events in milliseconds (1000-5000)
    val gridSize: Int = 3,                   // Grid size for visual mode (3-5)
    val numberOfLetters: Int = 8             // Number of letters for audio mode (6-10)
) {

    val intervalSeconds: Float
        get() = eventInterval / 1000f


    val gridDimensions: String
        get() = "${gridSize}×${gridSize}"
}