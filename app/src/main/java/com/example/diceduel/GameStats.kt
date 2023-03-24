package com.example.diceduel

class GameStats(var humanWins: Int = 0, var computerWins: Int = 0) {
    fun reset() {
        humanWins = 0
        computerWins = 0
    }
}
